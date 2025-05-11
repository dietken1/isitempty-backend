package com.isitempty.backend.favorite.controller;

import com.isitempty.backend.favorite.dto.request.FavoriteReq;
import com.isitempty.backend.favorite.service.FavoriteService;
import com.isitempty.backend.review.dto.response.ReviewRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 찜 추가
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteReq favoriteReq, Principal principal) {
        return favoriteService.addFavorite(principal.getName(), favoriteReq.getParkingLotId());
    }

    // 유저별 찜 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFavoritesByUserId(@PathVariable String userId) {
        return favoriteService.getFavoritesByUserId(userId);
    }
    // 찜 삭제!
    @DeleteMapping
    public ResponseEntity<?> deleteFavorite(@RequestParam String parkingLotId, Principal principal) {
        return favoriteService.deleteFavorite(principal.getName(), parkingLotId);
    }
}
