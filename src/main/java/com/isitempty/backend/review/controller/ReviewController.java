package com.isitempty.backend.review.controller;

import com.isitempty.backend.review.dto.response.ReviewRes;
import com.isitempty.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Principal principal) {
        return reviewService.deleteReview(id, principal.getName());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody ReviewRes dto, Principal principal) {
        return reviewService.updateReview(id, dto.getContent(), dto.getRating(),principal.getName() );
    }
}