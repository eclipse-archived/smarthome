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


/**
 * @author Ana Dimova
 *
 */
public abstract class AutomationCommand {

  static final String                   SUCCESS   = "SUCCESS";
  static final String                   FAIL      = "FAIL";

  protected static final String         OPTION_ST = "-st";

  protected String                      parsingResult;

  protected int                         adminType;
  protected String                      command;
  protected boolean                     st        = false;    // print stack trace option
  protected AutomationCommandsPluggable autoCommands;

  public AutomationCommand(String command, String[] params, int adminType, AutomationCommandsPluggable autoCommands) {
    this.command = command;
    this.adminType = adminType;
    this.autoCommands = autoCommands;
    parsingResult = parseOptionsAndParameters(params);
  }

  /**
   *
   */
  public abstract String execute();

  /**
   * 
   * @param writer
   * @param params
   * @param options
   * @return
   */
  protected abstract String parseOptionsAndParameters(String[] params);

}
