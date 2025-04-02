/**
 *  Copyright 2025 Karl Kegel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package main

import coconlib.context.Context
import coconlib.core.MultiRevisionSystem
import coconlib.core.TinkerRevisionGraph
import coconlib.system.SystemDescription
import picocli.CommandLine
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable
import kotlin.Int
import kotlin.system.exitProcess

val commands = listOf<String>(
    "init",
    "init-subsys",
    "rm-subsys",
    "ls-subsys",
    "add-rev",
    "rm-rev",
    "ls-rev",
    "ls-revs",
    "ls-rev-rel",
    "ls-rel",
    "add-crel",
    "rm-crel",
    "add-proj",
    "rm-proj",
    "query-time",
    "query-space",
    "query-rel",
    "query-proj"
)

const val commandList = "<init>, <init-subsys>, <rm-subsys>, <ls-subsys>, " +
        "<add-rev>, <rm-rev>, <ls-rev>, <ls-revs>, <ls-rev-rel>, " +
        "<ls-rel>, <add-crel>, <rm-crel>, <add-proj>, " +
        "<rm-proj>, <query-time>, <query-space>, " +
        "<query-rel>, <query-proj>"

@CommandLine.Command(name = "driftool", mixinStandardHelpOptions = true, version = ["v.2.0 (beta)"])
class Checksum : Callable<Int> {

    @Parameters(
        index = "0",
        paramLabel = "<command>",
        description = ["Command to execute. Available commands are: $commandList"],
    )
    var command: String = ""

    @Parameters
    lateinit var positional: List<String>

    @CommandLine.Option(
        names = ["-w", "--ws"],
        description = ["Path to the input repository. " +
                "The path must start in the input directory root. "])
    var workspace: String = "./workspace.cocon"

    @CommandLine.Option(
        names = ["-p1", "--predecessor1"],
        description = ["First predecessor of a revision. " +
                "Use this option only in combination with the add-rev command."])
    var predecessor1: String? = null

    @CommandLine.Option(
        names = ["-p2", "--predecessor2"],
        description = ["First predecessor of a revision. " +
                "Use this option only in combination with the add-rev command."])
    var predecessor2: String? = null

    @CommandLine.Option(
        names = ["-l", "--log"],
        description = ["Path to the input repository. " +
                "The path must start in the input directory root. "])
    var exceptionLog: Boolean = false

    @CommandLine.Option(
        names = ["-d", "--dir"],
        description = ["Output directory for operations that require it."])
    var dir: String? = null


    override fun call(): Int {
        try {
            assert(command.isNotEmpty())
            assert(command in commands)
            assert(positional.size > 1)
            assert(command == positional.first())
            exec(command, positional, workspace, predecessor1, predecessor2, dir)
        } catch (a: AssertionError) {
            println("COCON-CLI failed with a missing or invalid argument.")
            if(exceptionLog) {
                println("ERROR: ${a.message}")
                println("ERROR: ${a.stackTraceToString()}")
            } else {
                println("Run COCON-CLI with the --log option to get the full error log.")
            }
        } catch (e: Exception) {
            println("COCON-CLI terminated unsuccessfully (e.g. potential invalidation)")
            if(exceptionLog) {
                println("ERROR: ${e.message}")
                println("ERROR: ${e.stackTraceToString()}")
            } else {
                println("Run COCON-CLI with the --log option to get the full error log.")
            }
        }
        println("Done.")
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(Checksum()).execute(*args))
}

fun exec(command: String, args: List<String>, workspace: String, p1: String?, p2: String?, dir: String?) {
    val argList = args.toMutableList()
    argList.removeAt(0) // Remove the command from the list

    if (command == "init") {
        Commander.init()
        return
    }
    val ws = Workspace(workspace)
    val serializedSystem: String = ws.open()
    val rsd: SystemDescription = SystemDescription.parse(
        serializedSystem,
        TinkerRevisionGraph::build
    )
    val revisionSystem: MultiRevisionSystem = MultiRevisionSystem.create(
        rsd.parts,
        rsd.relations,
        rsd.projections
    )
    println(">>> EXEC BEGIN")
    when (command) {
        "init-subsys" -> Commander.initSubsys(revisionSystem, argList[0])
        "rm-subsys" -> Commander.removeSubsys(revisionSystem, argList[0])
        "ls-subsys" -> Commander.listSubsys(revisionSystem)
        "add-rev" -> {
            assert(p1 != null && p1.isNotEmpty())
            Commander.addRev(revisionSystem, argList[0], argList[1], argList[2], p1!!, p2)
        }
        "rm-rev" -> Commander.removeRev(revisionSystem, argList[0])
        "ls-rev" -> Commander.listRev(revisionSystem, argList[0])
        "ls-revs" -> Commander.listRevs(revisionSystem, argList[0])
        "ls-rev-rel" -> Commander.listRevRel(revisionSystem, argList[0])
        "ls-rel" -> Commander.listRel(revisionSystem, argList[0])
        "add-crel" -> Commander.addCrel(revisionSystem, argList[0], argList[1])
        "rm-crel" -> Commander.rmCrel(revisionSystem, argList[0], argList[1])
        "add-proj" -> {
            val targets = argList.subList(1, argList.size)
            Commander.addProj(revisionSystem, argList[0], targets)
        }
        "rm-proj" -> Commander.rmProj(revisionSystem, argList[0])
        "query-time" -> {
            val context = Commander.queryTime(revisionSystem, argList[0], argList[1].toInt())
            checkoutContextDirectory(context, dir)
        }
        "query-space" -> {
            val context = Commander.querySpace(revisionSystem, argList[0], argList[1].toInt())
            checkoutContextDirectory(context, dir)
        }
        "query-rel" -> {
            val context = Commander.queryRel(revisionSystem, argList[0])
            checkoutContextDirectory(context, dir)
        }
        "query-proj" -> {
            val context =  Commander.queryProj(revisionSystem, argList[0])
            checkoutContextDirectory(context, dir)
        }
    }
    println("<<< EXEC END")
    if( command.startsWith("ls-").not() && command.startsWith("query").not()) {
        ws.write(revisionSystem.serialize())
    }
}

fun checkoutContextDirectory(context: Context, targetPath: String?) {
    if (targetPath == null) {
        println("Use the -d / --dir option to specify a target directory for context checkout.")
        return
    }
    val targetDir = File(targetPath)
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    context.participants.forEach { revision ->
        val file = File(revision.location)
        if (file.exists()) {
            val targetFile = File(targetDir, revision.revId + "__" + file.name)
            file.copyTo(targetFile, overwrite = true)
        }
    }

}