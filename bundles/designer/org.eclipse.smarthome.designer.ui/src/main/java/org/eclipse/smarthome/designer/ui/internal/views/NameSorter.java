/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.designer.ui.internal.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class NameSorter extends ViewerSorter {
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IFolder && e2 instanceof IFile) {
            return -1;
        } else if (e1 instanceof IFile && e2 instanceof IFolder) {
            return 1;
        } else if (e1 instanceof IResource && e2 instanceof IResource) {
            IResource r1 = (IResource) e1;
            IResource r2 = (IResource) e2;
            return r1.getName().compareToIgnoreCase(r2.getName());
        } else {
            return super.compare(viewer, e1, e2);
        }
    }
}