package org.eclipse.smarthome.automation;

public interface StatusInfoCallback {

    void statusInfoChanged(String ruleUID, RuleStatusInfo statusInfo);
}
