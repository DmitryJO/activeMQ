package rt.vt.activemq.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import rt.vt.activemq.config.AppProperties;

@Slf4j
@Component
@Profile("!amq-only")
public class ExternalDictionaryClient {

    private final AppProperties properties;
    private final RestClient restClient = RestClient.create();

    public ExternalDictionaryClient(AppProperties properties) {
        this.properties = properties;
    }

    public void send(DictionaryMessage message) {
        String url = properties.getExternalApi().getUrl();
        if (!StringUtils.hasText(url)) {
            return;
        }
        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Sent dictionary {}/{} to external API {}", message.getType(), message.getCode(), url);
        } catch (Exception ex) {
            log.warn("External API call failed for {}/{}: {}", message.getType(), message.getCode(), ex.getMessage());
        }
    }
}
