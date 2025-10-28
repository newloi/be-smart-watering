package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.annotation.RateLimiter;
import com.smart_watering_system.SmartWateringSystem.dto.request.DeviceRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @RateLimiter(capacity = 10, refillTokens = 10, refillPeriodSeconds = 60)
    ApiResponse<DeviceResponse> createDevice(@RequestBody @Valid DeviceRequest request) {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(deviceService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<DeviceResponse>> getAllDevices(@PageableDefault(sort = "createdAt",
                                                            direction = Sort.Direction.DESC) Pageable pageable) throws MqttException {
        return ApiResponse.<List<DeviceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.getAll(pageable))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<DeviceResponse> getDevice(@PathVariable("id") String id) throws MqttException {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.get(id, userService.getUser()))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteDevice(@PathVariable("id") String id) throws MqttException {
        deviceService.delete(id, userService.getUser());

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<DeviceResponse> updateDevice(@PathVariable("id") String id,
                                             @RequestBody @Valid DeviceRequest request) throws MqttException {
        return ApiResponse.<DeviceResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.update(id, request, userService.getUser()))
                .build();
    }

    @GetMapping("/free")
    ApiResponse<List<DeviceResponse>> getAllFreeDevice(@PageableDefault(sort = "createdAt",
                                                               direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<List<DeviceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.getAllFree(pageable))
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<DeviceResponse>> searchDeviceByName(@RequestParam("name") String keyword,
                                                         @PageableDefault(sort = "createdAt",
                                                                 direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<List<DeviceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceService.searchByKeyword(keyword, pageable))
                .build();
    }

}
