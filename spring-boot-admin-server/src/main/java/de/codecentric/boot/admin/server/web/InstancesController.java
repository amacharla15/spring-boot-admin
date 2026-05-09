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

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

/**
 * REST controller for controlling registration of managed instances.
 */
@AdminController
@ResponseBody
public class InstancesController {

	private final InstanceAdministrationFacade instanceAdministrationFacade;

	public InstancesController(InstanceRegistry registry, InstanceEventStore eventStore) {
		this.instanceAdministrationFacade = new InstanceAdministrationFacade(registry, eventStore);
	}

	/**
	 * Register an instance.
	 * @param registration registration info
	 * @param builder the UriComponentsBuilder
	 * @return the registered instance id;
	 */
	@PostMapping(path = "/instances", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Map<String, InstanceId>>> register(@RequestBody Registration registration,
			UriComponentsBuilder builder) {
		return this.instanceAdministrationFacade.register(registration, builder);
	}

	/**
	 * List all registered instances with name
	 * @param name the name to search for
	 * @return application list
	 */
	@GetMapping(path = "/instances", produces = MediaType.APPLICATION_JSON_VALUE, params = "name")
	public Flux<Instance> instances(@RequestParam("name") String name) {
		return this.instanceAdministrationFacade.getInstances(name);
	}

	/**
	 * List all registered instances with name
	 * @return application list
	 */
	@GetMapping(path = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Instance> instances() {
		return this.instanceAdministrationFacade.getInstances();
	}

	/**
	 * Get a single instance.
	 * @param id the application identifier.
	 * @return the registered application.
	 */
	@GetMapping(path = "/instances/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Instance>> instance(@PathVariable String id) {
		return this.instanceAdministrationFacade.getInstance(id);
	}

	/**
	 * Unregister an instance
	 * @param id the instance id.
	 * @return response indicating the success
	 */
	@DeleteMapping(path = "/instances/{id}")
	public Mono<ResponseEntity<Void>> unregister(@PathVariable String id) {
		return this.instanceAdministrationFacade.unregister(id);
	}

	/**
	 * Retrieve all instance events as a JSON array. Returns all events for all registered
	 * instances. Useful for reconstructing application state or initializing the UI.
	 * @return flux of {@link InstanceEvent} objects
	 */
	@GetMapping(path = "/instances/events", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<InstanceEvent> events() {
		return this.instanceAdministrationFacade.getEvents();
	}

	/**
	 * Stream all instance events as Server-Sent Events (SSE). Returns a continuous stream
	 * of instance events for real-time monitoring and UI updates.
	 * @return flux of {@link ServerSentEvent} containing {@link InstanceEvent}
	 */
	@GetMapping(path = "/instances/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<InstanceEvent>> eventStream() {
		return this.instanceAdministrationFacade.eventStream();
	}

	/**
	 * Stream events for a specific instance as Server-Sent Events (SSE). Streams events
	 * for the instance identified by its ID. Each event is delivered as an SSE message.
	 * @param id the instance ID
	 * @return flux of {@link ServerSentEvent} containing {@link Instance}
	 */
	@GetMapping(path = "/instances/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<Instance>> instanceStream(@PathVariable String id) {
		return this.instanceAdministrationFacade.instanceStream(id);
	}

}
