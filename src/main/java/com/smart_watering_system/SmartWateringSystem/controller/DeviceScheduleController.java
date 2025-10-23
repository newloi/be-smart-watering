package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.ScheduleRequest;
import com.smart_watering_system.SmartWateringSystem.dto.request.TriggerRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.ScheduleResponse;
import com.smart_watering_system.SmartWateringSystem.service.DeviceSchedulerService;
import jakarta.validation.Valid;
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
@RequestMapping("/devices/{id}/schedule")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceScheduleController {

    DeviceSchedulerService deviceSchedulerService;

    @PostMapping
    public ApiResponse<ScheduleResponse> createSchedule(@RequestHeader("Authorization") String authHeader,
                                                        @RequestBody @Valid ScheduleRequest request,
                                                        @PathVariable("id") String id) {
        return ApiResponse.<ScheduleResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceSchedulerService.create(id, request, authHeader))
                .build();
    }

    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable("id") String id,
                                            @PathVariable("scheduleId") String scheduleId) {
        deviceSchedulerService.deleteSchedule(authHeader, id, scheduleId);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> updateSchedule(@RequestHeader("Authorization") String authHeader,
                                                        @RequestBody @Valid ScheduleRequest request,
                                                        @PathVariable("id") String id,
                                                        @PathVariable("scheduleId") String scheduleId) {
        return ApiResponse.<ScheduleResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceSchedulerService.update(authHeader, id, scheduleId, request))
                .build();
    }

    @PostMapping("/{scheduleId}/trigger")
    public ApiResponse<Void> triggerSchedule(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody @Valid TriggerRequest request,
                                             @PathVariable("id") String id,
                                             @PathVariable("scheduleId") String scheduleId) {
        deviceSchedulerService.trigger(authHeader, id, scheduleId, request);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @GetMapping
    public ApiResponse<List<ScheduleResponse>> getAllSchedule(@RequestHeader("Authorization") String authHeader,
                                                              @PathVariable("id") String id,
                                                              @PageableDefault(sort = "createdAt",
                                                                      direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<List<ScheduleResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(deviceSchedulerService.getAll(id, authHeader, pageable))
                .build();
    }

}
