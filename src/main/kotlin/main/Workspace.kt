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

import java.io.File

class Workspace(val descriptor: String) {

    /**
     * Opens the workspace descriptor and loads the workspace.
     * @return The content of the workspace file.
     */
    fun open(): String {
        val file = File(descriptor)
        if (!file.exists()) {
            throw IllegalArgumentException("Workspace descriptor file does not exist.")
        }
        if (!file.canRead()) {
            throw IllegalArgumentException("Cannot read workspace descriptor file.")
        }
        if (!file.isFile) {
            throw IllegalArgumentException("Workspace descriptor is not a file.")
        }
        return file.readText()
    }

    fun write(content: String) {
        val file = File(descriptor)
        if (!file.canWrite()) {
            throw IllegalArgumentException("Cannot write to workspace descriptor file.")
        }
        file.writeText(content)
    }

    companion object {

        fun create(descriptor: String, initialContent: String) {
            val file = File(descriptor)
            if (file.exists()) {
                throw IllegalArgumentException("Workspace descriptor file already exists.")
            }
            if (!file.createNewFile()) {
                throw IllegalArgumentException("Cannot create workspace descriptor file.")
            }
            file.createNewFile()
            file.writeText(initialContent)
        }
    }

}