package org.eclipse.smarthome.model.script.jvmmodel;

import org.eclipse.smarthome.model.script.script.QuantityLiteral;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.computation.XbaseTypeComputer;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

@SuppressWarnings("restriction")
public class ScriptTypeComputer extends XbaseTypeComputer {

    @Override
    public void computeTypes(XExpression expression, ITypeComputationState state) {
        if (expression instanceof QuantityLiteral) {
            _computeTypes((QuantityLiteral) expression, state);
        } else {
            super.computeTypes(expression, state);
        }
    }

    protected void _computeTypes(final QuantityLiteral assignment, ITypeComputationState state) {
        LightweightTypeReference qt = getRawTypeForName(Number.class, state);
        state.acceptActualType(qt);
    }

}
