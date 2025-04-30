package com.isitempty.backend.camera.repository;

import com.isitempty.backend.camera.entity.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<Camera, Long> {
}
