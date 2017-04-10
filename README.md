**Build Status** [![Travis Build Status](https://travis-ci.org/paissad/soapui-multi-testrunner.svg?branch=master)](https://travis-ci.org/paissad/soapui-multi-testrunner)

**Quality Gate** <a href="https://sonarqube.com/dashboard/index/net.paissad.tools:soapui-multi-testrunner"><img alt="SoapUI Multi Runner Sonar Quality Gate" src="https://www.sonarqube.org/assets/logo-31ad3115b1b4b120f3d1efd63e6b13ac9f1f89437f0cf6881cc4d8b5603a52b4.svg" width="100px"></a>

----------

Table of contents

[TOC]


#About

`SoapUI Multi Testrunner` is a tool built with Java which permits the execution of multiple SoapUI projects. Though, it relies on the genuine `SoapUI` testrunner.
With the genuine `SoapUI` software, it is not possible to execute multiple projects with th `testrunner` script.
`Soapui Multi Testrunner` was built to fulfill that task.
It also reduces the configuration needed to run multiple projects from one command line, based on conventions and hierarchization

#Downloads
Coming soon ! For now, just do a checkout of the project and build it with `mvn clean package`

#Source

The latest source code can be found [here on GitHub](https://github.com/paissad/soapui-multi-testrunner "soapui-multi-testrunner"). Feel free to fork and/or contribute.

#How to use

Run the following command to see the help:

    java -jar soapui-multi-runner-x.y.z.jar --help

Run the following command to execute all projects located into a specified directory

    java -jar soapui-multi-runner-x.y.z.jar 
        --trp /path/to/soapui/testrunner.sh
        --in /path/to/projects
        --out /path/to/results

The directory `settings/` and all its contents are optional.
If the file `settings/system.properties` exits, it will be used/shared by all executed projects.
If the file `settings/global.properties` exits, it will be used/shared by all executed projects.

Every single project can declare its own project properties by creating a file `foobar.project.properties` where `foobar`is the name of the SoapUI project.


Every single project can declare its system and/or global properties by creating a file `foobar.system.properties` or `foobar.global.properties` where `foobar` is the name of the SoapUI project.

#Bug reports

It is possible to file bugs or enhancement requests here in the [GitHub Issues Page](https://github.com/paissad/soapui-multi-testrunner/issues "Github Issues").
 

#License

`Soapui Multi Testrunner` is licensed under the [LGPL v3 license](https://raw.githubusercontent.com/paissad/soapui-multi-testrunner/master/LICENSE "License"). 

`SoapUI` is licensed under the [EUPL, Version 1.1 license](https://raw.githubusercontent.com/SmartBear/soapui/next/LICENSE.md "SoapUI License").