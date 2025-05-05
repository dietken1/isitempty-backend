package com.isitempty.backend.review.service;


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

    public ResponseEntity<?> deleteReview(Long id, String username)
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
//
        return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
    }

    public ResponseEntity<?> updateReview(Long id, String content, int rating,String username  )
    {      Review review = reviewRepo.findById(id)
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