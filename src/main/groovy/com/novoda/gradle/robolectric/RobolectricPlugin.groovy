package com.novoda.gradle.robolectric

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test

import java.util.concurrent.Callable

class RobolectricPlugin implements Plugin<Project> {

    public static final String ANDROID_PLUGIN_NAME = "android";
    public static final String ANDROID_LIBRARY_PLUGIN_NAME = "android-library";

    public static final String COMPILE_CONFIGURATION_NAME = "compile";
    public static final String TEST_COMPILE_CONFIGURATION_NAME = "robolectricTestCompile";
    public static final String RUNTIME_CONFIGURATION_NAME = "runtime";
    public static final String TEST_RUNTIME_CONFIGURATION_NAME = "robolectricTestRuntime";

    public static final String ROBOLECTRIC_SOURCE_SET_NAME = "robolectric";
    public static final String ROBOLECTRIC_CONFIGURATION_NAME = "robolectric";
    public static final String ROBOLECTRIC_TASK_NAME = "robolectric";

    void apply(Project project) {
        project.getPlugins().apply(JavaBasePlugin.class);
        ensureValidProject(project);

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        configureConfigurations(project);
        configureSourceSets(javaConvention);

        configureTest(project, javaConvention);

        project.afterEvaluate {
            configureAndroidDependency(project, javaConvention)
        }
    }

    def configureAndroidDependency(Project project, JavaPluginConvention pluginConvention) {
        SourceSet robolectric = pluginConvention.getSourceSets().findByName(ROBOLECTRIC_SOURCE_SET_NAME);

        ((BasePlugin) getAndroidPlugin(project)).mainSourceSet.java.srcDirs.each { dir ->
            def buildDir = dir.getAbsolutePath().split('/')
            buildDir = (buildDir[0..(buildDir.length - 4)] + ['build', 'classes', 'debug']).join('/')
            robolectric.compileClasspath += project.files(buildDir)
            robolectric.runtimeClasspath += project.files(buildDir)
        }

        getAndroidPlugin(project).buildTypes.each {
            it.value.getLocalDependencies().each {
                robolectric.compileClasspath += project.files(it.jarFile)
                robolectric.runtimeClasspath += project.files(it.jarFile)
            }
        }

        // AAR files
        getAndroidPlugin(project).prepareTaskMap.each {
            robolectric.compileClasspath += project.fileTree(dir: it.value.explodedDir, include: '*.jar')
            robolectric.runtimeClasspath += project.fileTree(dir: it.value.explodedDir, include: '*.jar')
        }

        // Default Android jar
        getAndroidPlugin(project).getRuntimeJarList().each {
            robolectric.compileClasspath += project.files(it)
            robolectric.runtimeClasspath += project.files(it)
        }

        robolectric.runtimeClasspath = robolectric.runtimeClasspath.filter {
            it
            true
        }
    }

    private void ensureValidProject(Project project) {
        boolean isAndroidProject = project.getPlugins().hasPlugin(ANDROID_PLUGIN_NAME);
        boolean isAndroidLibProject = project.getPlugins().hasPlugin(ANDROID_LIBRARY_PLUGIN_NAME);
        if (!(isAndroidLibProject | isAndroidProject)) {
            throw new NotAnAndroidProject();
        }
    }

    void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration compileConfiguration = configurations.getByName(COMPILE_CONFIGURATION_NAME);
        Configuration robolectric = configurations.create(ROBOLECTRIC_CONFIGURATION_NAME);
        robolectric.extendsFrom(compileConfiguration);
    }

    private void configureSourceSets(final JavaPluginConvention pluginConvention) {
        final Project project = pluginConvention.getProject();

        SourceSet robolectric = pluginConvention.getSourceSets().create(ROBOLECTRIC_SOURCE_SET_NAME);

        robolectric.java.srcDir project.file('src/test/java')
        robolectric.compileClasspath += project.configurations.robolectric
        robolectric.runtimeClasspath += robolectric.compileClasspath
    }

    private void configureTest(final Project project, final JavaPluginConvention pluginConvention) {
        project.getTasks().withType(Test.class, new Action<Test>() {
            public void execute(final Test test) {
                test.workingDir 'src/main'
                test.getConventionMapping().map("testClassesDir", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName("robolectric").getOutput().getClassesDir();
                    }
                });

                test.getConventionMapping().map("classpath", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName("robolectric").getRuntimeClasspath();
                    }
                });

                test.getConventionMapping().map("testSrcDirs", new Callable<Object>() {
                    public Object call() throws Exception {
                        return new ArrayList<File>(pluginConvention.getSourceSets().getByName("robolectric").getJava().getSrcDirs());
                    }
                });
            }
        });

        Test test = project.getTasks().create(ROBOLECTRIC_TASK_NAME, Test.class);
        project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(test);
        test.setDescription("Runs the unit tests using robolectric.");
        test.setGroup(JavaBasePlugin.VERIFICATION_GROUP);

        test.dependsOn(project.getTasks().findByName('robolectricClasses'))
        test.dependsOn(project.getTasks().findByName('assemble'))
    }

    private AppPlugin getAndroidPlugin(Project project) {
        return (AppPlugin) project.getPlugins().findPlugin(ANDROID_PLUGIN_NAME);
    }

}
