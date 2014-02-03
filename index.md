---
layout: default
title: Savant Build
---

# Savant Build System
Savant is a complete build and dependency management system built entirely from Scratch. It doesn't use Maven, Ivy or Ant under the hoods and provides a entirely new way of thinking about your builds.

# A Little Something Different
Unlike Ant, Savant has plugins. Unlike Maven and Gradle, Savant plugins don't provide build targets. Instead, Savant plugins provide functionality that you can use in your targets. This solves one of the main pain points of other build systems.

To explain this better, let's look at an extremely simple build file:

```
import 'testng-plugin'
```

In most build systems, this would add the target **test** to your project. You could then execute this target from the command-line like this:

```
$ build test
```

Let's now say that your project needs to first create a database and load it up with some data before you run your tests. How do you do this?

Well, in some build tools you can do something like this:

```
import 'testng-plugin'

test.doBefore << {
  setupDatabase()
}
```

So far so good right? Well, what happens when you want to first setup the database, then run one test set, then reset the database, then run another test set? Things become much more difficult because you don't have access to the internals of the plugin and can't easily inject code in the middle of its processing.

Another example where traditional plugin systems break down is target dependencies. The issue here is that the **testng** plugin can't run until the **java** plugin has run. Specifically, the **java** plugin's compile target needs to be called first. That means that the **testng** plugin needs to depend either directly on the **java** plugin or depend on there being a target named **compile**.

Savant is completely different. It is a hybrid approach that incorporates concepts from Ant tasks, but also provides the power of Groovy and a complete dependency management system (even plugins are dependencies).

# Getting Started

First, you need to follow these steps:

* Download the Savant bundle from http://savant.inversoft.org/savant-0.1.0.tar.gz
* Extract this bundle somewhere on disk
* Put the **bin** directory from the bundle in the your PATH
* Download and install JDK 8 (Savant requires JDK 8 to run but you can compile with any JDK include JDK 1, 1.1, 1.2, 1.3, 4, 5, 6, 7 or 8)
* Make sure the JDK 8 java executable is also in your PATH

# Executing Savant

Now you should be able to invoke Savant like this:

```
$ sb
```

If you aren't in a project directory that contains a **build.savant** build file, you will get an error like this:

```
Build file [build.savant] is missing or not readable.
```

# Basic Savant Build File

Let's start out by looking at a simple Savant build file that defines a project. Edit the file build.savant in your IDE or favorite text editor. Put this in your build file.

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
}
```

You define your project details using the project method. This method takes a block, that allows you to define additional details about your project like dependencies. The key here is that the project requires these attributes:

* group - The group identifier for your organization (usually a reverse domain name)
* name - The name of the project
* version - A semantic version http://semver.org
* license - The license of your project

One of the most important details is that Savant requires that everything uses Semantic Versions. This is vitally important when Savant calculates dependencies of your project. You should read the Semantic Version specification by visiting http://semver.org.

You also need to specify a license for your project. All projects must provide a license. This helps Savant determine if your project is in compliance with organizational requirements about software licensing.

# Dependencies

Next, let's add some dependencies to the project

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}
```

This defines a single compile-time dependency on the Commons Collections library version 3.1.0. This isn't enough information for Savant to download this dependency. You need to tell Savant where to download from. This is done using a workflow definition:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    fetch {
      cache()
      url(url: "http://savant.inversoft.org")
    }
    publish {
      cache()
    }
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}
```

This tells Savant to first check the local cache for dependencies. If they aren't found there, download them from http://savant.inversoft.org and cache them locally (that's what the publish section is for).

You can simplify this build file like this:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}
```

The local cache for Savant is stored at ~/.savant/cache.

We now have given Savant enough information to download the dependency and cache it locally.

# Targets

Our Savant build file still doesn't do anything, let's add some targets.

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}

target(name: "clean", description: "Cleans out the build directory") {
  ...
}

target(name: "compile", description: "Compiles the project") {
  ...
}

target(name: "test", description: "Executes the projects tests", dependsOn: ["compile"]) {
  ...
}
```

This is a fairly common build file that includes targets to clean the project, compile the project and run the tests. Notice that the **test** target depends on the **compile** target.

## Target Dependencies

Target dependencies are an ordered list. This means that Savant will ensure that dependent targets are executed in the order they are defined. For example:

```
target(name: "one") {
  output.info("One")
}

target(name: "two") {
  output.info("Two")
}

