package net.paissad.tools.soapui;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

@Getter
public class ResultSummary {

	private static final String	MSG_BAR					= "==================================================================";

	private static final Logger	LOGGER					= LoggerFactory.getLogger(ResultSummary.class);

	private int					testSuitesCount			= 0;

	private int					testCasesCount			= 0;

	private int					failedTestCasesCount	= 0;

	private int					testStepsCount			= 0;

	private int					requestAsserstionsCount	= 0;

	private int					failedAssertionsCount	= 0;

	private int					exportedResultsCount	= 0;

	private int					exitCode				= 0;

	public ResultSummary() {
		// Initialization already done !
	}

	public synchronized ResultSummary addTestsSuites(final int add) {
		this.testSuitesCount += add;
		return this;
	}

	public synchronized ResultSummary addTestCases(final int add) {
		this.testCasesCount += add;
		return this;
	}

	public synchronized ResultSummary addFailedTestsCases(final int add) {
		this.failedTestCasesCount += add;
		return this;
	}

	public synchronized ResultSummary addTestSteps(final int add) {
		this.testStepsCount += add;
		return this;
	}

	public synchronized ResultSummary addRequestAssertions(final int add) {
		this.requestAsserstionsCount += add;
		return this;
	}

	public synchronized ResultSummary addFailedAssertions(final int add) {
		this.failedAssertionsCount += add;
		return this;
	}

	public synchronized ResultSummary addExportedResults(final int add) {
		this.exportedResultsCount += add;
		return this;
	}

	/**
	 * @param resultSummary - The {@link #ResultSummary} to merge with this instance.
	 * @return the merged instance.
	 */
	public ResultSummary merge(final ResultSummary resultSummary) {
		if (resultSummary != null) {
			this.addTestsSuites(resultSummary.getTestSuitesCount());
			this.addTestCases(resultSummary.getTestCasesCount());
			this.addFailedTestsCases(resultSummary.getFailedTestCasesCount());
			this.addTestSteps(resultSummary.getTestStepsCount());
			this.addRequestAssertions(resultSummary.getRequestAsserstionsCount());
			this.addFailedAssertions(resultSummary.getFailedAssertionsCount());
			this.addExportedResults(resultSummary.getExportedResultsCount());
			this.updateExitCode(resultSummary.getExitCode());
		}
		return this;
	}

	public synchronized ResultSummary updateExitCode(int exitCode) {
		if (exitCode != 0) {
			this.exitCode = exitCode; // Always keep the last specified exit code if different of 0 !
		}
		return this;
	}

	/**
	 * Builds an instance of {@link #ResultSummary} from the specified string input.
	 * 
	 * @param input - The testrunner output.
	 * @return A new instance of {@link #ResultSummary} built from the specified input.
	 */
	public static ResultSummary build(final String input) {

		final ResultSummary resultSummary = new ResultSummary();

		if (input != null && !input.trim().isEmpty()) {
			resultSummary.addTestsSuites(Integer.valueOf(input.replaceAll("(?s).*\\bTotal TestSuites\\s*:\\s*(\\d+)\\b.*", "$1")));
			resultSummary.addTestCases(Integer.valueOf(input.replaceAll("(?s).*\\bTotal TestCases\\s*:\\s*(\\d+)\\b.*", "$1")));
			resultSummary.addFailedTestsCases(
			        Integer.valueOf(input.replaceAll("(?s).*\\bTotal TestCases\\s*:\\s*\\d+\\b\\s*\\((\\d+)\\s+failed\\s*\\).*", "$1")));
			resultSummary.addTestSteps(Integer.valueOf(input.replaceAll("(?s).*\\bTotal TestSteps\\s*:\\s*(\\d+)\\b.*", "$1")));
			resultSummary.addRequestAssertions(Integer.valueOf(input.replaceAll("(?s).*\\bTotal Request Assertions\\s*:\\s*(\\d+)\\b.*", "$1")));
			resultSummary.addFailedAssertions(Integer.valueOf(input.replaceAll("(?s).*\\bTotal Failed Assertions\\s*:\\s*(\\d+)\\b.*", "$1")));
			resultSummary.addExportedResults(Integer.valueOf(input.replaceAll("(?s).*\\bTotal Exported Results\\s*:\\s*(\\d+)\\b.*", "$1")));
		}

		return resultSummary;
	}

	/**
	 * Pretty prints the summary.
	 * 
	 * @param resultSummary - The summary to print.
	 */
	public static void prettyPrint(final ResultSummary resultSummary) {
		LOGGER.info(MSG_BAR);
		LOGGER.info("SOAPUI MultiTestRunner (" + getAppVersion() + ") : Results Summary");
		LOGGER.info(MSG_BAR);
		LOGGER.info("Total TestSuites          : {}", resultSummary.getTestSuitesCount());
		LOGGER.info("Total TestCases           : {} ({} failed)", resultSummary.getTestCasesCount(), resultSummary.getFailedTestCasesCount());
		LOGGER.info("Total TestSteps           : {}", resultSummary.getTestStepsCount());
		LOGGER.info("Total Request Assertions  : {}", resultSummary.getRequestAsserstionsCount());
		LOGGER.info("Total Failed Assertions   : {}", resultSummary.getFailedAssertionsCount());
		LOGGER.info("Total Exported Results    : {}", resultSummary.getExportedResultsCount());
		LOGGER.info(MSG_BAR);
	}

	private static String getAppVersion() {

		try (final InputStream in = ResultSummary.class
		        .getResourceAsStream("/META-INF/maven/net.paissad.tools/soapui-multi-testrunner/pom.properties");) {

			Properties mavenProps = new Properties();
			mavenProps.load(in);
			return (String) mavenProps.get("version");

		} catch (Exception e) {
			LOGGER.error("Error while retrieving App version", e); // should never happen though !
			return "UNKNOWN";
		}
	}

}
