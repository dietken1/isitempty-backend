package com.isitempty.backend.toilet.service;

import com.isitempty.backend.toilet.entity.Toilet;
import com.isitempty.backend.toilet.repository.ToiletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToiletService {

    private final ToiletRepository toiletRepository;

    public List<Toilet> getNearbyToilets(double latitude, double longitude) {
        float radius = 2.0f;
        return toiletRepository.findNearbyToilets(latitude, longitude, radius);
    }
}
