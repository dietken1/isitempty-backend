package com.isitempty.backend.oauthlogin.api.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String userId;
    private String username;
    private String email;

    public UserResponse(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
