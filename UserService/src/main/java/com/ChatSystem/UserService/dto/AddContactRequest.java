package com.ChatSystem.UserService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddContactRequest {

    @NotBlank(message = "Contact user UUID is required")
    private String contactUserUuid;

    @Size(max = 100, message = "Contact name must be under 100 characters")
    private String contactName;   // optional custom label
}
