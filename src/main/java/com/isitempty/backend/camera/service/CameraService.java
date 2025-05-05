package com.isitempty.backend.camera.service;

import com.isitempty.backend.camera.entity.Camera;
import com.isitempty.backend.camera.repository.CameraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CameraService {

    private final CameraRepository cameraRepository;
    
    // 모든 카메라
    public List<Camera> getAllCameras() {
        return cameraRepository.findAll();
    }

    // 반경 2km 카메라
    public List<Camera> getNearbyCameras(double latitude, double longitude) {
        float radius = 2.0f;
        return cameraRepository.findNearbyCameras(latitude, longitude, radius);
    }
}
