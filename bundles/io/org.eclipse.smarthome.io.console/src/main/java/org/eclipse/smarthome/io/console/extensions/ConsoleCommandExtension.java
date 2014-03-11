package org.eclipse.smarthome.io.console.extensions;

import org.eclipse.smarthome.io.console.Console;

public interface ConsoleCommandExtension {

	/**
	 * @param args array which contains the console command and all its arguments
	 * @return true if the extension is able to handle the command
	 */
	boolean canHandle(String[] args);
	

	/**
	 * This method called if {@link #canHandle(String[]) canHandle} returns true.
	 * Clients are not allowed to throw exceptions. They have to write corresponding messages to the given {@link Console}
	 * @param args array which contains the console command and all its arguments
	 * @param console the console used to print
	 */
	void execute(String[] args, Console console);
	
	/**
	 * @return the help text for this extension
	 */
	String getUsage();
}
