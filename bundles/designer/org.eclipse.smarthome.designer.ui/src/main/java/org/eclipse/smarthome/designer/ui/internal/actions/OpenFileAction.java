/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.designer.ui.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.smarthome.designer.ui.UIActivator;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Action for opening files
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class OpenFileAction extends Action {

    /**
     * The id of this action.
     */
    public static final String ID = UIActivator.PLUGIN_ID + ".OpenFileAction";//$NON-NLS-1$

    private Viewer viewer;

    public OpenFileAction(TreeViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void run() {
        ISelection selection = viewer.getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj instanceof IFile) {
            openFile((IFile) obj);
        }
    }

    /**
     * Opens an editor on the given file resource.
     *
     * @param file the file resource
     */
    void openFile(IFile file) {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        FileEditorInput editorInput = null;

        // IEditorReference[] editors = activePage.getEditorReferences();
        // for (IEditorReference editorReference : editors) {
        // IEditorInput input;
        // try {
        // input = editorReference.getEditorInput();
        // if (input instanceof FileEditorInput) {
        // FileEditorInput fileEditorInput = (FileEditorInput) input;
        // if(fileEditorInput.getStorage().getFullPath().equals(file.getFullPath())) {
        // editorInput = fileEditorInput;
        // }
        // }
        // } catch (PartInitException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (CoreException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }

        if (editorInput == null) {
            editorInput = new FileEditorInput(file);
        }

        IEditorDescriptor editor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
        String editorId = editor == null ? "org.eclipse.ui.DefaultTextEditor" : editor.getId();
        try {
            activePage.openEditor(editorInput, editorId);
        } catch (PartInitException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
