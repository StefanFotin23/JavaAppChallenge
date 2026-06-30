package com.example.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "jwt.secret=defaultSecretKeyForTestingStarWarsAppWhichIsLongEnoughForSha256Signature"
})
class AppApplicationTests {

	@Test
	void contextLoads() {
	}

}
