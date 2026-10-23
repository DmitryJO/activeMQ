package rt.vt.activemq.dictionary;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.activemq.broker-url=vm://amq-only-test?broker.persistent=false",
        "app.jms.dictionary-queue=amq-only-test.queue"
})
@ActiveProfiles("amq-only")
@ExtendWith(OutputCaptureExtension.class)
class AmqOnlyFlowTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JmsTemplate jmsTemplate;

    @LocalServerPort
    private int port;

    @Test
    void startsWithoutDatabaseOrPersistenceHandlers() {
        assertThat(context.getBeansOfType(DataSource.class)).isEmpty();
        assertThat(context.getBeansOfType(EntityManagerFactory.class)).isEmpty();
        assertThat(context.getBeansOfType(DictionaryRepository.class)).isEmpty();
        assertThat(context.getBeansOfType(DictionaryService.class)).isEmpty();
        assertThat(context.getBeansOfType(DictionaryListener.class)).isEmpty();
        assertThat(context.getBeansOfType(ExternalDictionaryClient.class)).isEmpty();
        assertThat(context.getBeansOfType(LoggingDictionaryListener.class)).hasSize(1);
    }

    @Test
    void logsIncomingTextWithoutRequiringDictionaryJson(CapturedOutput output) {
        jmsTemplate.convertAndSend("amq-only-test.queue", "AMQ_ONLY_RAW_TEXT");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(output.getOut()).contains(
                        "Received ActiveMQ message (amq-only): AMQ_ONLY_RAW_TEXT"));
    }

    @Test
    void decodesUtf8BytesMessageWithCyrillic(CapturedOutput output) {
        String json = """
                {"Type":"{MDM}Справочник.СтраныМира","Код":"792","Наименование":"ТУРЦИЯ"}
                """.strip();
        jmsTemplate.send("amq-only-test.queue", session -> {
            var message = session.createBytesMessage();
            message.writeBytes(json.getBytes(StandardCharsets.UTF_8));
            return message;
        });

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(output.getOut()).contains(
                        "Received ActiveMQ message (amq-only): " + json));
    }

    @Test
    void preservesTextEvenWhenItLooksLikeByteValues(CapturedOutput output) {
        String text = "123,13,10,32,34,-48,-95";
        jmsTemplate.convertAndSend("amq-only-test.queue", text);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(output.getOut()).contains(
                        "Received ActiveMQ message (amq-only): " + text));
    }

    @Test
    void publishEndpointWorksButDatabaseEndpointsAreUnavailable(CapturedOutput output) {
        RestClient client = RestClient.create("http://localhost:" + port);
        var response = client.post().uri("/api/dictionaries/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"type":"TEST","code":"AMQ_ONLY_HTTP","name":"Connection test"}
                        """)
                .retrieve().toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(output.getOut()).containsPattern(
                        "Received ActiveMQ message \\(amq-only\\): .*AMQ_ONLY_HTTP"));

        for (String path : new String[]{"/api/dictionaries", "/api/dictionaries/TEST"}) {
            Integer status = client.get().uri(path)
                    .exchange((request, result) -> result.getStatusCode().value());
            assertThat(status).isEqualTo(404);
        }
    }
}
