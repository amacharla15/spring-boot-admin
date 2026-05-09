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

import java.time.Duration;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

public class StatusUpdateTrigger extends AbstractInstanceUpdateTrigger {

	public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> publisher, Duration updateInterval,
			Duration statusLifetime, Duration maxBackoff) {
		this(new StatusUpdateTriggerStrategy(statusUpdater), publisher, updateInterval, statusLifetime, maxBackoff);
	}

	StatusUpdateTrigger(InstanceUpdateTriggerStrategy triggerStrategy, Publisher<InstanceEvent> publisher,
			Duration updateInterval, Duration statusLifetime, Duration maxBackoff) {
		super(publisher, triggerStrategy, updateInterval, statusLifetime, maxBackoff);
	}

	protected Mono<Void> updateStatus(InstanceId instanceId) {
		return updateInstance(instanceId);
	}

}
