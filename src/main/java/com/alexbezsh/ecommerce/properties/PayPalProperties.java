package com.alexbezsh.ecommerce.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "clientSecret")
@Validated
@ConfigurationProperties("paypal")
public class PayPalProperties {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String mode;

    @NotBlank
    private String cancelUrl;

    @NotBlank
    private String returnUrl;

}
