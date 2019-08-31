package com.github.jazzmuesli.mockextractor.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MockExtractorPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getTasks().create("extractmocks", MockExtractorTask.class, (task) -> { 
            task.setProject(project);
        });
    }
}
