package org.eclipse.smarthome.automation.commands;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleStatus;

public class AutomationCommandEnableRule extends AutomationCommand {

    private boolean enable;
    private boolean hasEnable;
    private String uid;

    public AutomationCommandEnableRule(String command, String[] parameterValues, int providerType,
            AutomationCommandsPluggable autoCommands) {
        super(command, parameterValues, providerType, autoCommands);
    }

    @Override
    public String execute() {
        if (parsingResult != SUCCESS) {
            return parsingResult;
        }
        if (hasEnable) {
            autoCommands.setEnabled(uid, enable);
            return SUCCESS;
        } else {
            RuleStatus status = autoCommands.getRuleStatus(uid);
            if (status != null)
                return Printer.printRuleStatus(uid, status);
        }
        return FAIL;
    }

    @Override
    protected String parseOptionsAndParameters(String[] parameterValues) {
        for (int i = 0; i < parameterValues.length; i++) {
            if (null == parameterValues[i]) {
                continue;
            }
            if (parameterValues[i].charAt(0) == '-') {
                if (parameterValues[i].equals(OPTION_ST)) {
                    st = true;
                    continue;
                }
                return String.format("[Automation Commands : Command \"%s\"] Unsupported option: %s", command,
                        parameterValues[i]);
            }
            if (uid == null) {
                Rule rule = autoCommands.getRule(parameterValues[i]);
                if (rule != null) {
                    uid = rule.getUID();
                    continue;
                }
            }
            getEnable(parameterValues[i]);
            if (hasEnable)
                continue;
            if (uid == null)
                return String.format("[Automation Commands : Command \"%s\"] Missing required parameter: Rule UID",
                        command);
            return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command,
                    parameterValues[i]);
        }
        return SUCCESS;
    }

    private void getEnable(String string) {
        if (string.equals("true")) {
            enable = true;
            hasEnable = true;
        } else if (string.equals("false")) {
            enable = false;
            hasEnable = true;
        } else {
            hasEnable = false;
        }
    }

}
