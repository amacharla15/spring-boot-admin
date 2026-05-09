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

package de.codecentric.boot.admin.server.web;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class HttpHeaderFilterTest {

	@Test
	void should_filter_hop_by_hop_headers_and_configured_ignored_headers() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.HOST, "example.com");
		headers.add(HttpHeaders.CONNECTION, "keep-alive");
		headers.add("X-Application-Context", "test");
		headers.add("X-Ignored", "secret");
		headers.add("X-Kept", "value");

		HttpHeaderFilter filter = new HttpHeaderFilter(Collections.singleton("X-Ignored"));
		HttpHeaders filtered = filter.filterHeaders(headers);

		assertThat(filtered).doesNotContainKey(HttpHeaders.HOST);
		assertThat(filtered).doesNotContainKey(HttpHeaders.CONNECTION);
		assertThat(filtered).doesNotContainKey("X-Application-Context");
		assertThat(filtered).doesNotContainKey("X-Ignored");
		assertThat(filtered).containsKey("X-Kept");
	}

	@Test
	void should_delegate_header_decision_to_strategy() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Allow", "1");
		headers.add("X-Deny", "2");

		HeaderFilterStrategy strategy = (header) -> !"X-Deny".equalsIgnoreCase(header);
		HttpHeaderFilter filter = new HttpHeaderFilter(strategy);
		HttpHeaders filtered = filter.filterHeaders(headers);

		assertThat(filtered).containsKey("X-Allow");
		assertThat(filtered).doesNotContainKey("X-Deny");
	}

}
