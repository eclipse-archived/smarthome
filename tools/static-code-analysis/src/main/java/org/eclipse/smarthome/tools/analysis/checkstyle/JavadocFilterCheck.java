/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck;

/**
 * Provides a filter that determines whether to check the inner units for a
 * javadoc.
 * 
 * @author Petar Valchev
 *
 */
public class JavadocFilterCheck extends JavadocTypeCheck {
    private boolean checkInnerUnits = false;

    // A configuration property that determines whether to check the inner units
    public void setCheckInnerUnits(boolean checkInnerUnits) {
        this.checkInnerUnits = checkInnerUnits;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!checkInnerUnits) {
            DetailAST astParent = ast.getParent();
            // if outer class/interface/enum
            if (astParent == null) {
                super.visitToken(ast);
            }
        } else {
            super.visitToken(ast);
        }
    }

}
