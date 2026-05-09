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

package de.codecentric.boot.admin.server.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application
 * querying the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class InfoUpdater extends AbstractInstanceEndpointUpdater<Info> {

	private static final Logger log = LoggerFactory.getLogger(InfoUpdater.class);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	public InfoUpdater(InstanceRepository repository, InstanceWebClient instanceWebClient,
			ApiMediaTypeHandler apiMediaTypeHandler) {
		super(repository, instanceWebClient, apiMediaTypeHandler);
	}

	public Mono<Void> updateInfo(InstanceId id) {
		return update(id);
	}

	@Override
	protected boolean shouldUpdate(Instance instance) {
		return !instance.getStatusInfo().isOffline() && !instance.getStatusInfo().isUnknown()
				&& instance.getEndpoints().isPresent(Endpoint.INFO);
	}

	@Override
	protected void logUpdate(Instance instance) {
		log.debug("Update info for {}", instance);
	}

	@Override
	protected String getEndpoint() {
		return Endpoint.INFO;
	}

	@Override
	protected Mono<Info> convertResponse(Instance instance, ClientResponse response) {
		return convertInfo(instance, response);
	}

	@Override
	protected Mono<Info> handleError(Instance instance, Throwable ex) {
		return Mono.just(convertInfo(instance, ex));
	}

	@Override
	protected Instance applyUpdate(Instance instance, Info info) {
		return instance.withInfo(info);
	}

	@Override
	protected String getLoggerName() {
		return log.getName();
	}

	protected Mono<Info> convertInfo(Instance instance, ClientResponse response) {
		if (response.statusCode().is2xxSuccessful() && response.headers()
			.contentType()
			.filter((mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON)
					|| getApiMediaTypeHandler().isApiMediaType(mt))
			.isPresent()) {
			return response.bodyToMono(RESPONSE_TYPE).map(Info::from).defaultIfEmpty(Info.empty());
		}
		log.info("Couldn't retrieve info for {}: {}", instance, response.statusCode());
		return response.releaseBody().then(Mono.just(Info.empty()));
	}

	protected Info convertInfo(Instance instance, Throwable ex) {
		log.warn("Couldn't retrieve info for {}", instance, ex);
		return Info.empty();
	}

}
