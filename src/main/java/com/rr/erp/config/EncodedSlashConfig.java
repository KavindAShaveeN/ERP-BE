package com.rr.erp.config;

import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Some asset codes contain a "/" (e.g. Hydraulic Jack sets, "HJ-11/09"). The frontend sends them
 * percent-encoded ("HJ-11%2F09") as a single path segment, but Tomcat rejects an encoded slash in
 * a path by default (400). "passthrough" leaves it encoded, and Spring MVC then decodes it inside
 * the one path variable — so every /{assetCode} endpoint works for these codes. */
@Configuration
public class EncodedSlashConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> encodedSlashCustomizer() {
        return factory -> factory.addConnectorCustomizers(
                connector -> connector.setEncodedSolidusHandling("passthrough"));
    }
}
