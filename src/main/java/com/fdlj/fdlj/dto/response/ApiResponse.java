package com.fdlj.fdlj.dto.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
		boolean success,
		int status,
		String message,
		T data
) {

	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(true, HttpStatus.CREATED.value(), "Recurso creado exitosamente", data);
	}

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, HttpStatus.OK.value(), "OK", data);
	}
}
