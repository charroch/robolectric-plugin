Gradle plugin for running local robolectric test
================================================


Gradle plugin to run robolectric tests. It is hackishly working. The main issue with the Android plugin is the way that it declares build variances and sourcesets - which are not Gradle sourcesets. Currently the plugin does a brute force when it comes to addings jars (from aar, folders, configs). 


We aim to achieve the following:

```groovy

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.4.2'
        classpath files('/home/acsia/dev/groovy/robolectric-plugin/build/libs/robolectric-plugin-0.0.1-SNAPSHOT.jar')
    }
}

apply plugin: 'android'
apply plugin: 'robolectric'

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
    maven {
        url "https://github.com/novoda/public-mvn-repo/raw/master/releases"
    }
}

dependencies {
    //compile files('libs/android-support-v4.jar')

    // had to deploy to sonatype to get AAR to work
    compile 'com.novoda:actionbarsherlock:4.3.2-SNAPSHOT'

    robolectricCompile 'org.robolectric:robolectric:2.0-alpha-2'
    robolectricCompile group: 'junit', name: 'junit', version: '4.+'
}

android {
    compileSdkVersion 17
    buildToolsVersion "17.0.0"

    defaultConfig {
        minSdkVersion 7
        targetSdkVersion 17
    }
}
```

Originally, we no plugin, you could write along the lines of the following build.gradle

```groovy
import com.android.build.gradle.internal.tasks.BaseTask
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.testing.Test

import java.util.concurrent.Callable

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:0.4.2'
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
    maven {
        url "https://github.com/novoda/public-mvn-repo/raw/master/releases"
    }
}

apply plugin: 'android'

sourceCompatibility = JavaVersion.VERSION_1_6
targetCompatibility = JavaVersion.VERSION_1_6

configurations {
    robolectric {
        extendsFrom compile
    }
}

sourceSets {
    robolectric {
        java.srcDir file('src/test/java')
        resources.srcDir file('src/test/resources')
        compileClasspath += configurations.robolectric
        runtimeClasspath += compileClasspath
    }
}

dependencies {
    compile project(':core')
    compile project(':google_play_services')

    compile 'com.novoda:actionbarsherlock:4.3.2-SNAPSHOT'
    compile 'com.novoda:showcaseview:3.1.3-SNAPSHOT'

    compile 'de.keyboardsurfer.android.widget:crouton:1.7'

    compile 'com.google.android:support-v4:r11',
            'com.squareup:tape:1.1.0',
            'com.squareup:otto:1.3.3',
            'com.novoda:sqliteprovider-core:1.0.0',
            'com.novoda.imageloader:imageloader-core:1.5.8',
            'com.novoda.merlin:merlin-core:0.4'
            
    instrumentTestCompile project(':core')
    instrumentTestCompile files('../core/lib/jackson-databind-2.2.0.jar')
    instrumentTestCompile 'com.google.dexmaker:dexmaker:1.0',
            'com.google.dexmaker:dexmaker-mockito:1.0',
            'org.mockito:mockito-core:1.9.5',
            'com.jayway.android.robotium:robotium-solo:3.6',
            'com.squareup.spoon:spoon-client:1.0.1',
            'com.squareup:fest-android:1.0.3'

    robolectricCompile 'com.squareup:fest-android:1.0.3'
    robolectricCompile 'org.robolectric:robolectric:2.0-alpha-2'
    robolectricCompile 'org.mockito:mockito-all:1.9.5', 'org.easytesting:fest-assert-core:2.0M8'
    robolectricCompile group: 'junit', name: 'junit', version: '4.+'
}

android {
    compileSdkVersion 17
    buildToolsVersion "17"

    defaultConfig {
        versionCode 5
        versionName "0.1.7-SNAPSHOT"
        minSdkVersion 14
        targetSdkVersion 17
    }
}

project.getPlugins().getPlugin('android').getExtension().getApplicationVariants().each {
    it.getJavaCompile().doLast {
        println '<- {placeholder to run aspectj} ->'
    }
}

task robolectric(type: Test, dependsOn: assemble) {

    workingDir 'src/main'

    testClassesDir = sourceSets.robolectric.output.classesDir

    android.sourceSets.main.java.srcDirs.each { dir ->
        def buildDir = dir.getAbsolutePath().split('/')
        buildDir = (buildDir[0..(buildDir.length - 4)] + ['build', 'classes', 'debug']).join('/')

        project.getPlugins().getPlugin('android').prepareTaskMap.each {
            sourceSets.robolectric.compileClasspath += files(it.value.explodedDir.getAbsolutePath() + '/classes.jar')
            sourceSets.robolectric.runtimeClasspath += files(it.value.explodedDir.getAbsolutePath() + '/classes.jar')
        }

        sourceSets.robolectric.compileClasspath += files(buildDir)
        sourceSets.robolectric.runtimeClasspath += files(buildDir)
    }

    classpath = sourceSets.robolectric.runtimeClasspath
}
```
