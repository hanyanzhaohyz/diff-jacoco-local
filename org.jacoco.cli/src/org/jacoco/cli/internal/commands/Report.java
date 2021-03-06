/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *    Marc R. Hoffmann - rework
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import org.jacoco.cli.internal.Command;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.*;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The <code>report</code> command.
 */
public class Report extends Command {

	@Argument(usage = "list of JaCoCo *.exec files to read", metaVar = "<execfiles>")
	List<File> execfiles = new ArrayList<File>();

//	@Option(name = "--classfiles", usage = "location of Java class files", metaVar = "<path>", required = true)
	@Option(name = "--classfiles", usage = "location of Java class files", metaVar = "<path>")
	List<File> classfiles = new ArrayList<File>();

	@Option(name = "--sourcefiles", usage = "location of the source files", metaVar = "<path>")
	List<File> sourcefiles = new ArrayList<File>();

	@Option(name = "--tabwith", usage = "tab stop width for the source pages (default 4)", metaVar = "<n>")
	int tabwidth = 4;

	@Option(name = "--name", usage = "name used for this report", metaVar = "<name>")
	String name = "JaCoCo Coverage Report";

	@Option(name = "--encoding", usage = "source file encoding (by default platform encoding is used)", metaVar = "<charset>")
	String encoding;

	@Option(name = "--xml", usage = "output file for the XML report", metaVar = "<file>")
	File xml;

	@Option(name = "--csv", usage = "output file for the CSV report", metaVar = "<file>")
	File csv;

	@Option(name = "--html", usage = "output directory for the HTML report", metaVar = "<dir>")
	File html;

	@Option(name = "--projectPath", usage = "projectPath used for git local repository", metaVar = "<string>")
	String projectPath;

	@Option(name = "--revision", usage = "projectPath used for git local repository", metaVar = "<string>")
	String revision;

	@Option(name = "--baseRevision", usage = "git baseRevision used for calculate diff", metaVar = "<string>")
	String baseRevision;

	@Option(name = "--excludes", usage = "coverage exclusion", metaVar = "<string>")
	String excludes;

	@Option(name = "--includes", usage = "coverage includes", metaVar = "<string>")
	String includes;

	@Override
	public String description() {
		return "Generate reports in different formats by reading exec and Java class files.";
	}

	@Override
	public int execute(final PrintWriter out, final PrintWriter err)
			throws IOException {
		final ExecFileLoader loader = loadExecutionData(out);
		final IBundleCoverage bundle = analyze(loader.getExecutionDataStore(),
				out);
		writeReports(bundle, loader, out);
		return 0;
	}

	private ExecFileLoader loadExecutionData(final PrintWriter out)
			throws IOException {
		final ExecFileLoader loader = new ExecFileLoader();
		if (execfiles.isEmpty()) {
			out.println("[WARN] No execution data files provided.");
		} else {
			for (final File file : execfiles) {
				out.printf("[INFO] Loading execution data file %s.%n",
						file.getAbsolutePath());
				loader.load(file);
			}
		}
		return loader;
	}

	private List<File> getClassfileListByFilter() throws IOException{

		List<File> result=new ArrayList<>();
		List<String> incluesList = new ArrayList<>();
		List<String> excluesList = new ArrayList<>();
		if(includes !=null){
			if(!includes.trim().isEmpty()){
				incluesList = new ArrayList<>(Arrays.asList(includes.split(",")));
			}
		}
		if(excludes !=null){
			if(!excludes.trim().isEmpty()){
				excluesList = new ArrayList<>(Arrays.asList(excludes.split(",")));
			}
		}
		List<File> classesDirList = new ArrayList<>();
		if(classfiles.isEmpty()){
			classesDirList.add(new File(projectPath));
		}else{
			classesDirList.addAll(classfiles);
		}
		for (final File projectDir : classesDirList){
			if (projectDir.isDirectory()) {
				final FileFilter filter = new FileFilter(incluesList, excluesList);
				for (final File f : filter.getFiles(projectDir)) {
					if(f.isFile() && f.getName().endsWith(".class") && f.getParent().contains("/target/classes")){
						result.add(f);
					}
				}
			}
		}
		return result;
	}

	private CoverageBuilder getCoverageBuilder(){
		if(projectPath !=null && revision !=null && baseRevision !=null ){
			return new CoverageBuilder(projectPath,revision,baseRevision,true);
		}else{
			return new CoverageBuilder();
		}
	}

	private IBundleCoverage analyze(final ExecutionDataStore data,
									final PrintWriter out) throws IOException {

		final CoverageBuilder coverageBuilder = getCoverageBuilder();
		final Analyzer analyzer = new Analyzer(data, coverageBuilder);
		List<File> classesFileList = getClassfileListByFilter();
		for (final File f : classesFileList) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(coverageBuilder.getNoMatchClasses(), out);
		return coverageBuilder.getBundle(name);
	}

	// analyze???????????????
	private IBundleCoverage analyze1(final ExecutionDataStore data,
									final PrintWriter out) throws IOException {
		CoverageBuilder coverageBuilder = null;
		if(projectPath !=null && revision !=null && baseRevision !=null){
			coverageBuilder = new CoverageBuilder(projectPath,revision,baseRevision,true);
		}else{
			coverageBuilder = new CoverageBuilder();
		}
		final Analyzer analyzer = new Analyzer(data, coverageBuilder);
		for (final File f : classfiles) {
			analyzer.analyzeAll(f);
		}
		printNoMatchWarning(coverageBuilder.getNoMatchClasses(), out);
		return coverageBuilder.getBundle(name);
	}

	private void printNoMatchWarning(final Collection<IClassCoverage> nomatch,
									 final PrintWriter out) {
		if (!nomatch.isEmpty()) {
			out.println(
					"[WARN] Some classes do not match with execution data.");
			out.println(
					"[WARN] For report generation the same class files must be used as at runtime.");
			for (final IClassCoverage c : nomatch) {
				out.printf(
						"[WARN] Execution data for class %s does not match.%n",
						c.getName());
			}
		}
	}

	private void writeReports(final IBundleCoverage bundle,
							  final ExecFileLoader loader, final PrintWriter out)
			throws IOException {
		out.printf("[INFO] Analyzing %s classes.%n",
				Integer.valueOf(bundle.getClassCounter().getTotalCount()));
		final IReportVisitor visitor = createReportVisitor();
		visitor.visitInfo(loader.getSessionInfoStore().getInfos(),
				loader.getExecutionDataStore().getContents());
		visitor.visitBundle(bundle, getSourceLocator());
		visitor.visitEnd();
	}

	private IReportVisitor createReportVisitor() throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();

		if (xml != null) {
			final XMLFormatter formatter = new XMLFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(xml)));
		}

		if (csv != null) {
			final CSVFormatter formatter = new CSVFormatter();
			visitors.add(formatter.createVisitor(new FileOutputStream(csv)));
		}

		if (html != null) {
			final HTMLFormatter formatter = new HTMLFormatter();
			visitors.add(
					formatter.createVisitor(new FileMultiReportOutput(html)));
		}

		return new MultiReportVisitor(visitors);
	}

	private ISourceFileLocator getSourceLocator() {
		final MultiSourceFileLocator multi = new MultiSourceFileLocator(
				tabwidth);
		for (final File f : sourcefiles) {
			multi.add(new DirectorySourceFileLocator(f, encoding, tabwidth));
		}
		return multi;
	}

}
