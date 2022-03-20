/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal;

import org.junit.Test;

/**
 * Unit tests for {@link Main}.
 */
public class MainTest extends CommandTestBase {

	@Test
	public void should_print_usage_when_no_arguments_given() throws Exception {
		execute();

		assertFailure();
		assertNoOutput(out);
		assertContains("\"<command>\"", err);
		assertContains("Usage: java -jar jacococli.jar --help | <command>",
				err);
		assertContains("Command line interface for JaCoCo.", err);
	}

	@Test
	public void should_print_error_message_when_invalid_command_is_given()
			throws Exception {
		execute("foo");

		assertFailure();
		assertNoOutput(out);
		assertContains("\"foo\" is not a valid value for \"<command>\"", err);
		assertContains("Usage: java -jar jacococli.jar --help | <command>",
				err);
	}

	@Test
	public void should_print_general_usage_when_help_option_is_given()
			throws Exception {
		execute("--help");

		assertOk();
		assertNoOutput(err);
		assertContains("Usage: java -jar jacococli.jar --help | <command>",
				out);
		assertContains("<command> : dump|instrument|merge|report", out);
	}

	@Test
	public void should_print_command_usage_when_command_and_help_option_is_given()
			throws Exception {
		execute("dump", "--help");

		assertOk();
		assertNoOutput(err);
		assertContains("Usage: java -jar jacococli.jar dump", out);
		assertContains(
				"Request execution data from a JaCoCo agent running in 'tcpserver' output mode.",
				out);
	}

	@Test
	public void should_not_print_any_output_when_quiet_option_is_given()
			throws Exception {
		execute("version", "--quiet");

		assertOk();
		assertNoOutput(out);
		assertNoOutput(err);
	}

	@Test
	public void test_version() throws Exception {
		execute("version","--help");
		System.out.println(out);
		System.out.println(err);
	}

	@Test
	public void test_report() throws Exception {
		execute("report","/Users/han/git_repo/smartunitmng/jacoco.exec","--classfiles", "/Users/han/git_repo/smartunitmng/app/service/target/classes", "--html", "/Users/han/git_repo/smartunitmng/report");
		System.out.println(out);
		System.out.println(err);
	}

	@Test
	public void test_report_diff() throws Exception {
		execute("report","/Users/han/git_repo/smartunitmng/jacoco.exec",
				"--classfiles","/Users/han/git_repo/smartunitmng/app/service/target/classes",
				"--classfiles","/Users/han/git_repo/smartunitmng/app/model/target/classes",
				"--sourcefiles","/Users/han/git_repo/smartunitmng/app/utils/src/main/java",
				"--sourcefiles","/Users/han/git_repo/smartunitmng/app/service/src/main/java",
				"--sourcefiles","/Users/han/git_repo/smartunitmng/app/web/src/main/java",
				"--projectPath" , "/Users/han/git_repo/smartunitmng",
//				"--revision", "8baaa4becbb185272c79c39e2684d5477242fdae",
//				"--baseRevision" , "b01e9c1631316b518b33c6735836e0045a536071",
				"--html", "/Users/han/git_repo/smartunitmng/report",
				"--xml","report.xml",
				"--excludes","**/test/**,**/dal/**,**/ibatis/**,**/mybatis/**,**/model/**,**/enums/**,**/enum/**,**/log/**,**/constants/**,**/vo/**,**/*Controller*",
				"--csv","report.csv");
		System.out.println(out);
		System.out.println(err);
	}

}
