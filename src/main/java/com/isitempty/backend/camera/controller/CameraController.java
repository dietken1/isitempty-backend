package com.isitempty.backend.camera.controller;

import com.isitempty.backend.camera.dto.request.NearbyCameraReq;
import com.isitempty.backend.camera.entity.Camera;
import com.isitempty.backend.camera.service.CameraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/camera")
public class CameraController {

    private final CameraService cameraService;

    @GetMapping
    public List<Camera> getAllCameras() {
        return cameraService.getAllCameras();
    }

    @PostMapping("/nearby")
    public ResponseEntity<List<Camera>> getNearbyCameras(@RequestBody NearbyCameraReq req) {
        List<Camera> cameras = cameraService.getNearbyCameras(req.getLatitude(), req.getLongitude());
        if (cameras.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(cameras);
    }
}
