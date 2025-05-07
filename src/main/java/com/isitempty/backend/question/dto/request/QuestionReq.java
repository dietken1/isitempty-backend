package com.isitempty.backend.question.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionReq {
    private String name;
    private String email;
    private String message;
}