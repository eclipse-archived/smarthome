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

package org.eclipse.smarthome.automation.sample.handler.factories;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.AbstractConsoleCommandExtension;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Text console commands to list and execute the created sample trigger handlers
 *
 * @author Ana Dimova - Initial Contribution
 */
public class SampleHandlerFactoryCommands extends AbstractConsoleCommandExtension {

    private static final String CMD = "autotype";
    private static final String DESC = "Automation Sample Spi Factory Management";

    private static final String COMMAND_LIST = "listTrigger";
    private static final String COMMAND_EXECUTE = "executeTrigger";

    private List<SampleTriggerHandler> currentTriggers;
    private SampleHandlerFactory sampleSpiFactory;
    private BundleContext bc;
    private ServiceRegistration commandsServiceReg;

    public SampleHandlerFactoryCommands(SampleHandlerFactory sampleSpiFactory, BundleContext bc) {
        super(CMD, DESC);
        this.sampleSpiFactory = sampleSpiFactory;
        this.bc = bc;
        commandsServiceReg = bc.registerService(ConsoleCommandExtension.class.getName(), this, null);
    }

    @Override
    public void execute(String[] args, Console console) {
        String command = args[0];

        String[] params = new String[args.length - 1];// extract the remaining arguments except the first one
        if (params.length > 0) {
            System.arraycopy(args, 1, params, 0, params.length);
        }

        if (COMMAND_LIST.equalsIgnoreCase(command) || "ls".equalsIgnoreCase(command)) {
            listTriggerSpi(params, console);
        } else if (COMMAND_EXECUTE.equalsIgnoreCase(command) || "ex".equalsIgnoreCase(command)) {
            executeTriggerSpi(params, console);
        }
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(COMMAND_LIST, "List all created TriggerSpi"),
                buildCommandUsage(COMMAND_EXECUTE, "Executes specific TriggerSpi by its index.") });
    }

    private void listTriggerSpi(String[] params, Console console) {
        console.println("ID                             Name");
        currentTriggers = sampleSpiFactory.getCreatedTriggerHandler();
        if (currentTriggers.size() > 0) {
            for (int i = 0; i < currentTriggers.size(); i++) {
                console.print(Integer.toString(i + 1));
                console.print("                            ");
                console.println(currentTriggers.get(i).getTriggerID());
            }
        } else {
            console.println("No created TriggerHandler. List is Empty");
        }

    }

    private void executeTriggerSpi(String[] params, Console console) {
        if (params.length >= 1) {
            int index = Integer.parseInt(params[0]);
            String param = null;
            if (currentTriggers.size() >= index) {
                SampleTriggerHandler triggerSpi = currentTriggers.get(index - 1);
                if (params.length >= 2) {
                    param = params[1];
                }
                triggerSpi.trigger(param);
            }
        }
    }

    public void stop() {
        commandsServiceReg.unregister();
    }

}
