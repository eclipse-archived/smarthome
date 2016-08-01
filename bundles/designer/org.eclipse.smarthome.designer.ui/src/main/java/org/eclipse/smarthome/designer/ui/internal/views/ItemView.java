/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui.internal.views;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.ItemRegistryChangeListener;
import org.eclipse.smarthome.designer.core.CoreActivator;
import org.eclipse.smarthome.designer.ui.UIActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ItemView extends ViewPart {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "org.eclipse.smarthome.designer.ui.ItemView";

    private TreeViewer viewer;

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider, ItemRegistryChangeListener {
        private Object invisibleRoot;

        private ItemRegistry registry;

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
            registry = null;
        }

        @Override
        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite())) {
                if (invisibleRoot == null)
                    initialize();
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child) {
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent == invisibleRoot) {
                if (registry != null) {
                    return getRootGroups(registry.getItems());
                }
            } else if (parent instanceof GroupItem) {
                GroupItem group = (GroupItem) parent;
                return group.getMembers().toArray();
            }
            return new Object[0];
        }

        private Object[] getRootGroups(Collection<Item> items) {
            List<GroupItem> allGroups = new LinkedList<GroupItem>();
            List<GroupItem> rootGroups = new LinkedList<GroupItem>();

            // first, let's get all group items
            for (Item item : items) {
                if (item instanceof GroupItem) {
                    allGroups.add((GroupItem) item);
                }
            }

            // now select only the groups, which are contained in another group
            for (GroupItem group1 : allGroups) {
                boolean found = false;
                for (GroupItem group2 : allGroups) {
                    if (group2.getMembers().contains(group1)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    rootGroups.add(group1);
            }
            return rootGroups.toArray(new GroupItem[rootGroups.size()]);
        }

        @Override
        public boolean hasChildren(Object parent) {
            return getChildren(parent).length > 0;
        }

        private void initialize() {
            registry = UIActivator.itemRegistryTracker.getService();
            if (registry != null) {
                try {
                    CoreActivator.updateFolderObserver();
                } catch (IOException e) {
                    // something is wrong, so we won't reload the folders.
                }
                registry.addRegistryChangeListener(this);
                invisibleRoot = new Object();
                allItemsChanged(null);
            }
        }

        @Override
        public void allItemsChanged(Collection<String> oldItemNames) {
            Display display = PlatformUI.getWorkbench().getDisplay();
            if (!display.isDisposed()) {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        viewer.refresh();
                    }
                });
            }
        }

        @Override
        public void added(Item item) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.refresh();
                }
            });
        }

        @Override
        public void removed(Item item) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.refresh();
                }
            });
        }

        @Override
        public void updated(Item oldItem, Item item) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.refresh();
                }
            });
        }
    }

    static class ViewLabelProvider extends LabelProvider {

        private Map<String, Image> imageCache = new HashMap<String, Image>();

        @Override
        public void dispose() {
            for (Image image : imageCache.values())
                image.dispose();
        }

        @Override
        public String getText(Object obj) {
            if (obj instanceof Item) {
                Item item = (Item) obj;
                return item.getName();
            } else {
                return obj.toString();
            }
        }

        @Override
        public Image getImage(Object obj) {
            String imageKey = null;
            if (obj instanceof GroupItem) {
                imageKey = ISharedImages.IMG_OBJ_FOLDER;
            } else {
                String imgName = obj.getClass().getSimpleName().replace("Item", "").toLowerCase();
                Image image = imageCache.get(imgName);
                if (image == null) {
                    ImageDescriptor imageDesc = UIActivator.getImageDescriptor("icons/" + imgName + ".png");
                    if (imageDesc != null) {
                        image = imageDesc.createImage();
                        imageCache.put(imgName, image);
                        return image;
                    }
                } else {
                    return image;
                }
                // use the shared image as a default
                imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }

    static class NameSorter extends ViewerSorter {
    }

    /**
     * The constructor.
     */
    public ItemView() {
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}