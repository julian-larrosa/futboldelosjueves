package com.fdlj.fdlj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FdljBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdljBackendApplication.class, args);
	}

}
