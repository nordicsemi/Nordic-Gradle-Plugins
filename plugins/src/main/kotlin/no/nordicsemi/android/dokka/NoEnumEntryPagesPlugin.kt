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

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DEnumEntry
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement
import org.jetbrains.dokka.transformers.pages.PageTransformer

/**
 * Simple name of the annotation that opts an enum class out of per-entry pages.
 *
 * The annotation is declared in the documented module, e.g. `@HideEnumEntryPages`.
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
private const val ANNOTATION_NAME = "HideEnumEntryPages"

/**
 * Dokka plugin that stops a separate page from being generated for each entry of enum classes
 * annotated with `@HideEnumEntryPages`.
 *
 * The entries are still listed in the "Entries" table of the enum class page, but are no longer
 * links to their own pages. Enums without the annotation are documented as usual.
 *
 * This runs inside Dokka, not Gradle. It is added to the `dokkaPlugin` configuration by the
 * Nordic convention plugins.
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
class NoEnumEntryPagesPlugin : DokkaPlugin() {

    @Suppress("unused")
    val removeEnumEntryPages by extending {
        plugin<DokkaBase>().htmlPreprocessors with EnumEntryPageRemover
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}

private object EnumEntryPageRemover : PageTransformer {

    override fun invoke(input: RootPageNode): RootPageNode = input.strip() as RootPageNode

    private fun PageNode.strip(): PageNode {
        val hideEntries = this is ClasslikePageNode && documentables.any { it.isAnnotated() }
        val kept = children
            .filterNot { hideEntries && it is ClasslikePageNode && it.documentables.firstOrNull() is DEnumEntry }
            .map { it.strip() }
        return modified(name = name, children = kept)
    }

    private fun Any.isAnnotated() = this is DEnum && extra[Annotations]
        ?.directAnnotations?.values?.flatten()
        ?.any { it.dri.classNames == ANNOTATION_NAME } == true
}
