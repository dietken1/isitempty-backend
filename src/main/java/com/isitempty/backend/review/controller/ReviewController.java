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
    // 리뷰 등록
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRes reviewRes, Principal principal) {
        // principal.getName()은 로그인된 사용자의 username을 반환
        return reviewService.createReview(principal.getName(), reviewRes.getParkingLotId(), reviewRes.getContent(), reviewRes.getRating());
    }

    // 주차장 별 리뷰 조회
    @GetMapping("/parkingLot/{parkingLotId}")
    public ResponseEntity<?> getReviewsByParkingLot(@PathVariable String parkingLotId) {
        return reviewService.getReviewsByParkingLot(parkingLotId);
    }

    // 유저 별 리뷰 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?>getReviewsByUser(@PathVariable String userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    // 리뷰 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Principal principal) {
        return reviewService.deleteReview(id, principal.getName());
    }

    // 리뷰 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody ReviewRes dto, Principal principal) {
        return reviewService.updateReview(id, dto.getContent(), dto.getRating(),principal.getName() );
    }
}