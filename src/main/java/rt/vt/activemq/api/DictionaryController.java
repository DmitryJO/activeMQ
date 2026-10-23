package rt.vt.activemq.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rt.vt.activemq.dictionary.DictionaryEntity;
import rt.vt.activemq.dictionary.DictionaryService;

import java.util.List;

@RestController
@Profile("!amq-only")
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping
    public List<DictionaryEntity> findAll() {
        return dictionaryService.findAll();
    }

    @GetMapping("/{type}")
    public List<DictionaryEntity> findByType(@PathVariable String type) {
        return dictionaryService.findByType(type);
    }
}
