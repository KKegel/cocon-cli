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

import picocli.CommandLine
import picocli.CommandLine.Parameters
import java.util.concurrent.Callable
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


    override fun call(): Int {
        assert(command.isNotEmpty())
        assert(command in commandList)
        assert(positional.size > 1)
        assert(command == positional.first())
        exec(command, positional, workspace, predecessor1, predecessor2)
        return 0
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(Checksum()).execute(*args))
}

fun exec(command: String, args: List<String>, workspace: String, p1: String?, p2: String?) {

}