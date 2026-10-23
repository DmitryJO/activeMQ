package rt.vt.activemq.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Service
@Profile("!amq-only")
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryRepository repository;
    private final JsonMapper jsonMapper;
    private final ExternalDictionaryClient externalClient;

    @Transactional
    public DictionaryEntity upsert(DictionaryMessage message) {
        DictionaryEntity entity = repository.findByTypeAndCode(message.getType(), message.getCode())
                .orElseGet(DictionaryEntity::new);
        entity.setType(message.getType());
        entity.setCode(message.getCode());
        entity.setName(message.getName());
        entity.setPayload(writePayload(message));
        DictionaryEntity saved = repository.save(entity);
        log.info("Saved dictionary {}/{} id={}", saved.getType(), saved.getCode(), saved.getId());
        externalClient.send(message);
        return saved;
    }

    public List<DictionaryEntity> findAll() {
        return repository.findAll();
    }

    public List<DictionaryEntity> findByType(String type) {
        return repository.findByTypeIgnoreCase(type);
    }

    private String writePayload(DictionaryMessage message) {
        if (message.getPayload() == null || message.getPayload().isEmpty()) {
            return null;
        }
        return jsonMapper.writeValueAsString(message.getPayload());
    }
}
