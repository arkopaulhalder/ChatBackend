package com.ChatSystem.UserService.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchUsersResponse {

    private List<UserProfileResponse> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
