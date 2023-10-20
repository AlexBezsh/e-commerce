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
@ToString(exclude = "url")
@Validated
@ConfigurationProperties("mongo")
public class MongoProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String database;

}
