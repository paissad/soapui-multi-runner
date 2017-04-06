package net.paissad.tools.soapui;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class MultiTestRunnerOptionsTest {

	private MultiTestRunnerOptions	multiTestRunnerOptions;

	private CmdLineParser			parser;

	@Before
	public void setUp() throws Exception {
		multiTestRunnerOptions = new MultiTestRunnerOptions();
		multiTestRunnerOptions.parseOptions("--trp", "dummyTestrunnerpath", "--in", "dummyIn", "--out", "dummyOut", "--log-level", "dummyLoglevel",
		        "--settings", "dummySettings", "arg1", "arg2");
		parser = new CmdLineParser(this);
	}

	@Test
	public void testParseOptions() throws CmdLineException {
		multiTestRunnerOptions.parseOptions("--help", "--settings", "aa");
	}

	@Test
	public void testPrintUsage() {
		MultiTestRunnerOptions.printUsage(parser);
	}

	@Test
	public void testIsHelp() throws CmdLineException {
		multiTestRunnerOptions.parseOptions("-h");
		Assert.assertTrue(multiTestRunnerOptions.isHelp());
	}

	@Test
	public void testGetTestRunnerPath() throws CmdLineException {
		Assert.assertEquals("dummyTestrunnerpath", multiTestRunnerOptions.getTestRunnerPath());
	}

	@Test
	public void testGetSettingsFile() {
		Assert.assertEquals("dummySettings", multiTestRunnerOptions.getSettingsFile());
	}

	@Test
	public void testGetOutputFolder() {
		Assert.assertEquals("dummyOut", multiTestRunnerOptions.getOutputFolder());
	}

	@Test
	public void testGetProjectsDir() {
		Assert.assertEquals("dummyIn", multiTestRunnerOptions.getProjectsDir());
	}

	@Test
	public void testGetLogLevel() {
		Assert.assertEquals("dummyLoglevel", multiTestRunnerOptions.getLogLevel());
	}

	@Test
	public void testGetArguments() {
		Assert.assertEquals(Arrays.asList(new String[] { "arg1", "arg2" }), multiTestRunnerOptions.getArguments());
	}

	@Test(expected = CmdLineException.class)
	public void testUnrecognizedOptions() throws CmdLineException {
		multiTestRunnerOptions.parseOptions("--unknown-option");
	}

	@Test
	public void testUnrecognizedOptionsAndHelpOption() throws CmdLineException {
		multiTestRunnerOptions.parseOptions("unknown-option", "-h");
	}

}
