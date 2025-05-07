package com.isitempty.backend.question.service;

import com.isitempty.backend.question.entity.Question;
import com.isitempty.backend.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional
    public Question createQuestion(String name, String email, String message) {
        Question question = new Question();
        question.setName(name);
        question.setEmail(email);
        question.setMessage(message);
        return questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
} 