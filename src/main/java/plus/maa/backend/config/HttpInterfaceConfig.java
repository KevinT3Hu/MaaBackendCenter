package plus.maa.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import plus.maa.backend.repository.GithubRepository;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    GithubRepository githubRepository() {

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();

        WebClient client = WebClient.builder()
                .baseUrl("https://api.github.com")
                .exchangeStrategies(ExchangeStrategies
                        .builder()
                        .codecs(codecs -> {
                            codecs.defaultCodecs()
                                    .jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                            codecs.defaultCodecs()
                                    .jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                            // 最大 20MB
                            codecs.defaultCodecs().maxInMemorySize(20 * 1024 * 1024);
                        })
                        .build())
                .defaultHeaders(headers -> {
                    headers.add("Accept", "application/vnd.github+json");
                    headers.add("X-GitHub-Api-Version", "2022-11-28");
                })
                .build();
        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client))
                .build()
                .createClient(GithubRepository.class);
    }

}
