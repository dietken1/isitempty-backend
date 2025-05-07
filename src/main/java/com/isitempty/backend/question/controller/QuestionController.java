package com.isitempty.backend.question.controller;

import com.isitempty.backend.question.dto.request.QuestionReq;
import com.isitempty.backend.question.entity.Question;
import com.isitempty.backend.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionReq req) {
        Question question = questionService.createQuestion(
            req.getName(),
            req.getEmail(),
            req.getMessage()
        );
        return ResponseEntity.ok(question);
    }

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }
} 