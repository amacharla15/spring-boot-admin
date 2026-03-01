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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.test.web.reactive.server.WebTestClient;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import static org.springframework.http.HttpMethod.POST;

class InstancesRegistrationSecurityIntegrationTest {

	private static ConfigurableApplicationContext instance;

	private static int port;

	private static WebTestClient client;

	@BeforeAll
	static void setUp() {
		instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
			.web(WebApplicationType.SERVLET)
			.run("--server.port=0",
					"--spring.autoconfigure.exclude=" + DataSourceAutoConfiguration.class.getName() + ","
							+ SessionAutoConfiguration.class.getName() + ","
							+ "org.springframework.boot.autoconfigure.session.jdbc.JdbcSessionAutoConfiguration",
					"--spring.session.store-type=none");

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
		String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	static class TestAdminApplication {

		@Bean
		SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			http.authorizeHttpRequests((authz) -> authz.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults())
				.csrf((csrf) -> csrf
					.ignoringRequestMatchers(PathPatternRequestMatcher.withDefaults().matcher(POST, "/instances")));
			return http.build();
		}

		@Bean
		InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
			return new InMemoryUserDetailsManager(
					User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build());
		}

		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}

	}

}
