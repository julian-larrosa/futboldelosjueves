package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ErrorDispatchSecurityTest extends IntegrationTestBase {

	@Test
	void errorDispatch_withoutAuthentication_isNotMaskedAs401() throws Exception {
		var result = mockMvc.perform(get("/error").with(request -> {
					request.setDispatcherType(DispatcherType.ERROR);
					request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
					return request;
				}))
				.andReturn();

		assertNotEquals(401, result.getResponse().getStatus(),
				"Un error interno no debe transformarse en 401: el frontend lo interpreta como sesion vencida y desloguea al usuario");
	}
}
