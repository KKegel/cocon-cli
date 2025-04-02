# cocon-cli

*cocon-cli* is a minimalistic command line interface for interacting with the *coconlib* library,
Please check the [coconlib documentation](https://github.com/KKegel/coconlib) for more information about the purpose and internals of the library.

With *cocon-cli* you can:
- Create a new cocon workspace descriptor
- Edit the workspace content (CRUD coconlib operations)
  - Register revisions (local paths)
  - Add revision relations
  - Query the workspace content
  - Assemble context files in joint directories

## Contributing

This project is open source and we welcome contributions from the community. 
The project is licensed under the Apache License 2.0. 
The initial version of this project was developed as part of the research project CONVIDE at Dresden University of Technology, Germany by Karl Kegel. 
Please see the [CONVIDE project](https://www.sfb1608.kit.edu/) for more information.

## About

A cocon workspace describes a collection of revisions and their relations.
A revision can be any versioned "thing" (e.g. a file, a directory, a git repository, etc.) that is identified by a unique identifier and
addressable by a local file path.
A revision relation describes a relation between two revisions.
Possible relations are *successor* (a newer version of an existing revision) and *unification* (the merge of two revisions).

The *cocon-cli* allows you to interact with the workspace via CLI commands.
Each command accesses and potentially modifies the workspace descriptor.
The workspace descriptor is stored as a ``.cocon`` file.

## User Guide

### Installation

The *cocon-cli* is a Kotlin JVM application built for Java 21 and can be run on any system with a Java Runtime Environment (JRE) installed.
The application can be:
- custom-built from source using the provided Gradle build script
- downloaded as prebuilt package from our CI pipeline
- used via the provided shell script

#### Download Prebuilt Package
You can download the prebuilt package from our CI pipeline. The package is a zip file containing the application and all its dependencies.
Within the zip file, you will find executables inside the `bin` directory. We recommend creating 
a `cocon` alias in your shell configuration file (e.g. `.bashrc`, `.zshrc`, etc.) to make it easier to run the application:
```bash
cocon <command> [options]
```

#### Shell Script
This directory contains a shell script that can be used to run all commands.
The shell script must be executed from within this directory and is meant for testing and development purposes only.
The script checks if a successful build of the application exists in the `build/` directory and runs the application with the provided arguments.
Otherwise, the script automatically builds the application and runs it.


#### General Remarks
* When specifying ids or paths, refrain from using trailing or leading spaces.
* No identifier, path or property must contain `;` and `,` as these are used as delimiters in the workspace descriptor.
* Do not use newlines in identifiers, paths or properties.
* *cocon-cli* will abort any operation that it considers an invalid operation (leading to an invalid workspace descriptor).
* It may still be possible to corrupt the workspace descriptor intentionally. *cocon-cli* will not handle these cases.
* Typical rules for CLI interactions, e.g., quoting, escaping, etc. apply.

#### Command Overview
```bash
# basic commands
cocon init # create a new workspace descriptor
cocon init-subsys <subsystem-name> # create a new subsystem
cocon rm-subsys <subsystem-name> # remove a subsystem
cocon ls-subsys # list all subsystems

# revisions
cocon add-rev <subsystem-name> <revision-id> <local-path> -p1 <predecessor-id> -p2 <predecessor-id> # add a revision
cocon rm-rev <revision-id> # remove a revision
cocon ls-rev <revision-id> # list a revision
cocon ls-revs <subsystem-name> # list all revisions in a subsystem

# successor / unification relations
cocon ls-rev-rel <revision-id> # list all relations of a revision
cocon ls-rel <subsystem-name> # list all relations in a subsystem

# cross-subsystem relations
cocon add-crel <revision-id> <revision-id> # add a cross-subsystem relation
cocon rm-crel <revision-id> <revision-id> # remove a cross-subsystem relation

# projections
cocon add-proj <projection-name> <revision-id> <revision-id> ... # add a projection
cocon rm-proj <projection-name> # remove a projection

# context queries
cocon query-time <revision-id> <bound> # query the time context
cocon query-space <revision-id> <bound> # query the space context
cocon query-rel <revision-id> # query the relational context
cocon query-proj <revision-id> # query the projectional context
```

### Workspace Creation
Calling
```bash
cocon init
``` 
will create a new workspace descriptor in the current directory called ``workspace.cocon``.
The workspace can be destroyed by just deleting the file.

You should not edit the workspace descriptor manually, as this could invalidate the workspace if not done correctly.
You can rename the workspace descriptor file to any name you like, but if the name deviates from the default,
each subsequent command must contain the ``--ws <workspace>`` option to specify the workspace descriptor file.

### Creating Subsystems
Subsystems are a collection of revisions of a specific artifact and their relations.
A workspace can contain multiple subsystems, each with its own revisions and relations.
A subsystem is identified by a unique name and can be created using the command

```bash
cocon init-subsys <subsystem-name>
```
Example:
```bash
cocon init-subsys robot
```
This will create a new subsystem called `robot` in the workspace descriptor.
The subsystem will be empty and can be populated with revisions and relations.

The subsystem can be removed using the command
```bash
cocon rm-subsys <subsystem-name>
```
You can view the registered subsystems using the command
```bash
cocon ls-subsys
```

### Registering Revisions
Revisions are the core of the workspace descriptor.
A revision is identified by a unique identifier and a local file path.
**The revision id must be unique all subsystems in the workspace descriptor.**
A revision can be registered using the command
```bash
cocon add-rev <subsystem-name> <revision-id> <local-path> -p1 <predecessor-id> -p2 <predecessor-id>
```
Example:
```bash
cocon add-rev robot rev1 ./my-robot-v1
```
This will register a new revision with the id `rev1` and the local path `./my-robot-v1` in the subsystem `robot` as the root revision.
```bash
cocon add-rev robot rev2 ./my-robot-v2 -p1 rev1
```
This will register a new revision with the id `rev2` and the local path `./my-robot-v2` in the subsystem `robot` as a successor of `rev1`.
```bash
cocon add-rev robot merge-rev ./my-robot-v3 -p1 rev1 -p2 rev2
```
This will register a new revision with the id `merge-rev` and the local path `./my-robot-v3` in the subsystem `robot` as a unification of `rev1` and `rev2`.

You can remove a revision using the command. This will also delete all relations to this revision.
This will not delete the local path, only the reference in the workspace descriptor.
Deleting revisions might corrupt the workspace descriptor. 
*cocon-cli* will abort the operation if it detects a potential corruption.
```bash
cocon rm-rev <revision-id>
```

You can view the registered revisions using the command
```bash
cocon ls-revs <subsystem-name>
```

You can view a single revision using the command
```bash
cocon ls-rev <revision-id>
```


### Revision Relations
Revision relations (inside a subsystem) describe the relations between revisions.
In the current version, only successor and unification relations are supported.
These relations are automatically created when registering revisions with the `-p1` and `-p2` options.
You can view the relations of a revision using the command
```bash
cocon ls-rev-rel <revision-id>
```
You can view all relations in the subsystem using the command
```bash
cocon ls-rel <subsystem-name>
```

### Cross-Subsystem Relations
Apart from the relations between revisions in a subsystem, you can also create relations between revisions in different subsystems.
These relations are called cross-subsystem relations.
Cross-subsystem relations are directed.
These relations can be managed freely.
You can create a cross-subsystem relation using the command
```bash
cocon add-crel <revision-id> <revision-id>
```
You can remove a cross-subsystem relation using the command
```bash
cocon rm-crel <revision-id> <revision-id>
```

### Projections
Projections are a way to register joint directories of revisions.
In model-driven engineering, projections are called views.
A projection is specified by a name and a list of revisions which it is composed of.
A projection can be created using the command
```bash
cocon add-proj <projection-name> <revision-id> <revision-id> ...
```
Example:
```bash
cocon add-proj assembly robot1.v1 gripper.v3 platform.v3
```
You can remove a projection using the command
```bash
cocon rm-proj <projection-name>
```


## Quality Contexts
Quality contexts are joint sets of revisions that are used to evaluate the quality of a system.
We presented the concept of quality contexts in our paper *"A Unifying Framework of Contexts for Quality Assessment in Revision Management"* (under review) 2025.

We provide query commands for the following quality contexts:
- Bounded & Unbounded Time Context
- Bounded & Unbounded Space Context
- 1-Bounded Relational Context
- 1-Bounded Projectional Context

Each context query command can be executed with or without a target directory.
If a target directory is specified, the context will be assembled in the target directory, i.e., all revisions will be copied to the target directory for further analysis.

### Time Context
The time context is a joint set of revisions that are used to evaluate the quality of a system in terms of time from
the viewpoint of a specific revision.

You can query the time context using the command
```bash
cocon query-time <revision-id> <bound>
```
Where `bound` is the backlog in revisions. 
For unbounded time context, use `-1` as the bound.
A bound of `0` will return the revision itself.

### Space Context
The space context is a joint set of revisions that are used to evaluate the quality of a system in terms of space from
the viewpoint of a specific revision.

You can query the space context using the command
```bash
cocon query-space <revision-id> <bound>
```
Where `bound` is the backlog in revisions that span the output space. 
For unbounded space context, use `-1` as the bound.
A bound of `0` will return the revision itself.

### Relational Context
The relational context is the joint set of revisions specified by traversing the cross-subsystem relations one step.

You can query the relational context using the command
```bash
cocon query-rel <revision-id>
```

### Projectional Context
The projectional context contains a projections of a specific revision.

You can query the projectional context using the command
```bash
cocon query-proj <revision-id>
```
