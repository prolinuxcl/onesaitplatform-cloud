/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.digitaltwin.broker.processor;

import org.json.JSONObject;

import com.minsait.onesait.platform.digitaltwin.broker.processor.model.EventResponseMessage;

public interface EventProcessor {

	public EventResponseMessage register(String apiKey, JSONObject data);

	public EventResponseMessage ping(String apiKey, JSONObject data);

	public EventResponseMessage log(String apiKey, JSONObject data);

	public EventResponseMessage shadow(String apiKey, JSONObject data);

	public EventResponseMessage notebook(String apiKey, JSONObject data);

	public EventResponseMessage flow(String apiKey, JSONObject data);

	public EventResponseMessage rule(String apiKey, JSONObject data);

	public EventResponseMessage custom(String apiKey, JSONObject data);

}
