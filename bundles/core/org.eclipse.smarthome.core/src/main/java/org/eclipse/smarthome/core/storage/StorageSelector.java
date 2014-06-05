package org.eclipse.smarthome.core.storage;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.internal.CoreActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * {@link StorageSelector} helps to select a storage based on the StorageService
 * with the highest service ranking.
 * 
 * @author Dennis Nobel - Initial contribution
 * 
 * @param <T>
 *            Type of the storage
 */
public class StorageSelector<T> {

    private BundleContext bundleContext;

    private Collection<StorageService> storageServiceCandidates = new CopyOnWriteArrayList<StorageService>();

    private StorageSelectionListener<T> storageSelectionListener;

    private String storageName;

    public StorageSelector(BundleContext bundleContext, String storageName,
            StorageSelectionListener<T> storageSelectionListener) {
        this.bundleContext = bundleContext;
        this.storageName = storageName;
        this.storageSelectionListener = storageSelectionListener;
    }

    /**
     * {@link StorageSelectionListener} must be implemented to get notified
     * about a selected storage.
     * 
     * @author Dennis Nobel - Initial contribution
     * 
     * @param <T>
     *            Type of the storage
     */
    public interface StorageSelectionListener<T> {

        void storageSelected(Storage<T> storage);

    }

    @SuppressWarnings("unchecked")
    public void addStorageService(StorageService storageService) {
        storageServiceCandidates.add(storageService);

        // small optimization - if there is just one StorageService available
        // we don't have to select amongst others.
        if (storageServiceCandidates.size() == 1) {
            storageSelectionListener.storageSelected((Storage<T>) storageService
                    .getStorage(this.storageName));
        } else {
            storageSelectionListener.storageSelected(findStorageServiceByPriority());
        }
    }

    /**
     * Returns a {@link Storage} returned by a {@link StorageService} with the
     * highest priority available in the OSGi container. In theory this should
     * not be necessary if DS would have taken the {@code service.ranking}
     * property into account properly. Unfortunately it haven't during my tests.
     * So this method should be seen as workaround until somebody proofs that DS
     * evaluates the property correctly.
     * 
     * @return a {@link Storage} created by the {@link StorageService} with the
     *         highest priority (according to the OSGi container)
     */
    private Storage<T> findStorageServiceByPriority() {
        ServiceReference<?> reference = bundleContext.getServiceReference(StorageService.class);
        if (reference != null) {
            StorageService service = (StorageService) CoreActivator.getContext().getService(
                    reference);
            Storage<T> storage = service.getStorage(this.storageName);

            return storage;
        }

        // no service of type StorageService available
        throw new IllegalStateException(
                "There is no Service of type 'StorageService' available. This should not happen!");
    }

    public void removeStorageService(StorageService storageService) {
        storageServiceCandidates.remove(storageService);

        // if there are still StorageService left, we have to select
        // a new one to take over ...
        if (storageServiceCandidates.size() > 0) {
            storageSelectionListener.storageSelected(findStorageServiceByPriority());
        } else {
            storageSelectionListener.storageSelected(null);
        }
    }
}
