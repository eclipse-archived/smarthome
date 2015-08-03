---
layout: documentation
---

{% include base.html %}

# How To Become Part of the Community

## Getting Involved

There are several ways of how to get in contact with the community in order to ask questions or discuss ideas:

## Discussion Forum

The forum is used to discuss ideas and to answer general questions. It is organized per topic, so you can easily decide what is of interest for you.

* [Discussion Forum](http://eclipse.org/forums/eclipse.smarthome)

## Bugtracker

Like all Eclipse projects, we use Eclipse Bugzilla as an issue tracking system.

* [Eclipse SmartHome Issues](https://bugs.eclipse.org/bugs/buglist.cgi?list_id=11615800&product=SmartHome)

If you have found a bug or if you would like to propose a new feature, please feel free to enter an issue. But before creating a new bug, please first check if the bug not already exists.

### Issue Tracker Links

* [Create a new Bug](https://bugs.eclipse.org/bugs/enter_bug.cgi?product=SmartHome)
* [All Issues](https://bugs.eclipse.org/bugs/buglist.cgi?list_id=11615800&product=SmartHome)
* [Open Bugs](https://bugs.eclipse.org/bugs/buglist.cgi?bug_severity=blocker&bug_severity=critical&bug_severity=major&bug_severity=normal&bug_severity=minor&bug_severity=trivial&classification=IoT&list_id=11613387&product=SmartHome&query_format=advanced&resolution=---)
* [Open Features](https://bugs.eclipse.org/bugs/buglist.cgi?bug_severity=enhancement&classification=IoT&list_id=11613436&product=SmartHome&query_format=advanced&resolution=---)
* [New/Unconfirmed Issues](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=UNCONFIRMED&classification=IoT&list_id=11615992&product=SmartHome&query_format=advanced)
 
### Issue Fields

For each issue you need to provide the following fields:

| Field       | Description |
|-------------|-------------|
| Summary     | Summary of the bug/feature
| Description | Detailed description of the bug/feature
| Component   | Affected component
| Version     | For bugs choose affected version, for features use **unspecified** 
| Severity    | Use the severity **enhancement** for feature requests and any other severity for bugs
| Hardware/OS | Usually not relevant. Should be set to All/All.

### Bug vs. Feature

Yes, we know that it is a fundamental question, if something is considered as bug or feature. But you need to decide it when you create a bug. We consider something as a bug, if an already existing functionality is not working as expected. 

Please use the severity **enhancement** for feature requests and any other severity for bugs.

### Components

When creating a new bug, please choose the affected component. The following components are defined:

| Component      | Description |
|----------------|-------------|
| Automation	 | Bugs and Features related to Automation and Rule Engine
| Binding	     | Bugs and features related to Bindings
| Core	         | Bugs and Features related to the Core (Item API, Thing API, Config API)
| Designer	     | Bugs and Features related to the Designer
| DSL	         | Bugs and Features related to the Domain Specific Languages (Items, Things, Sitemaps, Rules)
| Infrastructure | Bugs and Features related to the Infrastructure (Build server, Maven, Tooling)
| REST/SSE	     | Bugs and Features related to the REST interface and SSE event mechanism
| UI	         | Bugs and Features related to the User Interface (Classic UI, Charting, Servlets)

# Code Contributions

If you want to become a contributor to the project, please check our guidelines first:

## Pull requests are always welcome

We are always thrilled to receive pull requests, and do our best to process them as fast as possible. Not sure if that typo is worth a pull request? Do it! We will appreciate it.

If your pull request is not accepted on the first try, don't be discouraged! If there's a problem with the implementation, hopefully you received feedback on what to improve.

We're trying very hard to keep Eclipse SmartHome lean and focused. We don't want it to do everything for everybody. This means that we might decide against incorporating a new feature. However, there might be a way to implement that feature on top of Eclipse SmartHome.

## Discuss your design in the discussion forum

We recommend discussing your plans in the [Discussion Forum](https://www.eclipse.org/forums/eclipse.smarthome) before starting to code - especially for more ambitious contributions. This gives other contributors a chance to point you in the right direction, give feedback on your design, and maybe point out if someone else is working on the same thing.

## Conventions for pull requests

* Submit unit tests for your changes. Eclipse SmartHome has a great test framework built in; use it! Take a look at existing tests for inspiration. Run the full test suite on your branch before submitting a pull request.
* Update the documentation when creating or modifying features. Test your documentation changes for clarity, concision, and correctness, as well as a clean documentation build.
* Write clean code. Universally formatted code promotes ease of writing, reading, and maintenance. Check our [Coding Guidelines](../development/guidelines.html).
* Pull requests descriptions should be as clear as possible and include a reference to all the issues that they address.
* Pull requests must not contain commits from other users or branches.

The process to create a pull request is then the following:

1. Create an account at Eclipse if you do not have one yet.
1. Sign the Contributor License Agreement (CLA).
1. Fork the sources of Eclipse SmartHome on GitHub.
1. Do the coding!
1. Make sure your code applies to the [Coding Guidelines](../development/guidelines.html)
1. Make sure there is a [Bugzilla issue](https://bugs.eclipse.org/bugs/buglist.cgi?list_id=11615800&product=SmartHome) for your contribution. If it does not exist yet, [create one](https://bugs.eclipse.org/bugs/enter_bug.cgi?product=SmartHome).
1. Add a "Signed-off-by" line and a ["Bug"](https://bugs.eclipse.org/bugs/buglist.cgi?list_id=11615800&product=SmartHome) line to every commit you do - see the [Eclipse wiki](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git) for details.
1. Create a pull request, referencing the Bugzilla issue number