target(name: "three", dependsOn: ["one", "two"]) {
  output.info("Three")
}
```

If we run the build like this:

```
$ sb three
```

Savant guarantees that the output will always be:

```
One
Two
Three
```

# Plugins

At this point, you have a functional build file. However, it doesn't do very much. You could start writing your build using Groovy code directly inside the targets. This would require quite a bit of work though. Instead of writing this code yourself, you can leverage plugins to fill out your build.

Let's start with the basics of compiling Java source files. To accomplish this task, we will include the Java plugin and assign it to a variable. This will look like this:

```
java = loadPlugin(id: "org.savantbuild.plugin:java:0.1.0")
```

This code has to be put after the project and workflow definition because Savant uses the workflow to download and instantiate the plugin. The key to Savant's plugin mechanism is that Plugins are simply Groovy objects. They aren't abstracted in any way. That means after this line of code executes, the variable **java** will be in instance of the class org.savantbuild.plugin.java.JavaPlugin. Any public methods or fields on that instance can be invoked to perform parts of your build.

For the Java plugin, you need to define the version of the JDK to compile with (in case you have multiple JDKs installed or need to compile with a JDK besides JDK 8 which Savant requires to run).

```
java = loadPlugin(id: "org.savantbuild.plugin:java:0.1.0")
java.settings.version = "1.7"
```

Finally, you need to create a special configuration file for the Java plugin that tells Savant the location of the JDK. Create the file ~/.savant/plugins/org.savantbuild.plugin.java.properties and put the full path to the JDK base directory in it like this:

```
1.7=/Library/Java/JavaVirtualMachines/jdk1.7.0_10.jdk/Contents/Home
```

Now that you have loaded and configured the Java plugin, you can update your targets to use it. Plugins are simple Java objects and the public methods define their features. The public fields are often used to configure the plugin. Here's the build file again with the Java plugin calls added:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}

java = loadPlugin(id: "org.savantbuild.plugin:java:0.1.0")
java.settings.version = "1.7"

target(name: "clean", description: "Cleans out the build directory") {
  java.clean()
}

target(name: "compile", description: "Compiles the project") {
  java.compileMain()
  java.compileTest()
}

target(name: "test", description: "Executes the projects tests", dependsOn: ["compile"]) {
  ...
}
```

Most plugins provide detailed instructions on how to configure them at runtime. If you run Savant without the proper configuration, you should get a nice error message that tells you exactly how to configure the plugin to work properly.

# Java Plugin

The Java plugin provides these main features:

* Compile Java source files
* Create JARs
* Clean the build directory

The Java plugin is also configurable via the **org.savantbuild.plugin.java.JavaSettings** class. This class contains a number of public fields that control how the Java plugin works. For example, if you want to change command-line parameters passed to **javac**, you can control them like this:

```
java.settings.compilerArguments = "-nowarn"
```

The layout of a Java project that the Java plugin can work with is configurable, but the standard layout is as follows:

* **src/main/java** - The location of the main Java source files
* **src/main/resources** - The location of the main resource files
* **src/test/java** - The location of the test Java source files
* **src/test/resources** - The location of the test resource files
* **build/classes/main** - The output directory where the Java files in **src/main/java** are compiled to and the resources from **src/main/resources** are copied to
* **build/classes/test** - The output directory where the Java files in **src/test/java** are compiled to and the resources from **src/test/resources** are copied to
* **build/jars** - The output directory where the JAR files are created in

The layout of the Java project is controlled by the **org.savantbuild.plugin.java.JavaLayout** class that is a field on the **org.savantbuild.plugin.java.JavaPlugin** class. If you wanted to change the location of your main Java source files, you could do that like this:

```
java.layout.mainSourceDirectory = Paths.get("src/main/java-files")
```

**NOTE:** Most of the fields in the **JavaLayout** class are java.nio.file.Path instances.

## Clean

The **clean** method deletes everything inside the **build** directory and the **build** directory itself.

## Compile

The Java plugin provides two compile methods:

* compileMain - This compiles the main Java source files
* compileTest - This compiles the test Java source files

## JAR

The Java plugin's **jar** method creates 4 JAR files:

1. A JAR file that contains the compiled Java classes from the **build/classes/main** directory and the resources from the **src/main/resources** directory
2. A JAR file that contains the Java source files from the **src/main/java** directory and the resources from the **src/main/resources** directory
3. A JAR file that contains the compiled Java test classes from the **build/classes/test** directory and the resources from the **src/test/resources** directory
4. A JAR file that contains the Java test source files from the **src/test/java** directory and the resources from the **src/test/resources** directory

