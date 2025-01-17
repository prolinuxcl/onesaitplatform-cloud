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
package com.minsait.onesait.platform.multitenant.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;
import com.minsait.onesait.platform.config.model.listener.EntityListener;
import com.minsait.onesait.platform.config.model.listener.VersioningListener;

@Service
public class BeanUtil implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		BeanUtil.context = applicationContext;
	}

	public void initializeRepositoryBeans() {
		EntityListener.initialize();
		AuditEntityListener.initialize();
		VersioningListener.initialize();
	}

	public static <T> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static Environment getEnv() {
		return context.getEnvironment();
	}
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		initializeRepositoryBeans();

	}

	@PostConstruct
	public void init() {
		initializeRepositoryBeans();
	}
}