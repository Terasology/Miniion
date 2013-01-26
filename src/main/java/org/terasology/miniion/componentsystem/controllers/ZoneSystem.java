/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.miniion.componentsystem.controllers;

import org.terasology.componentSystem.*;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.ZoneSelectionComponent;

@RegisterComponentSystem(headedOnly = true)
public class ZoneSystem implements UpdateSubscriberSystem, RenderSystem {

	private EntityManager entityManager;

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void renderTransparent() {

		for (EntityRef entity : entityManager
				.iteratorEntities(ZoneSelectionComponent.class)) {
			ZoneSelectionComponent selection = entity
					.getComponent(ZoneSelectionComponent.class);
			selection.blockGrid.render();
			// entity.saveComponent(selection); // deserialization error tracing
		}

	}

	@Override
	public void renderOpaque() {
	}

	@Override
	public void renderOverlay() {
	}

	@Override
	public void renderFirstPerson() {

	}
}