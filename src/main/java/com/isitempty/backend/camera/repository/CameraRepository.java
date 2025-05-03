package com.isitempty.backend.camera.repository;

import com.isitempty.backend.camera.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Long> {
    // 주변 카메라 검색 (위도, 경도, 반경(km) 기준)
    @Query(value = "SELECT * FROM cameras p WHERE " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(?2)) + sin(radians(?1)) * sin(radians(p.lat)))) < ?3",
            nativeQuery = true)
    List<Camera> findNearbyCameras(double latitude, double longitude, float radius);
}
