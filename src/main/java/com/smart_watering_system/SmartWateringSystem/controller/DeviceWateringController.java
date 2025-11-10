package com.smart_watering_system.SmartWateringSystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceWateringService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices/{id}/watering")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceWateringController {

    DeviceWateringService deviceWateringService;

    @PostMapping
    ApiResponse<WateringResponse> doAction(@RequestBody @Valid WateringRequest request,
                                           @PathVariable("id") String id) throws MqttException, JsonProcessingException {
        return ApiResponse.<WateringResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceWateringService.doAction(id, request,false, null))
                .build();
    }

    @GetMapping("/history")
    ApiResponse<List<WateringResponse>> getAllHistories(@PathVariable("id") String id,
                                                        @PageableDefault(
                                                                size = 10, sort = "startTime", direction = Sort.Direction.DESC
                                                        )Pageable pageable) {
        return ApiResponse.<List<WateringResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .quantity(deviceWateringService.getQuantity(id))
                .data(deviceWateringService.getAllHistories(id, pageable))
                .build();
    }

}
