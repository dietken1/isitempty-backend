package com.isitempty.backend.toilet.controller;

import com.isitempty.backend.toilet.dto.request.NearbyToiletReq;
import com.isitempty.backend.toilet.entity.Toilet;
import com.isitempty.backend.toilet.service.ToiletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/toilet")
public class ToiletController {

    private final ToiletService toiletService;

    @PostMapping("/nearby")
    public ResponseEntity<List<Toilet>> getNearbyToilets(@RequestBody NearbyToiletReq req) {
        List<Toilet> toilets = toiletService.getNearbyToilets(req.getLatitude(), req.getLongitude());
        if (toilets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(toilets);
    }
}
