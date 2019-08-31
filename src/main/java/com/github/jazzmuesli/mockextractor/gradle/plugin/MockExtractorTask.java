package com.github.jazzmuesli.mockextractor.gradle.plugin;

import org.davidespadini.mockextractor.core.Parser;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockExtractorTask extends DefaultTask {

	private Project project;

	public void setProject(Project project) {
		this.project = project;
	}

	@TaskAction
	void processDirectories() {
		SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
		List<String> deps = getDependencies();
		for (Map.Entry<String, SourceSet> entry : sourceSets.getAsMap().entrySet()) {
			SourceSet sourceSet = entry.getValue();
			Set<File> srcDirs = sourceSet.getAllJava().getSrcDirs();
			System.out.println("Processing sourceSet " + entry.getKey() + " with srcDirs=" + srcDirs);
			srcDirs.forEach(x -> processSourceDirectory(x.getAbsolutePath(), deps));

		}
	}

	private List<String> getDependencies() {
		// very hacky way to resolve dependencies, found at https://discuss.gradle.org/t/how-to-get-the-dependencies-from-a-custom-plugin/10691
		List<String> deps = new ArrayList();
		ConfigurationContainer configurations = getProject().getConfigurations();
		System.out.println("configurations: " + configurations);
		for (Configuration element : configurations) {
			try {
				Set<File> filesSet = element.resolve();
				for (Iterator<File> filesIterator = filesSet.iterator(); filesIterator.hasNext();) {
					File file = filesIterator.next();
//					System.out.println(file.getName());
					deps.add(file.getAbsolutePath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deps;
	}

	private void processSourceDirectory(String dirName, List<String> deps) {
		try {
			if (new File(dirName).exists()) {
				System.out.printf("processing: " + dirName + " with deps=" + deps.size());
				String outputFile = dirName + File.separator + "mockusages.csv";
				//TODO: with deps containing full path to jars, I get invalid environment settings error in Parser at line 49.
				Parser parser = new Parser(dirName, outputFile, null/*deps.toArray(new String[0])*/);
				parser.parse();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
