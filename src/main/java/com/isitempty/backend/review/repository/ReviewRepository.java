package com.isitempty.backend.review.repository;

import com.isitempty.backend.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByParkingLot_Id(String parkingLotId);

    Optional<Review> findById(Long id);

    void deleteById(Long id);

    List<Review> findByUserUsername(String username);
    
    List<Review> findByUserUserId(String userId);
    
    // 주차장별 평균 리뷰 점수 조회
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.parkingLot.id = :parkingLotId")
    Double getAverageRatingByParkingLotId(String parkingLotId);
}