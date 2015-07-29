package org.eclipse.smarthome.automation.core;

import java.util.Collection;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.core.common.registry.AbstractProvider;
import org.eclipse.smarthome.core.common.registry.ManagedProvider;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class RuleProvider extends AbstractProvider implements ManagedProvider {

    private RuleManagerImpl rm;

    public RuleProvider(RuleManagerImpl rm, BundleContext context) {
        this.rm = rm;
    }

    @Override
    public Collection getAll() {
        return rm.getRules();
    }

    @Override
    public Object update(Object element) {
        String id = ((Rule) element).getUID();
        rm.updateRule((Rule) element);
        Object newElement = get(id);
        notifyListenersAboutUpdatedElement(element, newElement);
        return newElement;
    }

    @Override
    public Object get(Object key) {
        String ruleUID = (String) key;
        Rule r = rm.getRule(ruleUID);
        return r;
    }

    @Override
    public void add(Object element) {
        rm.addRule((Rule) element);
        notifyListenersAboutAddedElement(element);
    }

    @Override
    public Object remove(Object key) {
        String ruleUID = (String) key;
        Rule r = rm.getRule(ruleUID);
        rm.removeRule(ruleUID);
        notifyListenersAboutRemovedElement(r);
        ;
        return r;
    }

}
