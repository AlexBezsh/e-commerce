package com.alexbezsh.ecommerce.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    public ErrorResponse(HttpStatus status, String message) {
        this.code = status.value();
        this.status = status.getReasonPhrase();
        this.message = message;
    }

    private int code;
    private String status;
    private String message;

}
