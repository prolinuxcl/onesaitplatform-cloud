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
package com.minsait.onesait.platform.iotbroker.audit.aop;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteByIdMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyDeleteMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageDirection;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;
import com.minsait.onesait.platform.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.minsait.onesait.platform.iotbroker.audit.processor.DeleteAuditProcessor;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class DeleteAuditProcessorTest {

	@InjectMocks
	private DeleteAuditProcessor deleteAuditProcessor;

	private final String ONTOLOGY_NAME = "ontology";
	private final String QUERY = "db.test.find({});";

	@Test
	public void given_a_delete_message_get_audit_event() {

		IoTSession session = PojoGenerator.generateSession();
		GatewayInfo info = PojoGenerator.generateGatewayInfo();

		SSAPMessage<SSAPBodyDeleteMessage> message = new SSAPMessage<SSAPBodyDeleteMessage>();
		message.setMessageType(SSAPMessageTypes.DELETE);
		message.setBody(new SSAPBodyDeleteMessage());
		message.setSessionKey(session.getSessionKey());
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.getBody().setOntology(ONTOLOGY_NAME);
		message.getBody().setQuery(QUERY);

		IotBrokerAuditEvent event = deleteAuditProcessor.process(message, session, info);

		Assert.assertEquals(OperationType.DELETE.name(), event.getOperationType());
		Assert.assertEquals(ONTOLOGY_NAME, event.getOntology());
		Assert.assertNotNull(event.getQuery());
		Assert.assertNull(event.getData());
		Assert.assertEquals(info, event.getGatewayInfo());
		Assert.assertEquals(Module.IOTBROKER, event.getModule());
	}

	@Test
	public void given_a_delete_by_id_message_get_audit_event() {

		IoTSession session = PojoGenerator.generateSession();
		GatewayInfo info = PojoGenerator.generateGatewayInfo();

		SSAPMessage<SSAPBodyDeleteByIdMessage> message = new SSAPMessage<SSAPBodyDeleteByIdMessage>();
		message.setMessageType(SSAPMessageTypes.DELETE_BY_ID);
		message.setBody(new SSAPBodyDeleteByIdMessage());
		message.setSessionKey(session.getSessionKey());
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.getBody().setOntology(ONTOLOGY_NAME);

		IotBrokerAuditEvent event = deleteAuditProcessor.process(message, session, info);

		Assert.assertEquals(OperationType.DELETE.name(), event.getOperationType());
		Assert.assertEquals(ONTOLOGY_NAME, event.getOntology());
		Assert.assertNull(event.getQuery());
		Assert.assertNull(event.getData());
		Assert.assertEquals(info, event.getGatewayInfo());
		Assert.assertEquals(Module.IOTBROKER, event.getModule());
	}

	@Test
	public void given_getMessageTypes() {
		List<SSAPMessageTypes> types = deleteAuditProcessor.getMessageTypes();
		List<SSAPMessageTypes> expected = Arrays.asList(SSAPMessageTypes.DELETE, SSAPMessageTypes.DELETE_BY_ID);
		Assert.assertEquals(expected, types);
	}

}
