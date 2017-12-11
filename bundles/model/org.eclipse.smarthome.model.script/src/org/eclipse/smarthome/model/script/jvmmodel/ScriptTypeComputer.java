package org.eclipse.smarthome.model.script.jvmmodel;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.script.script.QuantityLiteral;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeExpectation;
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
        LightweightTypeReference qt = null;
        for (ITypeExpectation exp : state.getExpectations()) {
            if (exp.getExpectedType() == null) {
                continue;
            }

            if (exp.getExpectedType().isType(Number.class)) {
                qt = getRawTypeForName(Number.class, state);
            }
            if (exp.getExpectedType().isType(State.class)) {
                qt = getRawTypeForName(Number.class, state);
            }
            if (exp.getExpectedType().isType(Command.class)) {
                qt = getRawTypeForName(Number.class, state);
            }
        }
        if (qt == null) {
            qt = getRawTypeForName(QuantityType.class, state);
        }
        state.acceptActualType(qt);
    }

}
