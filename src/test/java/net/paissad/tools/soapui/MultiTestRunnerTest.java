package net.paissad.tools.soapui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kohsuke.args4j.CmdLineException;

import net.paissad.tools.soapui.exception.MultiTestRunnerException;
import net.paissad.tools.soapui.exception.ProcessBuilderException;

public class MultiTestRunnerTest {

	private MultiTestRunner		multiTestRunner;

	private Path				genuineSoapuiTestrunnerPath;

	/** Directory where to to store the generated SoapUI results */
	private Path				projectOutputPath;

	@Rule
	public ExpectedException	thrown	= ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.multiTestRunner = new MultiTestRunner();
		this.projectOutputPath = Files.createTempDirectory("_multitestrunner_output_");
		FileUtils.forceDeleteOnExit(this.projectOutputPath.toFile());
		final String scriptName = "testrunner" + getGenuineSoapuiTestrunnerExtension();
		genuineSoapuiTestrunnerPath = Paths.get(".", "tools/soapui/" + getOsName() + "/bin/" + scriptName);
		genuineSoapuiTestrunnerPath.toFile().setReadable(true);
		genuineSoapuiTestrunnerPath.toFile().setExecutable(true);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(this.projectOutputPath.toFile());
	}

	// @Test
	// public void testMainNominalCase() throws MultiTestRunnerException, URISyntaxException {
	// final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
	// MultiTestRunner.main(setupOptionsList);
	// } // FIXME: update code !

	@Test
	public void testMainWrongOptionGiven() throws MultiTestRunnerException, URISyntaxException {
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		setupOptionsList.add("--very-bad-option");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(1, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainHelpOptionPassed() throws MultiTestRunnerException, URISyntaxException {
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		setupOptionsList.add("-h");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(0, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainLogLevelOptionPassed() throws MultiTestRunnerException, URISyntaxException {
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		setupOptionsList.add("--log-level");
		setupOptionsList.add("ERROR");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(0, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainSettingsOptionPassed() throws MultiTestRunnerException, URISyntaxException {
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		setupOptionsList.add("--settings");
		final Path settingsPath = Paths.get(MultiTestRunnerTest.class.getResource("/sample-soapui-settings.xml").toURI());
		setupOptionsList.add(settingsPath.toString());
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(0, this.multiTestRunner.proxyMain(args));
		Assert.assertNotNull(this.multiTestRunner.getOptions().getSettingsFile());
		Assert.assertEquals(settingsPath.toString(), this.multiTestRunner.getOptions().getSettingsFile().toString());
	}

	@Test
	public void testMainGenuineSoapuiTestrunnerIsNotAFile() throws URISyntaxException, MultiTestRunnerException, IOException {
		try {
			genuineSoapuiTestrunnerPath = Files.createTempDirectory("");
			FileUtils.forceDeleteOnExit(genuineSoapuiTestrunnerPath.toFile());

			final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
			final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
			Assert.assertEquals(2, this.multiTestRunner.proxyMain(args));

		} finally {
			FileUtils.deleteQuietly(genuineSoapuiTestrunnerPath.toFile());
		}
	}

	@Test
	public void testMainGenuineSoapuiTestrunnerIsNotReadable() throws URISyntaxException, MultiTestRunnerException {
		genuineSoapuiTestrunnerPath.toFile().setReadable(false);
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(2, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainGenuineSoapuiTestrunnerIsNotExecutable() throws URISyntaxException, MultiTestRunnerException {
		genuineSoapuiTestrunnerPath.toFile().setExecutable(false);
		final List<String> setupOptionsList = getSetupOptionsAsList("project_one_project_props");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(2, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainProjectDirIsNotADirectory() throws URISyntaxException, MultiTestRunnerException {
		final List<String> setupOptionsList = getSetupOptionsAsList("project_should_be_dir.txt");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		Assert.assertEquals(3, this.multiTestRunner.proxyMain(args));
	}

	@Test
	public void testMainSoapuiProjectIsNotAFile() throws URISyntaxException, MultiTestRunnerException {
		thrown.expect(MultiTestRunnerException.class);
		thrown.expectMessage("Project-1-soapui-project.xml is not file");
		final List<String> setupOptionsList = getSetupOptionsAsList("soapui_project_is_dir");
		final String[] args = setupOptionsList.stream().toArray(size -> new String[size]);
		this.multiTestRunner.proxyMain(args);
	}

	/**
	 * Test for the method {@link MultiTestRunner#addDefaultSystemAndGlobalProps(MultiTestRunnerOptions, List)}
	 * 
	 * @throws ProcessBuilderException
	 * @throws CmdLineException
	 * @throws URISyntaxException
	 */
	@Test
	public void testAddDefaultSystemAndGlobalPropsWhenNoPropsExist() throws ProcessBuilderException, CmdLineException, URISyntaxException {
		final List<String> runnerCommand = getSetUpCommand();
		this.multiTestRunner.addDefaultSystemAndGlobalProps(getSetUpOptions("project_no_props"), runnerCommand);
		Assert.assertEquals(getSetUpCommand(), runnerCommand);
	}

	/**
	 * Test for the method {@link MultiTestRunner#addDefaultSystemAndGlobalProps(MultiTestRunnerOptions, List)}
	 * 
	 * @throws ProcessBuilderException
	 * @throws CmdLineException
	 * @throws URISyntaxException
	 */
	@Test
	public void testAddDefaultSystemAndGlobalPropsWhenOnlyGlobalPropsExist() throws ProcessBuilderException, CmdLineException, URISyntaxException {
		final List<String> runnerCommand = getSetUpCommand();
		this.multiTestRunner.addDefaultSystemAndGlobalProps(getSetUpOptions("project_one_global_props"), runnerCommand);
		Assert.assertTrue("wrong global property prefix", runnerCommand.contains("-G"));
		Assert.assertTrue("wrong global property key=val", runnerCommand.contains("key1=val1"));
	}

	/**
	 * Test for the method {@link MultiTestRunner#addDefaultSystemAndGlobalProps(MultiTestRunnerOptions, List)}
	 * 
	 * @throws ProcessBuilderException
	 * @throws CmdLineException
	 * @throws URISyntaxException
	 */
	@Test
	public void testAddDefaultSystemAndGlobalPropsWhenOnlySystemPropsExist() throws ProcessBuilderException, CmdLineException, URISyntaxException {
		final List<String> runnerCommand = getSetUpCommand();
		this.multiTestRunner.addDefaultSystemAndGlobalProps(getSetUpOptions("project_one_system_props"), runnerCommand);
		Assert.assertTrue("wrong system property prefix", runnerCommand.contains("-D"));
		Assert.assertTrue("wrong system property key=val", runnerCommand.contains("key2=val2"));
	}

	@Test
	public void testGetOptions() throws CmdLineException, URISyntaxException {
		Assert.assertNotNull(getSetUpOptions("project_no_props"));
	}

	/**
	 * @param projectSampleName
	 * @return The options to pass to the "soapui multi testrunner" according to the sample project to use.
	 * @throws CmdLineException
	 * @throws URISyntaxException
	 */
	private MultiTestRunnerOptions getSetUpOptions(final String projectSampleName) throws CmdLineException, URISyntaxException {
		this.multiTestRunner.getOptions()
		        .parseOptions((String[]) getSetupOptionsAsList(projectSampleName).stream().toArray(size -> new String[size]));
		return this.multiTestRunner.getOptions();
	}

	/**
	 * @param projectSampleName
	 * @return The options to pass to the "soapui multi testrunner" according to the sample project to use.
	 * @throws CmdLineException
	 * @throws URISyntaxException
	 */
	private List<String> getSetupOptionsAsList(final String projectSampleName) throws URISyntaxException {
		final Path projectSamplePath = getProjectDirSample(projectSampleName);
		return new LinkedList<>(Arrays.asList(new String[] { "--trp", genuineSoapuiTestrunnerPath.toString(), "--in", projectSamplePath.toString(),
		        "--out", this.projectOutputPath.toString() }));
	}

	/**
	 * @return
	 */
	private List<String> getSetUpCommand() {
		final List<String> command = new LinkedList<>(Arrays.asList("-r", "-a", "-j", "-J"));
		return command;
	}

	/**
	 * @param projectSampleName
	 * @return The path of a project according to the specified sample name.
	 * @throws URISyntaxException
	 */
	private Path getProjectDirSample(final String projectSampleName) throws URISyntaxException {
		return Paths.get(MultiTestRunnerTest.class.getResource("/project_samples/" + projectSampleName).toURI());
	}

	private static String getOsName() {
		final String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
		String dirname = null;
		if (osName.contains("mac os")) {
			dirname = "osx";
		} else if (osName.contains("windows")) {
			dirname = "win";
		} else if (osName.contains("linux")) {
			dirname = "linux";
		} else {
			dirname = "__unsupported__os__";
		}
		return dirname;
	}

	private static String getGenuineSoapuiTestrunnerExtension() {
		return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows") ? ".bat" : ".sh";
	}

}
