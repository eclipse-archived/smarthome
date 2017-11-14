/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.common.osgi;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServiceBinder} class is a utility class to bind an <i>OSGi</i> service and to
 * inject the bound service into a specified object by calling the annotated method on it.
 * <p>
 * This class is an <i>OSGi</i> utility class which can be simply used for injecting a service by an API instead of
 * using a declarative way.
 * <p>
 * The annotated method for binding a service must have the signature {@code public void methodName(<ServiceType>)} and
 * the annotated method for unbinding a service must have the signature {@code public void methodName(<ServiceType>)} or
 * {@code public void methodName()}.
 * <p>
 * After starting the binder with {@link #open()} the service is tracked and if a service is found and bound, the
 * specified injection method is called with the service as parameter. If the service disappears again or the method
 * {@link #close()} is called to stop the binder, the specified injection method is called with {@code null} as
 * parameter if the unbind method is the same as for the binding, otherwise the released service instance is used as
 * parameter.
 * <p>
 * <b>Usage Example:</b> <code><pre>
 * ServiceBinder serviceBinder = new ServiceBinder(bundleContext, new MyObject());
 * serviceBinder.open();
 * serviceBinder.close();
 *   ...
 * public class MyObject {
 *   private AnyService anyService;
 *
 *   &#64Bind
 *   &#64Unbind
 *   public void setAnyService(AnyService anyService) {
 *     this.anyService = anyService;
 *   }
 * }
 * </pre></code>
 *
 * @author Michael Grammling - Initial Contribution
 */
public class ServiceBinder {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Bind {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Unbind {
    }

    private ServiceTracker<?, ?> serviceTracker;
    private Object trackedService;

    private Logger logger = LoggerFactory.getLogger(ServiceBinder.class);

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bundleContext the bundle context to be used for tracking the service (must not be null)
     * @param targetObject the object into which the service to be injected (must not be null)
     *
     * @throws IllegalArgumentException if any of the parameters is null,
     *             or if the annotated methods do not follow the scheme
     */
    public ServiceBinder(final BundleContext bundleContext, final Object targetObject) throws IllegalArgumentException {

        if (bundleContext == null) {
            throw new IllegalArgumentException("The bundle context must not be null!");
        }
        if (targetObject == null) {
            throw new IllegalArgumentException("The target object to be invoked must not be null!");
        }

        final Method bindMethod = findAnnotatedMethod(targetObject, Bind.class);
        if (bindMethod == null) {
            throw new IllegalArgumentException("The annotated bind method is missing!");
        }
        Class<?> serviceClazz = determineServiceType(bindMethod, true);

        final Method unbindMethod = findAnnotatedMethod(targetObject, Unbind.class);
        Class<?> serviceUnbindClazz = determineServiceType(unbindMethod, false);
        if ((serviceUnbindClazz != null) && (serviceUnbindClazz != serviceClazz)) {
            throw new IllegalArgumentException("The unbind method uses a wrong parameter type!");
        }

        this.serviceTracker = new ServiceTracker<>(bundleContext, serviceClazz.getName(),
                new ServiceTrackerCustomizer<Class<?>, Object>() {
                    @Override
                    public synchronized Object addingService(ServiceReference<Class<?>> reference) {
                        Object service = bundleContext.getService(reference);

                        if ((trackedService == null) && (service != null)) {
                            trackedService = service;
                            injectService(bindMethod, targetObject, service);

                            return service;
                        }

                        return null;
                    }

                    @Override
                    public void modifiedService(ServiceReference<Class<?>> reference, Object service) {
                    }

                    @Override
                    public synchronized void removedService(ServiceReference<Class<?>> reference, Object service) {

                        if (service == trackedService) {
                            trackedService = null;

                            if (bindMethod.equals(unbindMethod)) {
                                injectService(unbindMethod, targetObject, null);
                            } else {
                                injectService(unbindMethod, targetObject, service);
                            }
                        }
                    }
                });
    }

    /**
     * Starts tracking and binding the service.
     */
    public void open() {
        this.serviceTracker.open();
    }

    /**
     * Stops tracking the service and unbind it.
     */
    public void close() {
        this.serviceTracker.close();
    }

    private Method findAnnotatedMethod(Object object, Class<? extends Annotation> annotationClass)
            throws IllegalArgumentException {

        Method[] methods = object.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass)) {
                return method;
            }
        }

        return null;
    }

    private Class<?> determineServiceType(Method method, boolean required) throws IllegalArgumentException {

        if (method != null) {
            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length == 1) {
                return paramTypes[0];
            } else if (paramTypes.length > 1) {
                throw new IllegalArgumentException("The annotated method '" + method.getName()
                        + "' must contain one and only one parameter!");
            } else if (required) {
                throw new IllegalArgumentException("The annotated method '" + method.getName()
                        + "' must contain one parameter!");
            }
        }

        return null;
    }

    private void injectService(Method method, Object object, Object service) {
        if (method != null) {
            try {
                if (method.getParameterTypes().length == 1) {
                    method.invoke(object, service);
                } else {
                    method.invoke(object);
                }
            } catch (Exception ex) {
                logger.error("Error while executing inject method: {}", ex.getMessage(), ex);
            }
        }
    }

}
