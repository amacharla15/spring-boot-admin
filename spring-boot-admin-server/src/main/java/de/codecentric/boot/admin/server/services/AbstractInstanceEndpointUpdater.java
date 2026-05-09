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

import java.util.logging.Level;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

public abstract class AbstractInstanceEndpointUpdater<T> {

	private final InstanceRepository repository;

	private final InstanceWebClient instanceWebClient;

	private final ApiMediaTypeHandler apiMediaTypeHandler;

	protected AbstractInstanceEndpointUpdater(InstanceRepository repository, InstanceWebClient instanceWebClient,
			ApiMediaTypeHandler apiMediaTypeHandler) {
		this.repository = repository;
		this.instanceWebClient = instanceWebClient;
		this.apiMediaTypeHandler = apiMediaTypeHandler;
	}

	protected Mono<Void> update(InstanceId id) {
		return this.repository.computeIfPresent(id, (key, instance) -> doUpdate(instance)).then();
	}

	protected Mono<Instance> doUpdate(Instance instance) {
		if (!shouldUpdate(instance)) {
			return Mono.empty();
		}

		logUpdate(instance);

		Mono<T> response = this.instanceWebClient.instance(instance)
			.get()
			.uri(getEndpoint())
			.exchangeToMono((clientResponse) -> convertResponse(instance, clientResponse))
			.log(getLoggerName(), Level.FINEST);

		return applyRequestOptions(response).doOnError((ex) -> logError(instance, ex))
			.onErrorResume((ex) -> handleError(instance, ex))
			.map((value) -> applyUpdate(instance, value));
	}

	protected ApiMediaTypeHandler getApiMediaTypeHandler() {
		return this.apiMediaTypeHandler;
	}

	protected Mono<T> applyRequestOptions(Mono<T> response) {
		return response;
	}

	protected void logError(Instance instance, Throwable ex) {
	}

	protected abstract boolean shouldUpdate(Instance instance);

	protected abstract void logUpdate(Instance instance);

	protected abstract String getEndpoint();

	protected abstract Mono<T> convertResponse(Instance instance, ClientResponse response);

	protected abstract Mono<T> handleError(Instance instance, Throwable ex);

	protected abstract Instance applyUpdate(Instance instance, T value);

	protected abstract String getLoggerName();

}
