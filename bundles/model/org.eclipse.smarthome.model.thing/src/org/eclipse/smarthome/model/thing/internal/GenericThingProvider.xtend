/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.thing.internal

import java.util.Collection
import java.util.List
import java.util.Set
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener
import org.eclipse.smarthome.model.thing.thing.ModelChannel
import org.eclipse.smarthome.model.thing.thing.ModelPropertyContainer
import org.eclipse.smarthome.model.thing.thing.ModelThing
import org.eclipse.smarthome.model.thing.thing.ThingModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import java.util.Map
import org.eclipse.smarthome.core.thing.util.ThingHelper
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.smarthome.model.thing.thing.ModelBridge
import org.eclipse.smarthome.core.common.registry.AbstractProvider

/**
 * {@link ThingProvider} implementation which computes *.things files.
 * 
 * @author Oliver Libutzki - Initial contribution
 *
 */
class GenericThingProvider extends AbstractProvider<Thing> implements ThingProvider, ModelRepositoryChangeListener {

	private ModelRepository modelRepository

	private ThingTypeRegistry thingTypeRegistry

	private Map<String, Collection<Thing>> thingsMap = new ConcurrentHashMap

	private static final Logger logger = LoggerFactory.getLogger(GenericThingProvider)

	def void activate() {
		modelRepository.getAllModelNamesOfType("things").forEach [
			createThingsFromModel
		]
	}

	override Collection<Thing> getAll() {
		thingsMap.values.flatten.toList
	}

	def private void createThingsFromModel(String modelName) {
		logger.debug("Read things from model '{}'", modelName);

		val things = newArrayList
		if (modelRepository != null) {
			val model = modelRepository.getModel(modelName) as ThingModel
			if (model != null) {
				model.things.forEach [
					createThing(null, things)
				]
			}
		}
		if (things.empty) {
			thingsMap.remove(modelName)
		} else {
			thingsMap.put(modelName, things)
		}
	}

	def private void createThing(ModelThing modelThing, Bridge parentBridge, List<Thing> thingList) {
		var ThingTypeUID thingTypeUID = null
		var ThingUID thingUID = null
		if (parentBridge != null) {
			var ThingUID tempthingUID = new ThingUID(modelThing.id)
			thingTypeUID = new ThingTypeUID(tempthingUID.bindingId, tempthingUID.thingTypeId)
			thingUID = new ThingUID(thingTypeUID, tempthingUID.getId(), parentBridge.parentPath)
		} else {
			thingUID = new ThingUID(modelThing.id)
			thingTypeUID = new ThingTypeUID(thingUID.bindingId, thingUID.thingTypeId)
		}
		logger.debug("Creating thing for type '{}' with UID '{}.", thingTypeUID, thingUID);
		val configuration = modelThing.createConfiguration
		val thingBuilder = if (modelThing instanceof ModelBridge) {
				BridgeBuilder.create(thingUID)
			} else {
				ThingBuilder.create(thingUID)
			}

		thingBuilder.withConfiguration(configuration)
		if (parentBridge != null) {
			thingBuilder.withBridge(parentBridge.UID)
		}

		val thingType = thingTypeUID.thingType

		val channels = createChannels(thingUID, modelThing.channels, thingType?.channelDefinitions ?: newArrayList)
		thingBuilder.withChannels(channels)

		val thing = thingBuilder.build
		thingList += thing

		if (modelThing instanceof ModelBridge) {
			modelThing.things.forEach [
				createThing(thing as Bridge, thingList)
			]
		}
	}

	def private getParentPath(Bridge bridge) {
      var bridgeIds = bridge.UID.bridgeIds
      bridgeIds.add(bridge.UID.id)
      return bridgeIds
   }

	def private List<Channel> createChannels(ThingUID thingUID, List<ModelChannel> modelChannels,
		List<ChannelDefinition> channelDefinitions) {
		val Set<String> addedChannelIds = newHashSet
		val List<Channel> channels = newArrayList
		modelChannels.forEach [
			if (addedChannelIds.add(id)) {
				channels += ChannelBuilder.create(new ChannelUID(thingUID, id), type).withConfiguration(createConfiguration).build
			}
		]
		channelDefinitions.forEach [
			if (addedChannelIds.add(id)) {
				channels += ChannelBuilder.create(new ChannelUID(thingUID, id), it.type.itemType).build
			}
		]
		channels
	}

	def private createConfiguration(ModelPropertyContainer propertyContainer) {
		val configuration = new Configuration
		propertyContainer.properties.forEach [
			configuration.put(key, value)
		]
		configuration
	}

	def private getThingType(ThingTypeUID thingTypeUID) {
		thingTypeRegistry?.getThingType(thingTypeUID)
	}

	def protected void setModelRepository(ModelRepository modelRepository) {
		this.modelRepository = modelRepository
		modelRepository.addModelRepositoryChangeListener(this)
	}

	def protected void unsetModelRepository(ModelRepository modelRepository) {
		modelRepository.removeModelRepositoryChangeListener(this)
		this.modelRepository = null
	}

	override void modelChanged(String modelName, org.eclipse.smarthome.model.core.EventType type) {
		if (modelName.endsWith("things")) {
			switch type {
				case org.eclipse.smarthome.model.core.EventType.ADDED: {
					createThingsFromModel(modelName)
					val things = thingsMap.get(modelName) ?: newArrayList
					things.forEach [
						notifyListenersAboutAddedElement
					]
				}
				case org.eclipse.smarthome.model.core.EventType.MODIFIED: {
					val oldThings = thingsMap.get(modelName) ?: newArrayList
					val oldThingsUIDs = oldThings.map[UID].toList

					createThingsFromModel(modelName)
					val currentThings = thingsMap.get(modelName) ?: newArrayList
					val currentThingUIDs = currentThings.map[UID].toList
					val removedThings = oldThings.filter[!currentThingUIDs.contains(UID)]
					val addedThings = currentThings.filter[!oldThingsUIDs.contains(UID)]

					removedThings.forEach [
						notifyListenersAboutRemovedElement
					]
					addedThings.forEach [
						notifyListenersAboutAddedElement
					]
					
					currentThings.forEach[ newThing |
						oldThings.forEach [ oldThing |
							if (newThing.UID == oldThing.UID) {
								if (!ThingHelper.equals(oldThing, newThing)) {
									notifyListenersAboutUpdatedElement(oldThing, newThing)
								}
							}
						]
					]
				}
				case org.eclipse.smarthome.model.core.EventType.REMOVED: {
					val things = thingsMap.remove(modelName) ?: newArrayList
					things.forEach [
						notifyListenersAboutRemovedElement
					]
				}
			}
		}
	}

	def protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
		this.thingTypeRegistry = thingTypeRegistry
	}

	def protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
		this.thingTypeRegistry = null
	}

}
