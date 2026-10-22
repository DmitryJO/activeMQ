package rt.vt.activemq.dictionary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {

    Optional<DictionaryEntity> findByTypeAndCode(String type, String code);

    List<DictionaryEntity> findByTypeIgnoreCase(String type);
}
