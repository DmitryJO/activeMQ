package rt.vt.activemq.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rt.vt.activemq.dictionary.DictionaryMessage;
import rt.vt.activemq.dictionary.DictionaryPublisher;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryPublishController {

    private final DictionaryPublisher publisher;

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void publish(@Valid @RequestBody DictionaryMessage message) {
        publisher.publish(message);
    }
}
