/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui.internal.views;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.smarthome.designer.ui.UIActivator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * The label provider class is responsible for
 * providing labels to the view.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ViewLabelProvider extends LabelProvider {

    private Map<String, Image> imageCache = new HashMap<String, Image>();

    @Override
    public void dispose() {
        for (Image image : imageCache.values())
            image.dispose();
    }

    @Override
    public String getText(Object obj) {
        if (obj instanceof IFolder) {
            IResource res = (IResource) obj;
            return StringUtils.capitalize(res.getName());
        } else if (obj instanceof IFile) {
            IResource res = (IResource) obj;
            return res.getName();
        } else {
            return obj.toString();
        }
    }

    @Override
    public Image getImage(Object obj) {
        if (obj instanceof IFolder) {
            IFolder folder = (IFolder) obj;
            String name = folder.getName().toLowerCase();
            Image image = imageCache.get(name);
            if (image == null) {
                ImageDescriptor imageDesc = UIActivator.getImageDescriptor("icons/" + name + ".png");
                if (imageDesc != null) {
                    image = imageDesc.createImage();
                    imageCache.put(name, image);
                    return image;
                }
            } else {
                return image;
            }
            // use the folder image as a default
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        } else if (obj instanceof IFile) {
            IFile file = (IFile) obj;
            String fileExt = file.getFileExtension();
            Image image = imageCache.get(fileExt);
            if (image == null) {
                ImageDescriptor imageDesc = PlatformUI.getWorkbench().getEditorRegistry()
                        .getImageDescriptor("." + fileExt);
                image = imageDesc.createImage();
                imageCache.put(fileExt, image);
            }
            return image;
        } else {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
        }
    }
}