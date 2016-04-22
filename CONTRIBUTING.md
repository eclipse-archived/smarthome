# Contributing to Eclipse SmartHome

Thanks for your interest in this project!

You can propose contributions by sending pull requests through GitHub.

And of course you can [report issues](https://github.com/eclipse/smarthome/issues).

## Legal considerations

Please read the [Eclipse Foundation policy on accepting contributions via Git](http://wiki.eclipse.org/Development_Resources/Contributing_via_Git).

Your contribution cannot be accepted unless you have an [Eclipse Foundation Contributor License Agreement](http://www.eclipse.org/legal/CLA.php) in place.

Here is the checklist for contributions to be _acceptable_:

1. [create an account at Eclipse](https://dev.eclipse.org/site_login/createaccount.php), and
2. add your GitHub user name in your account settings, and
3. [log into the projects portal](https://projects.eclipse.org/) and look for ["Eclipse CLA"](https://projects.eclipse.org/user/sign/cla), and
4. ensure that you _sign-off_ your Git commits, and
5. ensure that you use the _same_ email address as your Eclipse Foundation in commits.

## Technical considerations

Again, check that your author email in commits is the same as your Eclipse Foundation account, and make sure that you sign-off every commit (`git commit -s`).

Do not make pull requests from your `master` branch, please use topic branches instead.

When submitting code, please make every effort to follow [our coding guidelines](https://www.eclipse.org/smarthome/documentation/development/guidelines.html) in order to keep the code as homogeneous as possible.

Please provide meaningful commit messages.

Here is a sample _good_ Git commit log message:

    [666999] Quick summary

    This is a discussion of the change with details on the impact, limitations, etc.

    Write just like if you were discussing with fellows :-)

    Bug: https://github.com/eclipse/smarthome/issues/1
    Also-By: Somebody who also contributed parts of this code <foo@bar.com>
    Signed-off-by: Yourself <baz@foobar.org>

Finally, a contribution is not a good contribution unless it comes with unit tests, integration tests and
documentation.

