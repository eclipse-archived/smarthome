package org.eclipse.smarthome.model.rule.ui.internal;

import org.eclipse.smarthome.model.core.ModelInjectorProvider;

import com.google.inject.Injector;

public class RuleUiInjectorProvider implements ModelInjectorProvider {

	@Override
	public Injector getInjector() {
		return RuleModelUIActivator.getInstance().getInjector(RuleModelUIActivator.ORG_ECLIPSE_SMARTHOME_MODEL_RULE_RULES);
	}

}
