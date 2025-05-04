package com.isitempty.backend.toilet.repository;

import com.isitempty.backend.toilet.entity.Toilet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToiletRepository extends JpaRepository<Toilet, Long> {
    @Query(value = "SELECT * FROM toilets p WHERE " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(?2)) + sin(radians(?1)) * sin(radians(p.lat)))) < ?3",
            nativeQuery = true)
    List<Toilet> findNearbyToilets(double latitude, double longitude, float radius);
}
