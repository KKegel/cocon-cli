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
import coconlib.context.ContextType
import coconlib.core.MultiRevisionSystem
import coconlib.graph.EdgeDescription
import coconlib.graph.EdgeLabel
import coconlib.graph.RevisionDescription
import coconlib.system.Projection
import coconlib.system.Relation
import kotlin.coroutines.Continuation

object Commander {

    fun init() {
        // Initialize a new empty workspace
        val emptySystem = MultiRevisionSystem(mutableSetOf(), mutableSetOf(), mutableSetOf())
        Workspace.create("./workspace.cocon", emptySystem.serialize())
    }

    fun initSubsys(mrs: MultiRevisionSystem, subsystemName: String) {
        // Initialize a new empty subsystem
        Validation.validateIdentifier(subsystemName)
        mrs.initNewGraph(subsystemName)
    }

    fun removeSubsys(mrs: MultiRevisionSystem, subsystemName: String) {
        // Remove a subsystem
        mrs.removeGraph(subsystemName)
    }

    fun listSubsys(mrs: MultiRevisionSystem) {
        // List the subsystems with some meta information
        val subsystems = mrs.getParts().map { Triple(it.graphId, it.getRevisions().size, it.getEdges().size) }
        subsystems.forEach {
            println("> ID ${it.first}, Revisions: ${it.second}, Edges: ${it.third}")
        }
    }

    fun addRev(mrs: MultiRevisionSystem, graphId: String, revisionId: String,
               path: String, predecessor1: String?, predecessor2: String?) {
        // Add a revision
        // The revision may be a unification (merge)
        Validation.validateIdentifier(revisionId)
        Validation.validateIdentifier(path)

        val revision = RevisionDescription(graphId, revisionId, revisionId, path)

        if(predecessor1 != null){
            val successorEdge1 = EdgeDescription(predecessor1, revisionId, EdgeLabel.SUCCESSOR)

            if (predecessor2 != null) {
                val successorEdge2 = EdgeDescription(predecessor2, revisionId, EdgeLabel.SUCCESSOR)
                mrs.addRevisionWithUnification(graphId, revision, successorEdge1, successorEdge2)
                //coconlib checks validity in this case
            }else{
                mrs.addRevision(graphId, revision, safe = false)
                mrs.addEdge(graphId, successorEdge1, safe = false)
                mrs.validate()
            }
        }else{
            //That's a root revision
            mrs.addRevision(graphId, revision, safe = true)
        }
    }

    fun removeRev(mrs: MultiRevisionSystem, revisionId: String) {
        // Remove a revision
        // all pointers to the revision are removed automatically
        mrs.removeRevision(revisionId)
    }

    fun listRevs(mrs: MultiRevisionSystem, subsystem: String) {
        // List the revisions of a specific subsystem
        val revisions = mrs.getParts().first { it.graphId == subsystem }.getRevisions()
        revisions.forEach {
            println("> ID ${it.revId}, Path: ${it.location}")
        }
    }

    fun listRev(mrs: MultiRevisionSystem, revision: String) {
        // List the specified revision
        val revisionDescription: RevisionDescription = mrs.findRevision(revision)
        println(">  ${revisionDescription.revId}, ${revisionDescription.location} (${revisionDescription.serialize()})")
    }

    fun listRevRel(mrs: MultiRevisionSystem, revision: String) {
        // List the revision relationships
        val edges: List<EdgeDescription> = mrs.findEdges(revision)
        val projections: List<Projection> = mrs.findProjections(revision)
        val relations: List<Relation> = mrs.findRelations(revision)
        println("> Edges: ")
        edges.forEach {
            println(">> ${it.sourceShortId} -> ${it.targetShortId} (${it.label})")
        }
        println("> Projections: ")
        projections.forEach {
            println(">> ${it.projectionId} (${it.sources.joinToString(", ")}) -> ${it.target}")
        }
        println("> Relations: ")
        relations.forEach {
            println(">> ${it.fromRevision} (${it.fromGraph}) -> ${it.toRevision} (${it.toGraph})")
        }
    }

    fun listRel(mrs: MultiRevisionSystem, subsystem: String) {
        // List the relationships outgoing and incoming of a subsystem
        val relations = mrs.getRelations().filter { it.fromGraph == subsystem || it.toGraph == subsystem }
        relations.forEach {
            println("> ${it.fromGraph} (${it.fromRevision}) -> ${it.toGraph} (${it.toRevision})")
        }
    }

    fun addCrel(mrs: MultiRevisionSystem, rev1: String, rev2: String) {
        // Add a relationship
        val revision1: RevisionDescription = mrs.findRevision(rev1)
        val revision2: RevisionDescription = mrs.findRevision(rev2)
        val relation = Relation(revision1.graph, revision2.graph, revision1.revId, revision2.revId)
        mrs.addRelation(relation)
    }

    fun rmCrel(mrs: MultiRevisionSystem, rev1: String, rev2: String) {
        // Remove a relationship
        val revision1: RevisionDescription = mrs.findRevision(rev1)
        val revision2: RevisionDescription = mrs.findRevision(rev2)
        val relation = Relation(revision1.graph, revision2.graph, revision1.revId, revision2.revId)
        mrs.removeRelation(relation)
    }

    fun addProj(mrs: MultiRevisionSystem, name: String, revisions: List<String>) {
        // Add a projection
        Validation.validateIdentifier(name)
        val revisionDescriptions = revisions.map { mrs.findRevision(it) }
        val projection = Projection(name, revisionDescriptions.map { it.revId }, name)
        mrs.addProjection(projection)
    }

    fun rmProj(mrs: MultiRevisionSystem, name: String) {
        // Remove a projection
        mrs.removeProjection(mrs.getProjections().first { it.projectionId == name })
    }

    fun queryTime(mrs: MultiRevisionSystem, revision: String, bound: Int): Context {
        // Query the time context of a revision
        val revision = mrs.findRevision(revision)
        val context = mrs.findLocalContext(revision.graph, revision.revId, ContextType.TIME, bound)
        println("> Time context of revision $revision bound by $bound:")
        context.participants.forEach {
            println(">> ${it.revId} (${it.graph})")
        }
        return context
    }

    fun querySpace(mrs: MultiRevisionSystem, revision: String, bound: Int): Context {
        // Query the space context of a revision
        val revision = mrs.findRevision(revision)
        val context = mrs.findLocalContext(revision.graph, revision.revId, ContextType.SPACE, bound)
        println("> Space context of revision $revision bound by $bound:")
        context.participants.forEach {
            println(">> ${it.revId} (${it.graph})")
        }
        return context
    }

    fun queryRel(mrs: MultiRevisionSystem, revision: String): Context {
        // Query the relational context of a revision
        val context = mrs.findGlobalContext(revision, ContextType.RELATIONAL)
        println("> Relational context of revision $revision:")
        context.participants.forEach {
            println(">> ${it.revId} (${it.graph})")
        }
        return context
    }

    fun queryProj(mrs: MultiRevisionSystem, project: String): Context {
        // Query the projectional context of a revision
        val context = mrs.findGlobalContext(project, ContextType.PROJECTIVE)
        println("> Projective context of projection $project:")
        context.participants.forEach {
            println(">> ${it.revId} (${it.graph})")
        }
        return context
    }

}