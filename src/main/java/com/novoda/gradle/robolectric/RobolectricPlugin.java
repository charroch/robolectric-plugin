package com.novoda.gradle.robolectric;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

public class RobolectricPlugin implements Plugin<Project> {

    public static final String PROCESS_RESOURCES_TASK_NAME = "processResources";
    public static final String CLASSES_TASK_NAME = "classes";
    public static final String COMPILE_JAVA_TASK_NAME = "compileJava";
    public static final String PROCESS_TEST_RESOURCES_TASK_NAME = "processTestResources";
    public static final String TEST_CLASSES_TASK_NAME = "testClasses";
    public static final String COMPILE_TEST_JAVA_TASK_NAME = "compileTestJava";
    public static final String TEST_TASK_NAME = "test";
    public static final String JAR_TASK_NAME = "jar";
    public static final String JAVADOC_TASK_NAME = "javadoc";

    public static final String COMPILE_CONFIGURATION_NAME = "compile";
    public static final String RUNTIME_CONFIGURATION_NAME = "runtime";
    public static final String TEST_RUNTIME_CONFIGURATION_NAME = "testRuntime";
    public static final String TEST_COMPILE_CONFIGURATION_NAME = "testCompile";

    @Override
    public void apply(Project project) {
        ensureValidProject(project);

//
//        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
//        configureSourceSets(javaConvention);
//        configureConfigurations(project);
//        configureTest(project, javaConvention);
//        //configureBuild(project);
        project.getLogger().info("feef", "feef");
    }

    private void ensureValidProject(Project project) {
        boolean isAndroidProject = project.getPlugins().hasPlugin("android");
        boolean isAndroidLibProject = project.getPlugins().hasPlugin("android-library");
        if (!(isAndroidLibProject | isAndroidProject)) {
            throw new NotAnAndroidProject();
        }
    }

    void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration compileConfiguration = configurations.getByName(COMPILE_CONFIGURATION_NAME);
        Configuration runtimeConfiguration = configurations.getByName(RUNTIME_CONFIGURATION_NAME);

        Configuration compileTestsConfiguration = configurations.getByName(TEST_COMPILE_CONFIGURATION_NAME);
        compileTestsConfiguration.extendsFrom(compileConfiguration);

        configurations.getByName(TEST_RUNTIME_CONFIGURATION_NAME).extendsFrom(runtimeConfiguration, compileTestsConfiguration);

        configurations.getByName(Dependency.DEFAULT_CONFIGURATION).extendsFrom(runtimeConfiguration);
    }

    private void configureSourceSets(final JavaPluginConvention pluginConvention) {
        final Project project = pluginConvention.getProject();
        SourceSet main = pluginConvention.getSourceSets().create(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = pluginConvention.getSourceSets().create(SourceSet.TEST_SOURCE_SET_NAME);
        test.setCompileClasspath(project.files(main.getOutput(), project.getConfigurations().getByName(TEST_COMPILE_CONFIGURATION_NAME)));
        test.setRuntimeClasspath(project.files(test.getOutput(), main.getOutput(), project.getConfigurations().getByName(TEST_RUNTIME_CONFIGURATION_NAME)));
    }

    private void configureTest(final Project project, final JavaPluginConvention pluginConvention) {
        project.getTasks().withType(Test.class, new Action<Test>() {
            public void execute(final Test test) {
                test.getConventionMapping().map("testClassesDir", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getOutput().getClassesDir();
                    }
                });
                test.getConventionMapping().map("classpath", new Callable<Object>() {
                    public Object call() throws Exception {
                        return pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getRuntimeClasspath();
                    }
                });
                test.getConventionMapping().map("testSrcDirs", new Callable<Object>() {
                    public Object call() throws Exception {
                        return new ArrayList<File>(pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getJava().getSrcDirs());
                    }
                });
            }
        });

        Test test = project.getTasks().create(TEST_TASK_NAME, Test.class);

        test.setClasspath(new SimpleFileCollection(new File("/tmp")));
        test.setDescription("Runs the unit tests.");
        test.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
    }
}
