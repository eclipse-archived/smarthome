/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.commands;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.handler.parser.Status;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.CompositeActionType;
import org.eclipse.smarthome.automation.type.CompositeConditionType;
import org.eclipse.smarthome.automation.type.CompositeTriggerType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.TriggerType;

/**
 * @author Ana Dimova
 *
 */
public class AutomationCommandExport extends AutomationCommand {

  private static final String OPTION_P   = "-p";

  private String              parserType = "json"; // parser type
  private File                file;               // output file

  private Locale locale;

  /**
   * 
   * @param command
   * @param params
   * @param adminType 
   * @param options
   */
  public AutomationCommandExport(String command, String[] params, int adminType, AutomationCommandsPluggable autoCommands) {
    super(command, params, adminType, autoCommands);
  }

  /**
   * @see org.eclipse.smarthome.automation.commands.AutomationCommand#execute()
   */
  @Override
  public String execute() {
    if (parsingResult != SUCCESS) {
      return parsingResult;
    }
    Set set = new LinkedHashSet();
    switch (adminType) {
      case AutomationCommands.MODULE_TYPE_ADMIN:
        set.addAll(autoCommands.getModuleTypes(TriggerType.class, locale));
        set.addAll(autoCommands.getModuleTypes(CompositeTriggerType.class, locale));
        set.addAll(autoCommands.getModuleTypes(ConditionType.class, locale));
        set.addAll(autoCommands.getModuleTypes(CompositeConditionType.class, locale));
        set.addAll(autoCommands.getModuleTypes(ActionType.class, locale));
        set.addAll(autoCommands.getModuleTypes(CompositeActionType.class, locale));
        Status s = autoCommands.exportModuleTypes(parserType, set, file);
        if (set.isEmpty()) {
          return String.format("[Automation Commands : Command \"%s\"] There are no ModuleTypes available!", command);
        }
        if (s.hasErrors()) {
          return s.toString();
        }
        return SUCCESS;
      case AutomationCommands.TEMPLATE_ADMIN:
        set.addAll(autoCommands.getTemplates(locale));
        s = autoCommands.exportTemplates(parserType, set, file);
        if (set.isEmpty()) {
          return String.format("[Automation Commands : Command \"%s\"] There are no Templates available!", command);
        }
        if (s.hasErrors()) {
          return s.toString();
        }
        return SUCCESS;
      case AutomationCommands.RULE_ADMIN:
        set.addAll(autoCommands.getRules(null));
        s = autoCommands.exportRules(parserType, set, file);
        if (set.isEmpty()) {
          return String.format("[Automation Commands : Command \"%s\"] There are no Rules available!", command);
        }
        if (s.hasErrors()) {
          return s.toString();
        }
        return SUCCESS;
    }
    return FAIL;
  }

  /**
   * @param command
   * @param string
   * @param writer
   * @return
   */
  private File initFile(String command, String string) {
    File f = new File(string);
    File parent = f.getParentFile();
    if (!parent.isDirectory() && !parent.mkdirs()) {
      return null;
    }
    return f;
  }

  /**
   * @see org.eclipse.smarthome.automation.commands.AutomationCommand#parseOptionsAndParameters(PrintStream, String[])
   */
  @Override
  protected String parseOptionsAndParameters(String[] params) {
    String command = this.command;
    boolean getFile = true;
    for (int i = 0; i < params.length; i++) {
      if (null == params[i]) {
        continue;
      }
      if (params[i].equals(OPTION_ST)) {
        st = true;
      }
      else if (params[i].equalsIgnoreCase(OPTION_P)) {
        i++;
        if (i >= params.length) {
          return String.format(
                  "[Automation Commands : Command \"%s\"] The option [%s] should be followed by value for the parser type.",
                  command, OPTION_P);
        }
        parserType = params[i];
      }
      else if (params[i].charAt(0) == '-') {
        return String.format("[Automation Commands : Command \"{0}\"] Unsupported option: {1}", command, params[i]);
      }
      else if (getFile) {
        file = initFile(command, params[i]);
        if (file != null) {
          getFile = false;
        }
      }
      else {
        return String.format("[Automation Commands : Command \"%s\"] Unsupported parameter: %s", command, params[i]);
      }
    }
    if (getFile) {
      return String.format("[Automation Commands : Command \"%s\"] Missing destination file parameter!", command);
    }
    return SUCCESS;
  }

}
