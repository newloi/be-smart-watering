package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.service.SchedulerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices/{id}/schedule")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleController {

    SchedulerService schedulerService;

    @PostMapping
    public ApiResponse<ScheduleResponse> createSchedule(@RequestHeader("Authorization") String authHeader,
                                                        @RequestBody ScheduleRequest request,
                                                        @PathVariable("id") String id) {
        return ApiResponse.<ScheduleResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(schedulerService.create(id, request, authHeader))
                .build();
    }

}
