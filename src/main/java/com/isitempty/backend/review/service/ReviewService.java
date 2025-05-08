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

    public ResponseEntity<?> createReview(String username, String parkingLotId, String content, int rating) {
        //boolean exists = reviewRepo.existsByUserUsernameAndParkingLotId(username, parkingLotId);
        //if (exists) {
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    //.body(Map.of("message", "이미 이 주차장에 리뷰를 작성하셨습니다."));
        //}

        // 리뷰 등록
        User user = userRepo.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ParkingLot lot = (ParkingLot) parkingRepo.findById(parkingLotId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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

    // 리뷰 삭제
    public ResponseEntity<?> deleteReview(String id, String username)
    {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!review.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "리뷰는 본인만 삭제 가능합니다."));
        }
        reviewRepo.deleteById(id);

        ParkingLot lot = review.getParkingLot();
        lot.setReviewCount(Math.max(0, lot.getReviewCount() - 1));
        parkingRepo.save(lot);

        return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
    }
    // 리뷰 업데이트
    public ResponseEntity<?> updateReview(String parkingLotId, String content, int rating, String username )
    {      Review review = (Review) reviewRepo.findById(parkingLotId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        if (!review.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "리뷰는 본인만 수정 가능합니다."));
        }
        review.setContent(content);
        review.setRating(rating);
        reviewRepo.save(review);
        return ResponseEntity.ok(Map.of("message", "리뷰가 수정되었습니다."));
    }
}