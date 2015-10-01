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
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import java.util.concurrent.CopyOnWriteArrayList
import org.eclipse.smarthome.model.thing.internal.GenericThingProvider.QueueContent
import java.util.ArrayList
import org.eclipse.smarthome.core.thing.type.TypeResolver

/**
 * {@link ThingProvider} implementation which computes *.things files.
 * 
 * @author Oliver Libutzki - Initial contribution
 * @author niehues - Fix ESH Bug 450236
 *         https://bugs.eclipse.org/bugs/show_bug.cgi?id=450236 - Considering
 *         ThingType Description
 * @author Simon Kaufmann - Added asynchronous retry in case the handler 
 *         factory cannot load a thing yet (bug 470368) 
 * 
 */
class GenericThingProvider extends AbstractProvider<Thing> implements ThingProvider, ModelRepositoryChangeListener {

	private ModelRepository modelRepository

	private ThingTypeRegistry thingTypeRegistry

	private Map<String, Collection<Thing>> thingsMap = new ConcurrentHashMap

	private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<ThingHandlerFactory>()

    private val List<QueueContent> queue = new CopyOnWriteArrayList
    private var Thread lazyRetryThread = null
    
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
		thingsMap.put(modelName, things)
	}

	def private void createThing(ModelThing modelThing, Bridge parentBridge, Collection<Thing> thingList) {
		createThing(modelThing, parentBridge, thingList, null)
	}

	def private void createThing(ModelThing modelThing, Bridge parentBridge, Collection<Thing> thingList,
		ThingHandlerFactory thingHandlerFactory) {
		var ThingTypeUID thingTypeUID = null
		var ThingUID thingUID = null
		var ThingUID bridgeUID = null
		if (parentBridge != null) {
			val bindingId = parentBridge.thingTypeUID.bindingId
			val thingTypeId = modelThing.thingTypeId
			val thingId = modelThing.thingId
			thingTypeUID = new ThingTypeUID(bindingId, thingTypeId)
			thingUID = new ThingUID(thingTypeUID, thingId, parentBridge.parentPath)
			bridgeUID = parentBridge.UID
		} else {
			thingUID = new ThingUID(modelThing.id)
			thingTypeUID = new ThingTypeUID(thingUID.bindingId, thingUID.thingTypeId)
			if(modelThing.bridgeUID != null && !modelThing.bridgeUID.empty) {
				bridgeUID = new ThingUID(modelThing.bridgeUID)
			}
		}

		if (!isSupportedByThingHandlerFactory(thingTypeUID, thingHandlerFactory)) {
			// return silently, we were not asked to do anything
			return
		}

		logger.trace("Creating thing for type '{}' with UID '{}.", thingTypeUID, thingUID);
		val configuration = modelThing.createConfiguration
		val uid = thingUID
		if (thingList.exists[UID.equals(uid)]) {
			//the thing is already in the list and nothing will be done!
			logger.debug("Thing already exists {}", uid.toString)
			return
		}

		val thingFromHandler = getThingFromThingHandlerFactories(thingTypeUID, configuration, thingUID, bridgeUID,
			thingHandlerFactory)

		val thingBuilder = if (modelThing instanceof ModelBridge) {
				BridgeBuilder.create(thingUID)
			} else {
				ThingBuilder.create(thingUID)
			}

		thingBuilder.withConfiguration(configuration)
		thingBuilder.withBridge(bridgeUID)
		

		val thingType = thingTypeUID.thingType

		val channels = createChannels(thingUID, modelThing.channels, thingType?.channelDefinitions ?: newArrayList)
		thingBuilder.withChannels(channels)

		var thing = thingBuilder.build

		//ask the ThingHandlerFactories for a thing
		if (thingFromHandler != null) {

			//If a thingHandlerFactory could create a thing, merge the content of the modelThing to it
			thingFromHandler.merge(thing)
		}

		thingList += thingFromHandler ?: thing

		if (modelThing instanceof ModelBridge) {
			val bridge = (thingFromHandler ?: thing) as Bridge
			modelThing.things.forEach [
				createThing(bridge as Bridge, thingList, thingHandlerFactory)
			]
		}
	}

	def private boolean isSupportedByThingHandlerFactory(ThingTypeUID thingTypeUID, ThingHandlerFactory specific) {
		if (specific !== null) {
			return specific.supportsThingType(thingTypeUID)
		}
		for (ThingHandlerFactory thingHandlerFactory : thingHandlerFactories) {
			if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
				return true;
			}
		}
		return false;
	}

	def private Thing getThingFromThingHandlerFactories(ThingTypeUID thingTypeUID, Configuration configuration,
		ThingUID thingUID, ThingUID bridgeUID, ThingHandlerFactory specific) {
		if (specific != null && specific.supportsThingType(thingTypeUID)) {
			logger.trace("Creating thing from specific ThingHandlerFactory {} for thingType {}", specific, thingTypeUID)
			return getThingFromThingHandlerFactory(thingTypeUID, configuration, thingUID, bridgeUID, specific)
		}
		for (ThingHandlerFactory thingHandlerFactory : thingHandlerFactories) {
			logger.trace("Searching thingHandlerFactory for thingType: {}", thingTypeUID)
			if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
			    return getThingFromThingHandlerFactory(thingTypeUID, configuration, thingUID, bridgeUID, thingHandlerFactory)
			}
		}
		null
	}
	
	def private getThingFromThingHandlerFactory(ThingTypeUID thingTypeUID, Configuration configuration,
        ThingUID thingUID, ThingUID bridgeUID, ThingHandlerFactory thingHandlerFactory) {
        val thing = thingHandlerFactory.createThing(thingTypeUID, configuration, thingUID, bridgeUID)
        if (thing == null) {
            // Apparently the HandlerFactory's eyes were bigger than its stomach...
            // Possible cause: Asynchronous loading of the XML files
            // Add the data to the queue in order to retry it later
            logger.debug("ThingHandlerFactory '{}' claimed it can handle '{}' type but actually did not. Queued for later refresh.", thingHandlerFactory.class.simpleName, thingTypeUID.asString)
            queue.add(new QueueContent(thingTypeUID, configuration, thingUID, bridgeUID, thingHandlerFactory))
            if (lazyRetryThread == null || !lazyRetryThread.alive) {
                lazyRetryThread = new Thread(lazyRetryRunnable)
                lazyRetryThread.start
            }
        }
        return thing
    }

	def dispatch void merge(Thing targetThing, Thing sourceThing) {
		targetThing.bridgeUID = sourceThing.bridgeUID
		targetThing.configuration.merge(sourceThing.configuration)
		targetThing.merge(sourceThing.channels)

	}

	def dispatch void merge(Configuration target, Configuration source) {
		source.keySet.forEach [
			target.put(it, source.get(it))
		]
	}

	def dispatch void merge(Thing targetThing, List<Channel> source) {
		val List<Channel> channelsToAdd = newArrayList()
		source.forEach [ sourceChannel |
			val targetChannels = targetThing.channels.filter[it.UID.equals(sourceChannel.UID)]
			targetChannels.forEach [
				merge(sourceChannel)
			]
			if (targetChannels.empty) {
				channelsToAdd.add(sourceChannel)
			}
		]

		//add the channels only defined in source list to the target list
		ThingHelper.addChannelsToThing(targetThing, channelsToAdd)
	}

	def dispatch void merge(Channel target, Channel source) {
		target.configuration.merge(source.configuration)
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
				channels +=
					ChannelBuilder.create(new ChannelUID(thingUID, id), type).withConfiguration(createConfiguration).
						build
			}
		]
		channelDefinitions.forEach [
			if (addedChannelIds.add(id)) {
			    val channelType = TypeResolver.resolve(it.channelTypeUID)
			    if(channelType != null) {
				    channels += ChannelBuilder.create(new ChannelUID(thingUID, id), channelType.itemType).build
				} else {
				    logger.warn(
                        "Could not create channel '{}' for thing '{}', because channel type '{}' could not be found.",
                        it.getId(), thingUID, it.getChannelTypeUID());
				}
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

					currentThings.forEach [ newThing |
						val oldThing = oldThings.findFirst[it.UID == newThing.UID]
						if(oldThing != null) {
							if (!ThingHelper.equals(oldThing, newThing)) {
								notifyListenersAboutUpdatedElement(oldThing, newThing)
							}
						}
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

	def protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
		logger.debug("ThingHandlerFactory added {}", thingHandlerFactory)
		this.thingHandlerFactories.add(thingHandlerFactory);
		thingHandlerFactoryAdded(thingHandlerFactory)
	}

	def protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
		this.thingHandlerFactories.remove(thingHandlerFactory);
		thingHandlerFactoryRemoved()
	}

	def private thingHandlerFactoryRemoved() {
		//Don't do anything, Things should not be deleted
	}

	def private thingHandlerFactoryAdded(ThingHandlerFactory thingHandlerFactory) {
		thingsMap.keySet.forEach [
			//create things for this specific thingHandlerFactory from the model.
			createThingsFromModelForThingHandlerFactory(it, thingHandlerFactory)
		]
	}

	def private createThingsFromModelForThingHandlerFactory(String modelName, ThingHandlerFactory factory) {
		val oldThings = thingsMap.get(modelName).clone
		val newThings = newArrayList()
		if (modelRepository != null) {
			val model = modelRepository.getModel(modelName) as ThingModel
			if (model != null) {
				model.things.forEach [
					createThing(null, newThings, factory)
				]
			}
		}
		newThings.forEach [ newThing |
			val oldThing = oldThings.findFirst[it.UID == newThing.UID]
			if (oldThing != null) {
				// this thing already existed, so let's ignore it
			} else {
				logger.debug("Adding thing '{}' from model '{}.", newThing.UID, modelName);
				thingsMap.get(modelName).add(newThing)
				newThing.notifyListenersAboutAddedElement
			}
		]
	}

    private val lazyRetryRunnable = new Runnable() {
        override run() {
            logger.debug("Starting lazy retry thread")
            while (!queue.empty) {
                if (!queue.empty) {
                    val newThings = new ArrayList
                    queue.forEach [ qc |
                        logger.trace("Searching thingHandlerFactory for thingType: {}", qc.thingTypeUID)
                        val thing = qc.thingHandlerFactory.createThing(qc.thingTypeUID, qc.configuration, qc.thingUID,
                            qc.bridgeUID)
                        if (thing != null) {
                            queue.remove(qc)
                            logger.debug("Successfully loaded '{}' during retry", qc.thingUID)
                            newThings.add(thing)
                        }
                    ]
                    if (!newThings.empty) {
                        newThings.forEach [ newThing |
                            val modelName = thingsMap.keySet.findFirst[mName|
                                !thingsMap.get(mName).filter[it.UID == newThing.UID].empty]
                            val oldThing = thingsMap.get(modelName).findFirst[it.UID == newThing.UID]
                            if (oldThing != null) {
                                thingsMap.get(modelName).remove(oldThing)
                                thingsMap.get(modelName).add(newThing)
                                logger.debug("Refreshing thing '{}' after successful retry", newThing.UID)
                                if (!ThingHelper.equals(oldThing, newThing)) {
                                    notifyListenersAboutUpdatedElement(oldThing, newThing)
                                }
                            } else {
                                throw new IllegalStateException(String.format("Item %s not yet known", newThing.UID))
                            }
                        ]
                    }
                }
                if (!queue.empty) {
                    Thread.sleep(1000)
                }
            }
            logger.debug("Lazy retry thread ran out of work. Good bye.")
        }
    }

    @Data
    private static final class QueueContent {
        ThingTypeUID thingTypeUID
        Configuration configuration
        ThingUID thingUID
        ThingUID bridgeUID
        ThingHandlerFactory thingHandlerFactory
    }

}
