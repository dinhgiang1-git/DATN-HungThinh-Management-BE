package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected <T>ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
}
