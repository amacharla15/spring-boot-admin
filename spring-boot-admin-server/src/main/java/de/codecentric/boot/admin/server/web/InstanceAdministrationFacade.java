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

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

public class InstanceAdministrationFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceAdministrationFacade.class);

	private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();

	private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
		.map((tick) -> PING);

	private final InstanceRegistry registry;

	private final InstanceEventStore eventStore;

	public InstanceAdministrationFacade(InstanceRegistry registry, InstanceEventStore eventStore) {
		this.registry = registry;
		this.eventStore = eventStore;
	}

	public Mono<ResponseEntity<Map<String, InstanceId>>> register(Registration registration,
			UriComponentsBuilder builder) {
		Registration withSource = Registration.copyOf(registration).source("http-api").build();
		LOGGER.debug("Register instance {}", withSource);
		return this.registry.register(withSource).map((id) -> {
			URI location = builder.replacePath("/instances/{id}").buildAndExpand(id).toUri();
			return ResponseEntity.created(location).body(Collections.singletonMap("id", id));
		});
	}

	public Flux<Instance> getInstances(String name) {
		return this.registry.getInstances(name).filter(Instance::isRegistered);
	}

	public Flux<Instance> getInstances() {
		LOGGER.debug("Deliver all registered instances");
		return this.registry.getInstances().filter(Instance::isRegistered);
	}

	public Mono<ResponseEntity<Instance>> getInstance(String id) {
		LOGGER.debug("Deliver registered instance with ID '{}'", id);
		return this.registry.getInstance(InstanceId.of(id))
			.filter(Instance::isRegistered)
			.map(ResponseEntity::ok)
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	public Mono<ResponseEntity<Void>> unregister(String id) {
		LOGGER.debug("Unregister instance with ID '{}'", id);
		return this.registry.deregister(InstanceId.of(id))
			.map((v) -> ResponseEntity.noContent().<Void>build())
			.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	public Flux<InstanceEvent> getEvents() {
		return this.eventStore.findAll();
	}

	public Flux<ServerSentEvent<InstanceEvent>> eventStream() {
		return Flux.from(this.eventStore).map((event) -> ServerSentEvent.builder(event).build()).mergeWith(ping());
	}

	public Flux<ServerSentEvent<Instance>> instanceStream(String id) {
		return Flux.from(this.eventStore)
			.filter((event) -> event.getInstance().equals(InstanceId.of(id)))
			.flatMap((event) -> this.registry.getInstance(event.getInstance()))
			.map((event) -> ServerSentEvent.builder(event).build())
			.mergeWith(ping());
	}

	@SuppressWarnings("unchecked")
	private static <T> Flux<ServerSentEvent<T>> ping() {
		return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
	}

}
