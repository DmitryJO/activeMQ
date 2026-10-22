package rt.vt.activemq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jms jms = new Jms();
    private ExternalApi externalApi = new ExternalApi();

    @Getter
    @Setter
    public static class Jms {
        private String dictionaryQueue = "dictionaries.queue";
    }

    @Getter
    @Setter
    public static class ExternalApi {
        private String url = "";
    }
}
