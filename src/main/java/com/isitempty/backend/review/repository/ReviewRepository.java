package com.isitempty.backend.review.repository;

import com.isitempty.backend.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByParkingLot_Id(String parkingLotId);

    boolean existsByUserUsernameAndParkingLotId(String username, String parkingLotId);

    Optional<Review> findById(String id);

    void deleteById(String id);

    List<Review> findByUserUsername(String username);
}