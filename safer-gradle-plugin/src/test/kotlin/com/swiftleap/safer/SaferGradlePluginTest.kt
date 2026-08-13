package com.swiftleap.safer

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SaferGradlePluginTest {

    @Test
    fun `test applying plugin to root project creates safer extension on root and subprojects`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject = ProjectBuilder.builder().withName("sub").withParent(rootProject).build()

        val plugin = SaferGradlePlugin()
        plugin.apply(rootProject)

        val rootExtension = rootProject.extensions.findByName("safer") as? SaferConfigurationBuilder
        assertNotNull(rootExtension)

        val subExtension = subProject.extensions.findByName("safer") as? SaferConfigurationBuilder
        assertNotNull(subExtension)
    }

    @Test
    fun `test configuring subproject safer extension`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject = ProjectBuilder.builder().withName("sub").withParent(rootProject).build()

        val plugin = SaferGradlePlugin()
        plugin.apply(rootProject)

        val rootExt = rootProject.extensions.getByType(SaferConfigurationBuilder::class.java)
        rootExt.unsafe {
            warnAsError(true)
            checkKotlinStdLib()
        }

        val subExt = subProject.extensions.getByType(SaferConfigurationBuilder::class.java)
        subExt.unsafe {
            checkKotlinCoroutines()
        }

        val merged = rootExt.merge(subExt).build()
        assertTrue(merged.unsafeWarnAsError)
        assertEquals(setOf("kotlin-stdlib", "kotlin-coroutines"), merged.unsafePresetLibs)
    }

    @Test
    fun `test subproject configuration overrides root configuration`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject = ProjectBuilder.builder().withName("sub").withParent(rootProject).build()

        val plugin = SaferGradlePlugin()
        plugin.apply(rootProject)

        val rootExt = rootProject.extensions.getByType(SaferConfigurationBuilder::class.java)
        rootExt.unsafe {
            warnAsError(true)
        }

        val subExt = subProject.extensions.getByType(SaferConfigurationBuilder::class.java)
        subExt.unsafe {
            warnAsError(false)
        }

        val merged = rootExt.merge(subExt).build()
        assertEquals(false, merged.unsafeWarnAsError)
    }
}
