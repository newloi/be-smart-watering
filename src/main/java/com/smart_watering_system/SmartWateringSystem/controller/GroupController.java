package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.GroupRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupDetailResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupResponse;
import com.smart_watering_system.SmartWateringSystem.service.GroupService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupController {

    GroupService groupService;
    UserService userService;

    @PostMapping
    ApiResponse<GroupDetailResponse> createGroup(@RequestHeader("Authorization") String headerAuth,
                                                 @RequestBody GroupRequest request) {
        return ApiResponse.<GroupDetailResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(groupService.create(request, userService.getUser(headerAuth)))
                .build();
    }

    @GetMapping
    ApiResponse<List<GroupResponse>> getAllGroups(@RequestHeader("Authorization") String headerAuth) {
        return ApiResponse.<List<GroupResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(groupService.getAll(userService.getUser(headerAuth)))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<GroupDetailResponse> getGroup(@RequestHeader("Authorization") String headerAuth,
                                              @PathVariable("id") String id) {
        return ApiResponse.<GroupDetailResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(groupService.get(id, userService.getUser(headerAuth)))
                .build();
    }

}
