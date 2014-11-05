# Setting up a development environment

_Note:_ We currently provide a [Yoxos profile](https://github.com/eclipse/smarthome/blob/master/targetplatform/Eclipse%20SmartHome.yoxos) to get you started with an appropriate Eclipse IDE. Unfortunately, it will require you to also manually install [Groovy Eclipse](http://groovy.codehaus.org/Eclipse+Plugin).

For the future, we plan to integrate Eclipse SmartHome in [Project Oomph](http://wiki.eclipse.org/Eclipse_Oomph_Installer), which should provide an even easier setup.

Ensure you have installed the following prerequisites:

1. Git
1. Maven 3.x
1. Oracle JDK 7
1. [Yoxos Installer](https://yoxos.eclipsesource.com/downloadlauncher.html)

Here are the steps to follow for a successful setup:

1. Create a local clone of the repository by running "git clone https://github.com/eclipse/smarthome.git" in a suitable folder.
1. Download and execute the file [Eclipse SmartHome.yoxos](https://github.com/eclipse/smarthome/blob/master/targetplatform/Eclipse%20SmartHome.yoxos). Alternatively, you can install the required plugins on top of an existing Eclipse 4.4 installation using this [update site](http://yoxos.eclipsesource.com/userdata/profile/09528bbc51589d837ad95c819fdac25b).
1. Install [Groovy Eclipse](http://groovy.codehaus.org/Eclipse+Plugin) in your Eclipse IDE.
1. Create a new workspace.
1. Choose File->Import->General->Existing Projects into Workspace, enter your clone repository directory as the root directory and press "Finish".
1. After the import is done, you have to select the target platform by selecting Window->Preferences->Plug-in Development->Target Platform->smarthome from the main menu. Ignore compilation problems at this step.
1. Now you need to run code generation for a few parts of Eclipse SmartHome. To do so, go to the project org.eclipse.smarthome.model.codegen and run the prepared launch files. For each .launch file, select "Run As->x Generate abc Model" from the context menu. Please follow the order given by the numbers. On the very first code generation, you are asked in the console to download an ANTLR file, answer with "y" and press enter on the console.
1. All your project in the workspace should now correctly compile without errors. If you still see error markers, try a "clean" on the concerned projects. If there are still errors, it could be that you use JDK 1.6 instead of JDK 1.7.
