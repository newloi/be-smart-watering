package com.smart_watering_system.SmartWateringSystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceWateringService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices/{id}/watering")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceWateringController {

    DeviceWateringService deviceWateringService;
    UserService userService;

    @PostMapping
    ApiResponse<WateringResponse> doAction(@RequestHeader("Authorization") String headerAuth,
                                           @RequestBody WateringRequest request,
                                           @PathVariable("id") String id) throws MqttException, JsonProcessingException {
        return ApiResponse.<WateringResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceWateringService.doAction(id, request, userService.getUser(headerAuth), false))
                .build();
    }

    @GetMapping("/history")
    ApiResponse<List<WateringResponse>> getAllHistories(@RequestHeader("Authorization") String headerAuth,
                                                        @PathVariable("id") String id) {
        return ApiResponse.<List<WateringResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceWateringService.getAllHistories(id, userService.getUser(headerAuth)))
                .build();
    }

}
