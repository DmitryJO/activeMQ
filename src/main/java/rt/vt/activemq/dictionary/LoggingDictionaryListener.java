package rt.vt.activemq.dictionary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("amq-only")
public class LoggingDictionaryListener {

    @JmsListener(destination = "${app.jms.dictionary-queue}")
    public void onMessage(String body) {
        log.info("Received ActiveMQ message (amq-only): {}", body);
    }
}
