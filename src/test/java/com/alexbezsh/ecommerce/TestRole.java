package com.alexbezsh.ecommerce;

import org.springframework.security.core.GrantedAuthority;

public enum TestRole implements GrantedAuthority {
    USER,
    TEST;

    @Override
    public String getAuthority() {
        return name();
    }

}
