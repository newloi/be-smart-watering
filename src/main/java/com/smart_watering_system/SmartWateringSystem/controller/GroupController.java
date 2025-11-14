package com.smart_watering_system.SmartWateringSystem.controller;

import com.smart_watering_system.SmartWateringSystem.dto.request.GroupRequest;
import com.smart_watering_system.SmartWateringSystem.dto.response.ApiResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.DeviceResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupDetailResponse;
import com.smart_watering_system.SmartWateringSystem.dto.response.GroupResponse;
import com.smart_watering_system.SmartWateringSystem.service.GroupService;
import com.smart_watering_system.SmartWateringSystem.service.UserService;
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
@RequestMapping("/groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupController {

    GroupService groupService;

    @PostMapping
    ApiResponse<GroupDetailResponse> createGroup(@RequestBody @Valid GroupRequest request) {
        return ApiResponse.<GroupDetailResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .data(groupService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<GroupResponse>> getAllGroups(@PageableDefault(sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<List<GroupResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .quantity(groupService.getQuantity())
                .data(groupService.getAll(pageable))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<GroupDetailResponse> getGroup(@PathVariable("id") String id) {
        return ApiResponse.<GroupDetailResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(groupService.get(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<GroupDetailResponse> updateGroup(@PathVariable("id") String id,
                                                 @RequestBody @Valid GroupRequest request) {
        return ApiResponse.<GroupDetailResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .data(groupService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteGroup(@PathVariable("id") String id) {
        groupService.delete(id);

        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @GetMapping("/search")
    ApiResponse<List<GroupResponse>> searchGroupByName(@RequestParam("name") String keyword,
                                                        @PageableDefault(sort = "createdAt",
                                                                direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<List<GroupResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .quantity(groupService.getQuantitySearch(keyword))
                .data(groupService.searchByKeyword(keyword, pageable))
                .build();
    }

    @GetMapping("/quantity")
    ApiResponse<Void> getQuantityGroup() {
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .quantity(groupService.getQuantity())
                .build();
    }

}
