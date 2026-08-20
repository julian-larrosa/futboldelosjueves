package com.fdlj.fdlj.config;

import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.username:admin}")
	private String adminUsername;

	@Value("${app.admin.email:admin@fdlj.com}")
	private String adminEmail;

	@Value("${app.admin.password:admin123}")
	private String adminPassword;

	@Override
	public void run(String... args) {
		boolean adminExists = userRepository.findAll().stream()
				.anyMatch(user -> user.getRole() == Role.ADMIN);
		if (adminExists) {
			return;
		}
		User admin = new User();
		admin.setUsername(adminUsername);
		admin.setEmail(adminEmail);
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(Role.ADMIN);
		admin.setMustChangePassword(false);
		userRepository.save(admin);
	}
}
