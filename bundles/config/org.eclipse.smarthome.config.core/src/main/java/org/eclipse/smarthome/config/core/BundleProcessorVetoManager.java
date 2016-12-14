/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.config.core.BundleProcessor.BundleProcessorListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Utility class in order to delay an action until a binding's XML loading completed.
 *
 * It takes an action and whenever it is instructed to execute this action, it is going to check first if there are any
 * vetoes due to the fact that the XML meta-data of the relevant binding might not be fully loaded. If this is the case,
 * then the action will be delayed until the loading was finished.
 *
 * As this class is not an OSGi service itself, it needs some help from the outside by injecting/removing
 * BundleProcessors via {@link #addBundleProcessor(BundleProcessor)} and
 * {@link #removeBundleProcessor(BundleProcessor)}.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
public class BundleProcessorVetoManager<T> implements BundleProcessorListener {

    public interface Action<T> {
        void apply(T object);
    }

    private final Logger logger = LoggerFactory.getLogger(BundleProcessorVetoManager.class);

    private final Set<BundleProcessor> bundleProcessors = new HashSet<BundleProcessor>();
    private final Multimap<Long, BundleProcessor> vetoes = Multimaps
            .synchronizedListMultimap(LinkedListMultimap.<Long, BundleProcessor> create());
    private final Multimap<Long, T> queue = Multimaps.synchronizedListMultimap(LinkedListMultimap.<Long, T> create());
    private final Action<T> action;

    /**
     * Construct a BundleProcessorVetoManager for the given action
     *
     * @param action the action to run (potentially delayed)
     */
    public BundleProcessorVetoManager(Action<T> action) {
        this.action = action;
    }

    @Override
    public void bundleFinished(final BundleProcessor context, final Bundle bundle) {
        vetoes.remove(bundle.getBundleId(), context);
        if (vetoes.get(bundle.getBundleId()).isEmpty()) {
            logger.debug("Finished loading meta-data of bundle '{}'.", bundle.getSymbolicName());
            for (T object : queue.removeAll(bundle.getBundleId())) {
                action.apply(object);
            }
        } else {
            logger.debug("'{}' still vetoed by '{}', queueing '{}'", bundle.getSymbolicName(),
                    vetoes.get(bundle.getBundleId()), queue.get(bundle.getBundleId()));
        }
    }

    private Bundle getBundle(final Class<?> classFromBundle) {
        final ClassLoader classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return classFromBundle.getClassLoader();
            }
        });
        if (classLoader instanceof BundleReference) {
            Bundle bundle = ((BundleReference) classLoader).getBundle();
            logger.trace("Bundle of {} is {}", classFromBundle, bundle.getSymbolicName());
            return bundle;
        }
        return null;
    }

    /**
     * Run the given action.
     *
     * If necessary, it will get queued and executed once the binding's XML files have been loaded.
     *
     * @param object the argument for the action
     */
    public void applyActionFor(final T object) {
        boolean veto = false;
        Bundle bundle = getBundle(object.getClass());
        long bundleId = bundle.getBundleId();
        for (BundleProcessor proc : bundleProcessors) {
            if (!proc.hasFinishedLoading(bundle)) {
                veto = true;
                if (!vetoes.containsEntry(bundleId, proc)) {
                    logger.trace("Marking '{}' as vetoed by '{}'", bundle.getSymbolicName(), proc);
                    vetoes.put(bundleId, proc);
                }
            } else {
                logger.trace("'{}' already finished processing '{}'", proc, bundle.getSymbolicName());
            }
        }
        if (veto) {
            if (!queue.containsEntry(bundle, object)) {
                logger.trace("Queueing '{}' in bundle '{}'", object, bundle.getSymbolicName());
                queue.put(bundleId, object);
            }
            logger.debug("Meta-data of bundle '{}' is not fully loaded ({}), deferring action for '{}'",
                    bundle.getSymbolicName(), vetoes.get(bundleId), object);
        } else {
            logger.trace("No veto for bundle '{}', directly executing the action", bundle.getSymbolicName());
            action.apply(object);
        }
    }

    /**
     * Add a {@link BundleProcessor} to listen to.
     *
     * @param bundleProcessor
     */
    public void addBundleProcessor(BundleProcessor bundleProcessor) {
        bundleProcessors.add(bundleProcessor);
        bundleProcessor.registerListener(this);
    }

    /**
     * Remove a {@link BundleProcessor} again.
     *
     * @param bundleProcessor
     */
    public void removeBundleProcessor(BundleProcessor bundleProcessor) {
        bundleProcessor.unregisterListener(this);
        bundleProcessors.remove(bundleProcessor);
    }

}
