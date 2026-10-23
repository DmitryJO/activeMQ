package rt.vt.activemq.dictionary;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!amq-only")
@RequiredArgsConstructor
public class DictionaryListener {

    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final DictionaryService dictionaryService;

    @JmsListener(destination = "${app.jms.dictionary-queue}")
    public void onMessage(String body) {
        try {
            DictionaryMessage message = jsonMapper.readValue(body, DictionaryMessage.class);
            Set<ConstraintViolation<DictionaryMessage>> violations = validator.validate(message);
            if (!violations.isEmpty()) {
                String details = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining(", "));
                log.warn("Skip invalid dictionary message: {}", details);
                return;
            }
            dictionaryService.upsert(message);
        } catch (Exception ex) {
            log.warn("Failed to process dictionary message: {}", ex.getMessage());
        }
    }
}
