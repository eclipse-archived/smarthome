/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractDiscoveryService} provides methods which handle the
 * {@link DiscoveryListener}s.
 * 
 * Subclasses do not have to care about adding and removing those listeners.
 * They can use the protected methods {@link #thingDiscovered(DiscoveryResult)}
 * and {@link #thingRemoved(String)} in order to notify the registered
 * {@link DiscoveryListener}s.
 * 
 * @author Oliver Libutzki - Initial contribution
 * 
 */
public abstract class AbstractDiscoveryService implements DiscoveryService {

	private final static Logger logger = LoggerFactory
			.getLogger(AbstractDiscoveryService.class);

	private List<DiscoveryListener> discoveryListeners = new CopyOnWriteArrayList<>();
	private boolean autoDiscoveryEnabled = false;

	@Override
	public void setAutoDiscoveryEnabled(boolean enabled) {
		this.autoDiscoveryEnabled = enabled;
	}

	@Override
	public boolean isAutoDiscoveryEnabled() {
		return autoDiscoveryEnabled;
	}

	@Override
	public void addDiscoveryListener(DiscoveryListener listener) {
		discoveryListeners.add(listener);
	}

	@Override
	public void removeDiscoveryListener(DiscoveryListener listener) {
		discoveryListeners.remove(listener);
	}

	/**
	 * Notifies the registered {@link DiscoveryListener}s about a discovered
	 * device.
	 * 
	 * @param discoveryResult
	 *            Holds the information needed to identify the discovered
	 *            device.
	 */
	protected void thingDiscovered(DiscoveryResult discoveryResult) {
		for (DiscoveryListener discoveryListener : discoveryListeners) {
			try {
				discoveryListener.thingDiscovered(this, discoveryResult);
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(
							"An error occurred while calling the discovery listener "
									+ discoveryListener.getClass().getName()
									+ ".", e);
				}
			}
		}
	}

	/**
	 * Notifies the registered {@link DiscoveryListener}s about a removed
	 * device.
	 * 
	 * @param thingUID
	 *            The UID of the removed thing.
	 */
	protected void thingRemoved(ThingUID thingUID) {
		for (DiscoveryListener discoveryListener : discoveryListeners) {
			try {
				discoveryListener.thingRemoved(this, thingUID);
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(
							"An error occurred while calling the discovery listener "
									+ discoveryListener.getClass().getName()
									+ ".", e);
				}
			}
		}
	}

	/**
	 * Notifies the registered {@link DiscoveryListener}s about an error.
	 * 
	 * @param exception
	 *            The exception which occurred.
	 */
	protected void discoveryErrorOccurred(Exception exception) {
		for (DiscoveryListener discoveryListener : discoveryListeners) {
			try {
				discoveryListener.discoveryErrorOccurred(this, exception);
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(
							"An error occurred while calling the discovery listener "
									+ discoveryListener.getClass().getName()
									+ ".", e);
				}
			}
		}
	}

	/**
	 * Notifies the registered {@link DiscoveryListener}s about a completed
	 * discovery.
	 */
	protected void discoveryFinished() {
		for (DiscoveryListener discoveryListener : discoveryListeners) {
			try {
				discoveryListener.discoveryFinished(this);
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error(
							"An error occurred while calling the discovery listener "
									+ discoveryListener.getClass().getName()
									+ ".", e);
				}
			}
		}
	}
}
