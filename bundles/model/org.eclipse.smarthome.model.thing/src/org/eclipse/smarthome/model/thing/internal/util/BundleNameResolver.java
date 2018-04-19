package org.eclipse.smarthome.model.thing.internal.util;

public interface BundleNameResolver {

    String resolveBundleName(Class<?> clazz);
}
