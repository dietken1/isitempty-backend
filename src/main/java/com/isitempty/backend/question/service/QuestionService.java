package com.isitempty.backend.question.service;

import com.isitempty.backend.question.entity.Question;
import com.isitempty.backend.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

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
    
    /**
     * 특정 ID의 문의 내용을 조회합니다.
     *
     * @param id 조회할 문의의 ID
     * @return 문의 내용
     * @throws RuntimeException 문의를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다. ID: " + id));
    }
    
    /**
     * 문의 내용을 수정합니다 (관리자 전용).
     *
     * @param id 수정할 문의의 ID
     * @param name 새 이름 (null이면 변경 안함)
     * @param email 새 이메일 (null이면 변경 안함)
     * @param message 새 메시지 (null이면 변경 안함)
     * @return 수정된 문의 내용
     * @throws RuntimeException 문의를 찾을 수 없는 경우
     */
    @Transactional
    public Question updateQuestion(Long id, String name, String email, String message) {
        Question question = getQuestionById(id);
        
        if (name != null && !name.trim().isEmpty()) {
            question.setName(name);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            question.setEmail(email);
        }
        
        if (message != null && !message.trim().isEmpty()) {
            question.setMessage(message);
        }
        
        return questionRepository.save(question);
    }
    
    /**
     * 문의를 삭제합니다 (관리자 전용).
     *
     * @param id 삭제할 문의의 ID
     * @throws RuntimeException 문의를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("문의를 찾을 수 없습니다. ID: " + id);
        }
        questionRepository.deleteById(id);
    }
} 