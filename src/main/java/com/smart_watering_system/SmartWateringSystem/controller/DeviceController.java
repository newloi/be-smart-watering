package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/devices")
public class DeviceController {

    DeviceService deviceService;
    UserService userService;

    @PostMapping
    ApiResponse<DeviceResponse> createDevice(@RequestHeader("Authorization") String headerAuthorizaion,
                                             @RequestBody @Valid DeviceRequest request) {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(deviceService.create(request, userService.getUser(headerAuthorizaion)))
                .build();
    }

    @GetMapping
    ApiResponse<List<DeviceResponse>> getAllDevices(@RequestHeader("Authorization") String headerAuthorizaion){
        return ApiResponse.<List<DeviceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.getAll(userService.getUser(headerAuthorizaion)))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<DeviceResponse> getDevice(@RequestHeader("Authorization") String headerAuthorizaion,
                                          @PathVariable("id") String id) {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.get(id, userService.getUser(headerAuthorizaion)))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteDevice(@RequestHeader("Authorization") String headerAuthorizaion,
                                   @PathVariable("id") String id) {
        deviceService.delete(id, userService.getUser(headerAuthorizaion));

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<DeviceResponse> updateDevice(@RequestHeader("Authorization") String headerAuthorizaion,
                                             @PathVariable("id") String id,
                                             @RequestBody @Valid DeviceRequest request) {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.update(id, request, userService.getUser(headerAuthorizaion)))
                .build();
    }

}
