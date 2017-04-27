package net.paissad.tools.soapui;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import net.paissad.tools.soapui.exception.MultiTestRunnerException;
import net.paissad.tools.soapui.exception.ProcessBuilderException;

public class MultiTestRunner {

	private static final Logger		LOGGER						= (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MultiTestRunner.class);

	private static final String		SOAPUI_PROJECT_LONG_SUFFIX	= "-soapui-project.xml";

	private static final String		MULTI_TEST_RUNNER_TAG		= "[MultiTestRunner]";

	@Getter
	private MultiTestRunnerOptions	options;

	public MultiTestRunner() {
		this.options = new MultiTestRunnerOptions();
	}

	public static void main(final String[] args) throws MultiTestRunnerException {
		System.exit(new MultiTestRunner().proxyMain(args));
	}

	public int proxyMain(String[] args) throws MultiTestRunnerException {

		final int ok = 0;

		setLoggingLevel("INFO");

		CmdLineParser parser = null;
		try {
			parser = getRunner().getOptions().parseOptions(args);
		} catch (CmdLineException e1) {
			LOGGER.trace("An error occured while parsing the options", e1);
			return 1;
		}

		if (getRunner().getOptions().isHelp()) {
			MultiTestRunnerOptions.printUsage(parser);
			return ok;
		}

		// Sets the log level if specified
		if (getRunner().getOptions().getLogLevel() != null) {
			setLoggingLevel(getRunner().getOptions().getLogLevel());
		}

		// Check the testrunner requirements
		final Path testRunnerPath = Paths.get(getRunner().getOptions().getTestRunnerPath());
		if (!(testRunnerPath.toFile().isFile() && Files.isReadable(testRunnerPath) && Files.isExecutable(testRunnerPath))) {
			LOGGER.error(MULTI_TEST_RUNNER_TAG + " " + testRunnerPath + " is not a file, or is not readable or not executable !");
			return 2;
		}

		// Parse the projects directory and execute testrunner for each *-soapui-project.xml file
		final Path projectsDirPath = Paths.get(getRunner().getOptions().getProjectsDir());
		if (!projectsDirPath.toFile().isDirectory()) {
			LOGGER.error(MULTI_TEST_RUNNER_TAG + " " + projectsDirPath + " is not a directory");
			return 3;

		} else {
			try (final DirectoryStream<Path> stream = Files.newDirectoryStream(projectsDirPath, "*" + SOAPUI_PROJECT_LONG_SUFFIX)) {

				final List<String> minimalCommand = new LinkedList<>(
				        Arrays.asList(new String[] { testRunnerPath.normalize().toString(), "-r", "-a", "-j", "-J" }));

				minimalCommand.add("-f");
				minimalCommand.add(Paths.get(getRunner().getOptions().getOutputFolder()).normalize().toString());

				if (getRunner().getOptions().getSettingsFile() != null) {
					minimalCommand.add("-t");
					minimalCommand.add(Paths.get(getRunner().getOptions().getSettingsFile()).normalize().toString());

				} else if (getRunner().getProjectsSettingsFilePath().toFile().isFile()) {
					minimalCommand.add("-t");
					minimalCommand.add(getRunner().getProjectsSettingsFilePath().toString());

				} else {
					// No specified settings, and no default settings available.
				}

				// Add default system & global properties located in 'settings' directory, if they exist
				getRunner().addDefaultSystemAndGlobalProps(getRunner().getOptions(), minimalCommand);

				final ResultSummary globalResultSummary = new ResultSummary();
				for (final Path path : stream) {
					LOGGER.info(MULTI_TEST_RUNNER_TAG + " =============================================================================");
					LOGGER.info(MULTI_TEST_RUNNER_TAG + " Picking SOAPUI Project : " + path.getFileName());
					LOGGER.info(MULTI_TEST_RUNNER_TAG + " =============================================================================");

					final ResultSummary rs = getRunner().executeSoapuiProject(path, minimalCommand);
					globalResultSummary.merge(rs);
				}

				ResultSummary.prettyPrint(globalResultSummary);

				return globalResultSummary.getExitCode();

			} catch (IOException | ProcessBuilderException e) {
				final String errMsg = "Error while parsing projects directory ...";
				LOGGER.error(MULTI_TEST_RUNNER_TAG + " " + errMsg);
				throw new MultiTestRunnerException(errMsg, e);
			}
		}
	}

