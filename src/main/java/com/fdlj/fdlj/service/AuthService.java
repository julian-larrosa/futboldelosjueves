package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.ChangePasswordRequest;
import com.fdlj.fdlj.dto.request.ForgotPasswordRequest;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterHinchaRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.request.ResetPasswordRequest;
import com.fdlj.fdlj.dto.response.AuthResponse;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse registerHincha(RegisterHinchaRequest request);

	AuthResponse login(LoginRequest request);

	void changePassword(String email, ChangePasswordRequest request);

	void resetPassword(ResetPasswordRequest request);

	void forgotPassword(ForgotPasswordRequest request);
}
