package rt.vt.activemq.dictionary;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DictionaryMessage {

    @NotBlank
    private String type;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private Map<String, Object> payload;
}
