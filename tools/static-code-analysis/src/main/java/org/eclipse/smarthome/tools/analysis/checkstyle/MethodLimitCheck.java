package org.eclipse.smarthome.tools.analysis.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 *
 * Detects classes that have number of methods that exceed a certain limit.
 * This can be a clue that the class has low cohesion.
 * <p/>
 * This check is used in the checkstyle documentation to demonstrate the usage of the checkstyle Java API.
 *
 * @see <a href=
 *      "https://github.com/lhanson/checkstyle/blob/master/contrib/examples/checks/com/mycompany/checks/MethodLimitCheck.java">
 *      https://github.com/lhanson/checkstyle
 *      </a>
 * @see <a href="http://checkstyle.sourceforge.net/writingchecks.html">http://checkstyle.sourceforge.net/</a>
 * @author Lyle Hanson
 *
 */

public class MethodLimitCheck extends AbstractCheck {

    public static final String MSG_KEY = "too.many.methods";

    /**
     * The maximum number of methods per class/interface. Can accept different values in the configuration.
     * 
     */
    private int max = 30;

    /**
     * Give user a chance to configure max in the config file.
     *
     * @param aMax the user specified maximum parsed from configuration property.
     */
    public void setMax(int aMax) {
        max = aMax;
    }

    /**
     * We are interested in CLASS_DEF and INTERFACE_DEF Tokens.
     *
     * @see Check
     */
    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF };
    }

    /**
     * @see Check
     */
    @Override
    public void visitToken(DetailAST ast) {
        // the tree below a CLASS_DEF/INTERFACE_DEF looks like this:

        // CLASS_DEF
        // MODIFIERS
        // class name (IDENT token type)
        // EXTENDS_CLAUSE
        // IMPLEMENTS_CLAUSE
        // OBJBLOCK
        // {
        // some other stuff like variable declarations etc.
        // METHOD_DEF
        // more stuff, the users might mix methods, variables, etc.
        // METHOD_DEF
        // ...and so on
        // }

        // We use helper methods to navigate in the syntax tree

        // find the OBJBLOCK node below the CLASS_DEF/INTERFACE_DEF
        DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);

        // count the number of direct children of the OBJBLOCK
        // that are METHOD_DEFS
        int methodDefs = objBlock.getChildCount(TokenTypes.METHOD_DEF);

        // report error if limit is reached
        if (methodDefs > max) {
            log(ast.getLineNo(), MSG_KEY, new Integer(max));
        }
    }
}
