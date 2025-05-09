package com.isitempty.backend.review.service;


import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.repository.user.UserRepository;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import com.isitempty.backend.review.entity.Review;
import com.isitempty.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ParkingLotRepository parkingRepo;
    private final UserRepository userRepo;

    // 주차장별 평균 리뷰 점수 조회
    public Double getAverageRatingByParkingLotId(String parkingLotId) {
        return reviewRepo.getAverageRatingByParkingLotId(parkingLotId);
    }

    public ResponseEntity<?> createReview(String userIdentifier, String parkingLotId, String content, int rating) {
        //boolean exists = reviewRepo.existsByUserUsernameAndParkingLotId(username, parkingLotId);
        //if (exists) {
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    //.body(Map.of("message", "이미 이 주차장에 리뷰를 작성하셨습니다."));
        //}

        // 리뷰 등록 - 토큰의 값(userId)으로 사용자 찾기
        User user = userRepo.findByUserId(userIdentifier);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        
        ParkingLot lot = parkingRepo.findById(parkingLotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주차장을 찾을 수 없습니다."));

        Review review = new Review();
        review.setUser(user);
        review.setParkingLot(lot);
        review.setContent(content);
        review.setRating(rating);
        reviewRepo.save(review);

        lot.setReviewCount(lot.getReviewCount() + 1);
        parkingRepo.save(lot);

        return ResponseEntity.ok(Map.of("message", "리뷰가 등록되었습니다."));
    }
    
    // 주차장 별 리뷰 조회
    public ResponseEntity<?> getReviewsByParkingLot(String parkingLotId) {
        List<Review> reviews = reviewRepo.findByParkingLot_Id(parkingLotId);
        if (reviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "리뷰가 없습니다."));
        }
        return ResponseEntity.ok(reviews);
    }

    // 유저 별 리뷰 조회
    public ResponseEntity<?> getReviewsByUser(String username) {
        List<Review> reviews = reviewRepo.findByUserUsername(username);
        if (reviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "리뷰가 없습니다."));
        }
        return ResponseEntity.ok(reviews);
    }

    // 유저 ID별 리뷰 조회
    public ResponseEntity<?> getReviewsByUserId(String userId) {
        List<Review> reviews = reviewRepo.findByUserUserId(userId);
        if (reviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "리뷰가 없습니다."));
        }
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 삭제
    public ResponseEntity<?> deleteReview(Long id, String userIdentifier) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        
        // 토큰의 사용자 ID와 리뷰 작성자의 userId 비교
        if (!review.getUser().getUserId().equals(userIdentifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "리뷰는 본인만 삭제 가능합니다."));
        }
        
        reviewRepo.deleteById(id);

        ParkingLot lot = review.getParkingLot();
        lot.setReviewCount(Math.max(0, lot.getReviewCount() - 1));
        parkingRepo.save(lot);

        return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
    }
    
    // 리뷰 수정
    public ResponseEntity<?> updateReview(Long id, String content, int rating, String userIdentifier) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        
        // 토큰의 사용자 ID와 리뷰 작성자의 userId 비교
        if (!review.getUser().getUserId().equals(userIdentifier)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "리뷰는 본인만 수정 가능합니다."));
        }
        
        review.setContent(content);
        review.setRating(rating);
        reviewRepo.save(review);
        return ResponseEntity.ok(Map.of("message", "리뷰가 수정되었습니다."));
    }
}