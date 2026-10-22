package rt.vt.activemq.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rt.vt.activemq.dictionary.DictionaryEntity;
import rt.vt.activemq.dictionary.DictionaryMessage;
import rt.vt.activemq.dictionary.DictionaryPublisher;
import rt.vt.activemq.dictionary.DictionaryService;

import java.util.List;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryPublisher publisher;
    private final DictionaryService dictionaryService;

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publish(@Valid @RequestBody DictionaryMessage message) {
        publisher.publish(message);
    }

    @GetMapping
    public List<DictionaryEntity> findAll() {
        return dictionaryService.findAll();
    }

    @GetMapping("/{type}")
    public List<DictionaryEntity> findByType(@PathVariable String type) {
        return dictionaryService.findByType(type);
    }
}
