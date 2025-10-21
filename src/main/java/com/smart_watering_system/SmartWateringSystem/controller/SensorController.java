package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.DataSensorResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.WateringResponse;
import com.smart_watering_system.SmartWateringSystem.service.SensorService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices/{id}/sensor")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SensorController {

    SensorService sensorService;
    UserService userService;

    @GetMapping("/history")
    ApiResponse<List<DataSensorResponse>> getAllHistories(@RequestHeader("Authorization") String headerAuth,
                                                          @PathVariable String id,
                                                          @PageableDefault(size = 12, sort = "timestamp", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.<List<DataSensorResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(sensorService.getHistory(id, userService.getUser(headerAuth), pageable))
                .build();
    }

}
