package com.isitempty.backend.oauthlogin.api.form;

import com.isitempty.backend.oauthlogin.oauth.entity.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String roleType;

    public UserResponse(String userId, String username, String email, String roleType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleType = roleType;
    }
}
