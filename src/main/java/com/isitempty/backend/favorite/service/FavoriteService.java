package com.isitempty.backend.favorite.service;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.repository.user.UserRepository;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import com.isitempty.backend.favorite.entity.Favorite;
import com.isitempty.backend.favorite.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;

    // 찜 추가
    public ResponseEntity<?> addFavorite(String userId, String parkingLotId) {
        // 토큰의 값(userId)으로 사용자 찾기
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주차장을 찾을 수 없습니다."));

        // 이미 찜한 주차장인지 확인
        if (favoriteRepository.findByUserAndParkingLot(user, parkingLot).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("이미 찜한 주차장입니다.");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setParkingLot(parkingLot);
        favoriteRepository.save(favorite);

        return ResponseEntity.ok("찜이 추가되었습니다.");
    }

    // 유저별 찜 조회
    public ResponseEntity<?> getFavoritesByUserId(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");
        }

        var favorites = favoriteRepository.findAllByUser(user); // 유저가 찜한 모든 주차장 조회

        return ResponseEntity.ok(favorites);
    }
    //if (favorites.isEmpty()) {
    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("찜한 주차장이 없습니다.");
    //}

    // 찜 삭제
    public ResponseEntity<?> deleteFavorite(String userId, String parkingLotId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.");
        }
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주차장을 찾을 수 없습니다."));

        favoriteRepository.deleteByUserAndParkingLot(user, parkingLot);

        return ResponseEntity.ok("찜이 삭제되었습니다.");
    }


}
