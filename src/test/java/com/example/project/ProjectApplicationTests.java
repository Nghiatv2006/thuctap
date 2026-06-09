package com.example.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProjectApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testBcrypt() {
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
		System.out.println("GEN admin123: " + encoder.encode("admin123"));
		System.out.println("GEN manager123: " + encoder.encode("manager123"));
		System.out.println("GEN staff123: " + encoder.encode("staff123"));
	}

}

