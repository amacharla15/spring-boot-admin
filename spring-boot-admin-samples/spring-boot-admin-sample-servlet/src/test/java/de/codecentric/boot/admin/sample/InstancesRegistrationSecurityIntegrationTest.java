/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.sample;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class InstancesRegistrationSecurityIntegrationTest {

	private static ConfigurableApplicationContext instance;

	private static int port;

	private static WebTestClient client;

	@BeforeAll
	static void setUp() {
		instance = new SpringApplicationBuilder().sources(SpringBootAdminServletApplication.class)
			.web(WebApplicationType.SERVLET)
			.profiles("secure")
			.run("--server.port=0");

		port = instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);

		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@AfterAll
	static void tearDown() {
		if (instance != null) {
			instance.close();
		}
	}

	@Test
	void should_reject_registration_without_credentials() {
		client.post()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRegistrationBody())
			.exchange()
			.expectStatus()
			.isUnauthorized();
	}

	@Test
	void should_allow_registration_with_valid_credentials() {
		client.post()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, basicAuth("user", "password"))
			.bodyValue(validRegistrationBody())
			.exchange()
			.expectStatus()
			.isCreated();
	}

	private static String validRegistrationBody() {
		String healthUrl = "http://localhost:" + port + "/actuator/health";
		String serviceUrl = "http://localhost:" + port + "/";
		return "{ \"name\": \"test\", \"healthUrl\": \"" + healthUrl + "\", \"serviceUrl\": \"" + serviceUrl + "\" }";
	}

	private static String basicAuth(String username, String password) {
		String token = java.util.Base64.getEncoder()
			.encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
		return "Basic " + token;
	}

}
