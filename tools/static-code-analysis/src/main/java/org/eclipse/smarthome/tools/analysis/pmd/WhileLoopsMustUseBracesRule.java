package org.eclipse.smarthome.tools.analysis.pmd;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTWhileStatement;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * The rule is checking if the while loop is declared with braces
 * <p/>
 * The check is used in the PMD documentation to demonstrate the usage of the PMD Java API.
 *
 * @see <a href="http://pmd.sourceforge.net/snapshot/customizing/howtowritearule.html">http://pmd.sourceforge.net/</a>
 */

public class WhileLoopsMustUseBracesRule extends AbstractJavaRule {
    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        Node firstStmt = node.jjtGetChild(1);
        if (!hasBlockAsFirstChild(firstStmt)) {
            addViolation(data, node);
        }
        return super.visit(node, data);
    }

    private boolean hasBlockAsFirstChild(Node node) {
        return (node.jjtGetNumChildren() != 0 && (node.jjtGetChild(0) instanceof ASTBlock));
    }
}
