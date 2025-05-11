package com.isitempty.backend.favorite.repository;

import com.isitempty.backend.favorite.entity.Favorite;
import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndParkingLot(User user, ParkingLot parkingLot); // 유저와 주차장으로 찜 조회

    void deleteByUserAndParkingLot(User user, ParkingLot parkingLot); // 유저와 주차장으로 찜 삭제

    List<Favorite> findAllByUser(User user);  // 반환타입 List로 수정

}
