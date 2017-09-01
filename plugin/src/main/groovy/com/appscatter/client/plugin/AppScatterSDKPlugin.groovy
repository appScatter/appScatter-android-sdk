package com.appscatter.client.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * A gradle plugin that adds Statful SDK and Aspects dependencies
 * to the end-project. Additionally, enables weaving aspectJ code in the
 * end-project.
 */
class AppScatterSDKPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def hasLibraryPlugin = project.plugins.withType(LibraryPlugin)
        def hasApplicationPlugin = project.plugins.withType(AppPlugin)
        final def variants

        if (!hasLibraryPlugin && !hasApplicationPlugin) {
            throw new IllegalStateException("either 'com.android.application' or 'com.android.library' plugin required.")
        }

        if (hasLibraryPlugin) {
            variants = project.android.libraryVariants
        } else {
            variants = project.android.applicationVariants
        }

        project.dependencies {
            compile 'org.aspectj:aspectjrt:1.8.10'
            compile 'com.statful.android:sdk-aspects:0.2.2-SNAPSHOT'
            compile 'com.appscatter.metrics:metrics:0.4.0-SNAPSHOT'
            compile 'com.appscatter.iab:core:0.4.+@aar'
            compile('com.appscatter.iab:utils:0.4.+@aar') {
                transitive = true
            }
        }

        variants.all { variant ->
            JavaCompile javaCompile = variant.javaCompile
            javaCompile.doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.7",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompile.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompile.classpath.asPath,
                        "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
                ]

                MessageHandler handler = new MessageHandler(true);
                new Main().run(args, handler);

                def log = project.logger
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            log.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                            log.warn message.message, message.thrown
                            break;
                        case IMessage.INFO:
                            log.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            log.debug message.message, message.thrown
                            break;
                    }
                }
            }

        }
    }
}
