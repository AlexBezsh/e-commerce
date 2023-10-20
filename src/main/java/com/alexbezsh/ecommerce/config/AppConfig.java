package com.alexbezsh.ecommerce.config;

import com.alexbezsh.ecommerce.properties.PayPalProperties;
import com.paypal.base.rest.APIContext;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public APIContext apiContext(PayPalProperties properties) {
        return new APIContext(properties.getClientId(),
            properties.getClientSecret(), properties.getMode());
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
