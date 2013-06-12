package com.novoda.gradle.robolectric

import com.android.build.gradle.AppPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class RobolectricPluginTest extends GroovyTestCase {

    void test_should_throw_exception_if_android_plugin_is_not_in_scope() {
        Project project = ProjectBuilder.builder().build()
        shouldFail(NotAnAndroidProject.class) {
            project.apply plugin: RobolectricPlugin
        }
    }

    void test_should_not_throw_exception_if_android_plugin_is_in_scope() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: AppPlugin
        project.apply plugin: RobolectricPlugin
        assertTrue(true)
    }
}
