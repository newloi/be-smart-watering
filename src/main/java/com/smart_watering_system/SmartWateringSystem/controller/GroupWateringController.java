package com.smart_watering_system.SmartWateringSystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smart_watering_system.SmartWateringSystem.dto.request.WateringRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceWateringService;
import com.smart_watering_system.SmartWateringSystem.service.GroupWateringService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{id}/watering")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupWateringController {

    GroupWateringService groupWateringService;
    UserService userService;

    @PostMapping
    ApiResponse<Void> doAction(@RequestHeader("Authorization") String headerAuth,
                                           @RequestBody WateringRequest request,
                                           @PathVariable("id") String id) throws MqttException, JsonProcessingException {
        groupWateringService.doAction(id, request, userService.getUser(headerAuth));

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

}
