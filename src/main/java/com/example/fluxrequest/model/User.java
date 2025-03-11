package com.example.fluxrequest.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private int id;
    private String username;
    private String firstName;
}
