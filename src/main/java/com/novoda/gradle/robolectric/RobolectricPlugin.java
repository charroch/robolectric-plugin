package com.novoda.gradle.robolectric;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

public class RobolectricPlugin implements Plugin<Project> {

    public static final String ANDROID_PLUGIN_NAME = "android";
    public static final String ANDROID_LIBRARY_PLUGIN_NAME = "android-library";

    public static final String COMPILE_CONFIGURATION_NAME = "compile";
    public static final String ROBOLECTRIC_SOURCE_SET_NAME = "robolectricTest";
    public static final String TEST_COMPILE_CONFIGURATION_NAME = "robolectricTestCompile";
    public static final String RUNTIME_CONFIGURATION_NAME = "runtime";
    public static final String TEST_RUNTIME_CONFIGURATION_NAME = "testRuntime";

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(JavaBasePlugin.class);
        ensureValidProject(project);

        JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

        configureSourceSets(javaConvention);
        configureConfigurations(project);
        configureTest(project, javaConvention);

//        configureBuild(project);
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

        Configuration runtimeConfiguration = configurations.getByName(RUNTIME_CONFIGURATION_NAME);
        Configuration compileTestsConfiguration = configurations.getByName(TEST_COMPILE_CONFIGURATION_NAME);
        compileTestsConfiguration.extendsFrom(compileConfiguration);
//        configurations.getByName(TEST_RUNTIME_CONFIGURATION_NAME).extendsFrom(runtimeConfiguration, compileTestsConfiguration);
//        configurations.getByName(Dependency.DEFAULT_CONFIGURATION).extendsFrom(runtimeConfiguration);
    }

    private void configureSourceSets(final JavaPluginConvention pluginConvention) {
        final Project project = pluginConvention.getProject();

        SourceSet main = pluginConvention.getSourceSets().create(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet test = pluginConvention.getSourceSets().create(ROBOLECTRIC_SOURCE_SET_NAME);

        test.setCompileClasspath(project.files(main.getOutput(), project.getConfigurations().getByName(TEST_COMPILE_CONFIGURATION_NAME)));
        test.setRuntimeClasspath(test.getCompileClasspath());
    }

    private void configureTest(final Project project, final JavaPluginConvention pluginConvention) {
//        project.getTasks().withType(Test.class, new Action<Test>() {
//            public void execute(final Test test) {
//                test.getConventionMapping().map("testClassesDir", new Callable<Object>() {
//                    public Object call() throws Exception {
//                        return pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getOutput().getClassesDir();
//                    }
//                });
//                test.getConventionMapping().map("classpath", new Callable<Object>() {
//                    public Object call() throws Exception {
//                        return pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getRuntimeClasspath();
//                    }
//                });
//                test.getConventionMapping().map("testSrcDirs", new Callable<Object>() {
//                    public Object call() throws Exception {
//                        return new ArrayList<File>(pluginConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getJava().getSrcDirs());
//                    }
//                });
//            }
//        });
        Test test = project.getTasks().create("robolectricTest", Test.class);
        project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(test);
        test.setDescription("Runs the unit tests using robolectric.");
        test.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
    }

    private void configureBuild(Project project) {
        addDependsOnTaskInOtherProjects(
                project.getTasks().getByName(JavaBasePlugin.BUILD_NEEDED_TASK_NAME),
                true,
                JavaBasePlugin.BUILD_NEEDED_TASK_NAME, "testRuntime"
        );
        addDependsOnTaskInOtherProjects(project.getTasks().getByName(JavaBasePlugin.BUILD_DEPENDENTS_TASK_NAME), false,
                JavaBasePlugin.BUILD_DEPENDENTS_TASK_NAME, "testRuntime");
    }

    private void addDependsOnTaskInOtherProjects(final Task task, boolean useDependedOn, String otherProjectTaskName,
                                                 String configurationName) {
        Project project = task.getProject();
        final Configuration configuration = project.getConfigurations().getByName(configurationName);
        task.dependsOn(configuration.getTaskDependencyFromProjectDependency(useDependedOn, otherProjectTaskName));
    }

}
