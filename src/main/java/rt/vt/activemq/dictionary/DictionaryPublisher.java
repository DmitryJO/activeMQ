package rt.vt.activemq.dictionary;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import rt.vt.activemq.config.AppProperties;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class DictionaryPublisher {

    private final JmsTemplate jmsTemplate;
    private final JsonMapper jsonMapper;
    private final AppProperties properties;

    public void publish(DictionaryMessage message) {
        String json = jsonMapper.writeValueAsString(message);
        jmsTemplate.convertAndSend(properties.getJms().getDictionaryQueue(), json);
    }
}
