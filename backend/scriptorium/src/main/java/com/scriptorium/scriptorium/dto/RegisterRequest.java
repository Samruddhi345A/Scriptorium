package com.scriptorium.scriptorium.dto;

import lombok.Getter;
import lombok.Setter;

public class RegisterRequest {

    @Setter
    @Getter
    private String email;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String institution;
    @Setter
    @Getter
    private String role;
    @Setter
    @Getter
    private String password;
}
