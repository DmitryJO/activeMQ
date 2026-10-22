package rt.vt.activemq.dictionary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class DictionaryFlowTest {

    @Autowired
    private DictionaryPublisher publisher;

    @Autowired
    private DictionaryRepository repository;

    @Test
    void messageFromQueueIsSavedToDatabase() {
        DictionaryMessage message = new DictionaryMessage();
        message.setType("STATUS");
        message.setCode("ACTIVE");
        message.setName("Активен");
        message.setPayload(Map.of("order", 1));

        publisher.publish(message);

        await().untilAsserted(() -> {
            assertThat(repository.findByTypeAndCode("STATUS", "ACTIVE"))
                    .isPresent()
                    .get()
                    .extracting(DictionaryEntity::getName)
                    .isEqualTo("Активен");
        });
    }
}
