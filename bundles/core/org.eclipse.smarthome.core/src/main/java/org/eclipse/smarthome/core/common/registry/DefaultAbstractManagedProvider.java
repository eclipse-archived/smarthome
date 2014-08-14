package org.eclipse.smarthome.core.common.registry;

/**
 * {@link DefaultAbstractManagedProvider} is a specific
 * {@link AbstractManagedProvider} implementation, where the stored element is
 * the same as the element of the provider. So no transformation is needed.
 * Therefore only two generic parameters are needed instead of three.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 * @param <E>
 *            type of the element
 * @param <K>
 *            type of the element key
 */
public abstract class DefaultAbstractManagedProvider<E, K> extends AbstractManagedProvider<E, K, E> {

    @Override
    protected E toElement(String key, E element) {
        return element;
    }

    @Override
    protected E toPersistableElement(E element) {
        return element;
    }

}