	/**
	 * Executes the testrunner script onto the specified <code>projectPath</code> file.
	 * 
	 * @param projectPath - The SoapUI project to execute.
	 * @param command - The command to use for executing the SoapUI project.
	 * @return The {@link ResultSummary}
	 * @throws MultiTestRunnerException
	 */
	private ResultSummary executeSoapuiProject(final Path projectPath, final List<String> command) throws MultiTestRunnerException {

		if (projectPath.toFile().isFile()) {
			try {
				List<String> runnerCommand = new ArrayList<>(command);
				runnerCommand.add(projectPath.normalize().toString());

				// Add properties according to the place & naming conventions
				addPropertiesIfNecessary(projectPath, runnerCommand, PROPERTY_TYPE.SYSTEM);
				addPropertiesIfNecessary(projectPath, runnerCommand, PROPERTY_TYPE.GLOBAL);
				addPropertiesIfNecessary(projectPath, runnerCommand, PROPERTY_TYPE.PROJECT);

				final ProcessResult processResult = new ProcessExecutor().destroyOnExit().command(runnerCommand).redirectOutput(System.out)
				        .readOutput(true).execute();

				final ResultSummary rs = ResultSummary.build(processResult.outputString());
				rs.updateExitCode(processResult.getExitValue());

				return rs;

			} catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException | NumberFormatException
			        | ProcessBuilderException e) {
				final String errMsg = "Error while running " + projectPath.getFileName();
				LOGGER.error(MULTI_TEST_RUNNER_TAG + " " + errMsg);
				throw new MultiTestRunnerException(errMsg, e);
			}
		} else {
			final String errMsg = MULTI_TEST_RUNNER_TAG + " " + projectPath.getFileName() + " is not file.";
			LOGGER.error(errMsg);
			throw new MultiTestRunnerException(errMsg);
		}
	}

	/**
	 * Populate the command to execute if ever the default 'settings/system.properties' and/or 'settings/global.properties' files exist.
	 * 
	 * @param options
	 * @param runnerCommand
	 * @throws ProcessBuilderException
	 */
	public void addDefaultSystemAndGlobalProps(final MultiTestRunnerOptions options, final List<String> runnerCommand)
	        throws ProcessBuilderException {

		LOGGER.debug("Checking for default system.properties file");
		final Path defaultSystemPropertiesPath = Paths.get(options.getProjectsDir(), "settings/system.properties");
		if (defaultSystemPropertiesPath.toFile().isFile()) {
			LOGGER.info("Found default global properties file : {}", defaultSystemPropertiesPath);
			addProperties(defaultSystemPropertiesPath, runnerCommand, PROPERTY_TYPE.SYSTEM);

		} else {
			LOGGER.debug("No default system properties file found !");
		}

		LOGGER.debug("Checking for default global.properties file");
		final Path defaultGlobalPropertiesPath = Paths.get(options.getProjectsDir(), "settings/global.properties");
		if (defaultGlobalPropertiesPath.toFile().isFile()) {
			LOGGER.info("Found default global properties file : {}", defaultGlobalPropertiesPath);
			addProperties(defaultGlobalPropertiesPath, runnerCommand, PROPERTY_TYPE.GLOBAL);

		} else {
			LOGGER.debug("No default global properties file found !");
		}
	}

	private void addPropertiesIfNecessary(final Path projectPath, final List<String> command, final PROPERTY_TYPE type)
	        throws ProcessBuilderException {

		// Check the existence of the properties file , first !
		final Path propertiesPath = Paths.get(projectPath.getParent().normalize().toString(), projectPath.getFileName().toString()
		        .replaceAll("(.*?)" + SOAPUI_PROJECT_LONG_SUFFIX + ".*", "$1." + type.name().toLowerCase() + ".properties"));

		if (propertiesPath.toFile().isFile() && Files.isReadable(propertiesPath)) {

			LOGGER.debug("{} properties file found for project {}", type.name(), projectPath.getFileName());
			LOGGER.debug("Loading {} properties file {}", type.name(), propertiesPath.toString());

			addProperties(propertiesPath, command, type);

		} else {

			LOGGER.debug("No {} properties file for {}", type.name(), projectPath.getFileName());
		}
	}

	/**
	 * Reads the specified properties file, assign the properties to the specified property type flag, and add them to the command.
	 * 
	 * @param propertiesPath - The properties file.
	 * @param command - The command to populate
	 * @param type - The type of properties.
	 * @throws ProcessBuilderException
	 */
	private void addProperties(final Path propertiesPath, final List<String> command, final PROPERTY_TYPE type) throws ProcessBuilderException {

		PropertiesBuilder propsBuilder = new PropertiesBuilder();

		switch (type) {
		case SYSTEM:
			propsBuilder.buildSystemProperties(propertiesPath);
			break;

		case GLOBAL:
			propsBuilder.buildGlobalProperties(propertiesPath);
			break;

		case PROJECT:
			propsBuilder.buildProjectProperties(propertiesPath);
			break;

		default:
			LOGGER.error("The properties type {} is unknown !!!", type);
			return;
		}

		command.addAll(propsBuilder.getResult());
	}

	private MultiTestRunner getRunner() {
		return this;
	}

	private Path getProjectsSettingsFilePath() {
		return Paths.get(getOptions().getProjectsDir(), "settings/soapui-settings.xml").normalize();
	}

	private static void setLoggingLevel(final String logLevel) {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final ch.qos.logback.classic.Logger appLogger = loggerContext.getLogger("net.paissad.tools.soapui");
		appLogger.setLevel(Level.valueOf(logLevel));
	}

	private enum PROPERTY_TYPE {
		SYSTEM, GLOBAL, PROJECT, OTHER;
	}
}
