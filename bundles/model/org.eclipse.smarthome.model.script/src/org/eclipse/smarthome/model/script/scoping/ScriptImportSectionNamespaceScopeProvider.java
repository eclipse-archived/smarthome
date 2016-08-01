/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.scoping;

import java.util.List;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.xbase.scoping.XImportSectionNamespaceScopeProvider;

public class ScriptImportSectionNamespaceScopeProvider extends XImportSectionNamespaceScopeProvider {

    public static final QualifiedName CORE_LIBRARY_TYPES_PACKAGE = QualifiedName.create("org", "eclipse", "smarthome",
            "core", "library", "types");
    public static final QualifiedName CORE_LIBRARY_ITEMS_PACKAGE = QualifiedName.create("org", "eclipse", "smarthome",
            "core", "library", "items");
    public static final QualifiedName CORE_ITEMS_PACKAGE = QualifiedName.create("org", "eclipse", "smarthome", "core",
            "items");
    public static final QualifiedName CORE_PERSISTENCE_PACKAGE = QualifiedName.create("org", "eclipse", "smarthome",
            "core", "persistence");
    public static final QualifiedName MODEL_SCRIPT_ACTIONS_PACKAGE = QualifiedName.create("org", "eclipse",
            "smarthome", "model", "script", "actions");

    @Override
    protected List<ImportNormalizer> getImplicitImports(boolean ignoreCase) {
        List<ImportNormalizer> implicitImports = super.getImplicitImports(ignoreCase);
        implicitImports.add(doCreateImportNormalizer(CORE_LIBRARY_TYPES_PACKAGE, true, false));
        implicitImports.add(doCreateImportNormalizer(CORE_LIBRARY_ITEMS_PACKAGE, true, false));
        implicitImports.add(doCreateImportNormalizer(CORE_ITEMS_PACKAGE, true, false));
        implicitImports.add(doCreateImportNormalizer(CORE_PERSISTENCE_PACKAGE, true, false));
        implicitImports.add(doCreateImportNormalizer(MODEL_SCRIPT_ACTIONS_PACKAGE, true, false));
        return implicitImports;
    }

}
