package com.mybank.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["app.jwt.secret=$TEST_JWT_SECRET"])
class BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
