package gangdong.diet.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // credentials 허용

        // 허용할 Origin 설정
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5500",
                "http://localhost:5501"
        ));

        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));

        // 허용할 HTTP 헤더 설정
        config.addAllowedHeader("*");

        // 노출할 헤더 설정
        config.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}