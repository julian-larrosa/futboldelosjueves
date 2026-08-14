package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.response.AuthResponse;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);
}
