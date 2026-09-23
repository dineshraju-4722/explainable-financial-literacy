package com.financialliteracy.service;

import com.financialliteracy.dto.user.CreateUserRequest;
import com.financialliteracy.dto.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
}
