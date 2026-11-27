/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.dokka

import org.jetbrains.dokka.DokkaSourceSetID
import org.jetbrains.dokka.DokkaSourceSetImpl
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DEnumEntry
import org.jetbrains.dokka.model.properties.PropertyContainer
import org.jetbrains.dokka.model.properties.plus
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumEntryPageRemoverTest {

    private val sourceSet = DokkaSourceSetImpl(sourceSetID = DokkaSourceSetID("test", "main"))
    private val content = ContentText("", DCI(emptySet(), ContentKind.Main), emptySet())

    private fun annotation(packageName: String) = Annotations.Annotation(
        dri = DRI(packageName = packageName, classNames = "HideEnumEntryPages"),
        params = emptyMap()
    )

    private fun entryPage(enum: String, name: String) = ClasslikePageNode(
        name = name,
        content = content,
        dri = setOf(DRI("test", "$enum.$name")),
        documentables = listOf(
            DEnumEntry(
                dri = DRI("test", "$enum.$name"),
                name = name,
                documentation = emptyMap(),
                expectPresentInSet = null,
                functions = emptyList(),
                properties = emptyList(),
                classlikes = emptyList(),
                sourceSets = setOf(sourceSet)
            )
        ),
        children = emptyList()
    )

    /** A page for a function or other member of an enum, which must never be removed. */
    private fun memberPage(name: String) = ClasslikePageNode(
        name = name,
        content = content,
        dri = setOf(DRI("test", name)),
        documentables = emptyList(),
        children = emptyList()
    )

    private fun enumPage(
        name: String,
        entries: List<String>,
        annotation: Annotations.Annotation? = null
    ): ClasslikePageNode {
        val extra = annotation?.let {
            PropertyContainer.empty<DEnum>() + Annotations(mapOf(sourceSet to listOf(it)))
        } ?: PropertyContainer.empty()
        return ClasslikePageNode(
            name = name,
            content = content,
            dri = setOf(DRI("test", name)),
            documentables = listOf(
                DEnum(
                    dri = DRI("test", name),
                    name = name,
                    entries = emptyList(),
                    documentation = emptyMap(),
                    expectPresentInSet = null,
                    sources = emptyMap(),
                    functions = emptyList(),
                    properties = emptyList(),
                    classlikes = emptyList(),
                    visibility = emptyMap(),
                    companion = null,
                    constructors = emptyList(),
                    supertypes = emptyMap(),
                    sourceSets = setOf(sourceSet),
                    isExpectActual = false,
                    extra = extra
                )
            ),
            children = entries.map { entryPage(name, it) } + memberPage("describe")
        )
    }

    private fun root(vararg enums: ClasslikePageNode): RootPageNode = ModulePageNode(
        name = "module",
        content = content,
        documentables = emptyList(),
        children = enums.toList()
    )

    private fun PageNode.childNames(enum: String) = children.single { it.name == enum }.children.map { it.name }

    @Test
    fun `annotated enum has no entry pages`() {
        val result = EnumEntryPageRemover(
            root(enumPage("Hidden", listOf("A", "B"), annotation("no.nordicsemi.android.dokka")))
        )
        assertEquals(listOf("describe"), result.childNames("Hidden"))
    }

    @Test
    fun `enum without the annotation keeps its entry pages`() {
        val result = EnumEntryPageRemover(root(enumPage("Plain", listOf("A", "B"))))
        assertEquals(listOf("A", "B", "describe"), result.childNames("Plain"))
    }

    @Test
    fun `only annotated enums are changed`() {
        val result = EnumEntryPageRemover(
            root(
                enumPage("Hidden", listOf("A"), annotation("no.nordicsemi.android.dokka")),
                enumPage("Plain", listOf("B", "C"))
            )
        )
        assertEquals(listOf("describe"), result.childNames("Hidden"))
        assertEquals(listOf("B", "C", "describe"), result.childNames("Plain"))
    }

    @Test
    fun `annotation with the same name from another package is ignored`() {
        val result = EnumEntryPageRemover(
            root(enumPage("Other", listOf("A", "B"), annotation("other")))
        )
        assertEquals(listOf("A", "B", "describe"), result.childNames("Other"))
    }

    @Test
    fun `the result is still a root page`() {
        val result = EnumEntryPageRemover(root())
        assertTrue(result.children.isEmpty())
    }
}
