package org.eclipse.smarthome.automation.commands;

import java.net.URL;
import java.util.List;

import org.osgi.framework.BundleContext;

public class PersistentRuleImporter extends RuleImporterImpl<List<String>> {

    public PersistentRuleImporter(BundleContext context) {
        super(context, PersistentRuleImporter.class);
    }

    @Override
    protected String getKey(URL element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getStorageName() {
        return "commands_rules";
    }

    @Override
    protected String keyToString(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected URL toElement(String key, List<String> persistableElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<String> toPersistableElement(URL element) {
        // TODO Auto-generated method stub
        return null;
    }


}
