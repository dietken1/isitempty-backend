package com.isitempty.backend.oauthlogin.api.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateForm {
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;
}
