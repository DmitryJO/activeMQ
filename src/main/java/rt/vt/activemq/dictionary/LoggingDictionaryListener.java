package rt.vt.activemq.dictionary;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageFormatException;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Profile("amq-only")
public class LoggingDictionaryListener {

    @JmsListener(destination = "${app.jms.dictionary-queue}")
    public void onMessage(Message message) throws JMSException {
        String body;
        if (message instanceof TextMessage textMessage) {
            body = textMessage.getText();
        } else if (message instanceof BytesMessage bytesMessage) {
            body = new String(bytesMessage.getBody(byte[].class), StandardCharsets.UTF_8);
        } else {
            throw new MessageFormatException("Expected TextMessage or BytesMessage, got "
                    + message.getClass().getSimpleName());
        }
        log.info("Received ActiveMQ message (amq-only): {}", body);
    }
}
