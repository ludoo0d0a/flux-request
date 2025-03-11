package com.example.fluxrequest.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private List<User> content;
}