# Publications

All project's produce some type of final result. This might be a JAR, WAR, ZIP, or various files. Savant manages these publications in the build file and you can define any number of publications.

Publications are put into groups. Groups can have arbitrary names. However, some plugins make use of certain groups. For example, the TestNG plugin uses the **test** publications when running the tests.

You can define a publication groups and publications like this:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }

  publications {
    main {
      publication(name: "my-project", type: "jar", file: "build/jars/my-project-${project.version}.jar", sourceFile: "build/jars/my-project-${project.version}-src.jar")
    }
    test {
      publication(name: "my-project-test", type: "jar", file: "build/jars/my-project-test-${project.version}.jar", sourceFile: "build/jars/my-project-test-${project.version}-src.jar")
    }
    someOtherType {
      ...
    }
  }
}
```

Rather than require each project build file to define these same publications, Savant provides a helper method that will add these two publications (main and test). You can invoke this shortcut like this:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }

  publications {
    standard()
  }
}
```

# Final Example

Here's a final example that includes most of the common plugins and targets for a Java project:

```
project(group: "org.example", name: "my-project", version: "1.0", license: "Apachev2") {
  workflow {
    standard()
  }

  dependencies {
    group(type: "compile") {
      dependency(id: "org.apache.commons:commons-collections:3.1.0")
    }
  }
}

// Load plugins
java = loadPlugin(id: "org.savantbuild.plugin:java:0.1.0")
testng = loadPlugin(id: "org.savantbuild.plugin:java-testng:0.1.0")
dependency = loadPlugin(id: "org.savantbuild.plugin:dependency:0.1.0")
release = loadPlugin(id: "org.savantbuild.plugin:release-git:0.1.0")

// Config
java.settings.version = "1.7"
testng.settings.javaVersion = "1.7"

target(name: "clean", description: "Cleans out the build directory") {
  java.clean()
}

target(name: "compile", description: "Compiles the project") {
  java.compileMain()
  java.compileTest()
}

target(name: "jar", description: "Creates all of the standard JAR files for a Java project", dependsOn: ["compile"]) {
  java.jar()
}

target(name: "test", description: "Executes the projects tests using TestNG", dependsOn: ["jar"]) {
  testng.test()
}

target(name: "int", description: "Publishes an integration build of the project to the local cache", dependsOn: ["test"]) {
  dependency.integrate()
}

target(name: "release", description: "Performs a full release of the project", dependsOn: ["test"]) {
  release.release()
}
```

# Integration Builds

Savant uses a slightly different approach than other build systems. When you run an **install** build in other build systems, it uses the version you defined on the project, even if that version is something like **1.0.8**. The problem with this approach is that you'll end up with a JAR file named my-project-1.0.8.jar even though it isn't actually the final release of version **1.0.8**. In other build systems, people often change the version to something like **1.0.8-SNAPSHOT** to indicate that the version is not a final release.

Savant doesn't work this way. Whatever version you have in your project build file Savant will use. However, it adds the indicator **-{integration}** to the end of the version to indicate that it is an integration build. It uses this version (**1.0.8-{integration}**) until you perform a full release of the project. At this point, Savant drops the **-{integration}** indicator and uses the version you defined.

When you have multiple projects that depend on each other, it is useful to configure them to use **-{integration}** versions of each other like this:

```
dependencies {
  group(type: "compile") {
    dependency(id: "org.example:my-other-project:1.0.8-{integration}")
  }
}
```

Savant also enforces the rule that you cannot perform a full release of a project that has dependencies on integration builds of other projects.

As you might have noticed, the **Dependency** plugin provides the ability to publish integration builds of your project using the **integrate** method.

# Full Releases

In addition, the **Release Git** plugin provides the ability to perform a full release of a project that is stored in a Git repository. This plugin provides the method **release** that performs these steps:

1. Check for dependencies on integration builds of other projects, libraries, etc.
2. Check for plugins that are integration builds
3. Ensure the project is a Git project
4. Perform a **git pull**
5. Ensure the project has no local changes
6. Ensure the project changes have been pushed to the remote
7. Ensure there isn't a tag in the Git repository for the version being released
8. Creates a tag whose name is the version being released (i.e. 1.0.8)
9. Publishes the project's artifacts (publications) using the publishWorkflow of the project