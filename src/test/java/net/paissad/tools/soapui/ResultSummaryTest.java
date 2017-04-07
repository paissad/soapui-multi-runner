package net.paissad.tools.soapui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResultSummaryTest {

	private ResultSummary resultSummary;

	@Before
	public void setUp() throws Exception {
		final String intput = getSampleOutput("/output_samples/soapui_output_1.txt");
		this.resultSummary = ResultSummary.build(intput);
	}

	private String getSampleOutput(final String sampleFile) throws URISyntaxException, IOException {
		return new String(Files.readAllBytes((Paths.get(ResultSummaryTest.class.getResource(sampleFile).toURI()))));
	}

	@Test
	public void testAddTestsSuites() {
		int add = 4;
		int beforeAdd = this.resultSummary.getTestSuitesCount();
		this.resultSummary.addTestsSuites(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getTestSuitesCount());
	}

	@Test
	public void testAddTestCases() {
		int add = 5;
		int beforeAdd = this.resultSummary.getTestCasesCount();
		this.resultSummary.addTestCases(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getTestCasesCount());
	}

	@Test
	public void testAddFailedTestsCases() {
		int add = 6;
		int beforeAdd = this.resultSummary.getFailedTestCasesCount();
		this.resultSummary.addFailedTestsCases(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getFailedTestCasesCount());
	}

	@Test
	public void testAddTestSteps() {
		int add = 7;
		int beforeAdd = this.resultSummary.getTestStepsCount();
		this.resultSummary.addTestSteps(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getTestStepsCount());
	}

	@Test
	public void testAddRequestAssertions() {
		int add = 8;
		int beforeAdd = this.resultSummary.getRequestAsserstionsCount();
		this.resultSummary.addRequestAssertions(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getRequestAsserstionsCount());
	}

	@Test
	public void testAddFailedAssertions() {
		int add = 9;
		int beforeAdd = this.resultSummary.getFailedAssertionsCount();
		this.resultSummary.addFailedAssertions(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getFailedAssertionsCount());
	}

	@Test
	public void testAddExportedResults() {
		int add = 10;
		int beforeAdd = this.resultSummary.getExportedResultsCount();
		this.resultSummary.addExportedResults(add);
		Assert.assertEquals(beforeAdd + add, this.resultSummary.getExportedResultsCount());
	}

	@Test
	public void testMerge() throws URISyntaxException, IOException {
		final String input2 = getSampleOutput("/output_samples/soapui_output_2.txt");
		ResultSummary rs2 = ResultSummary.build(input2);
		this.resultSummary.merge(rs2);
		Assert.assertEquals(3, this.resultSummary.getTestSuitesCount());
		Assert.assertEquals(7, this.resultSummary.getTestCasesCount());
		Assert.assertEquals(2, this.resultSummary.getFailedTestCasesCount());
		Assert.assertEquals(9, this.resultSummary.getTestStepsCount());
		Assert.assertEquals(7, this.resultSummary.getRequestAsserstionsCount());		
		Assert.assertEquals(6, this.resultSummary.getFailedAssertionsCount());
		Assert.assertEquals(20, this.resultSummary.getExportedResultsCount());
	}

	@Test
	public void testMergeWithNullInput() throws URISyntaxException, IOException {
		this.resultSummary.merge(null);
		Assert.assertEquals(this.resultSummary, this.resultSummary);
	}

	@Test
	public void testMergeWithEmptyInput() throws URISyntaxException, IOException {
		final ResultSummary rs2 = ResultSummary.build("  ");
		this.resultSummary.merge(rs2);
		Assert.assertEquals(this.resultSummary, this.resultSummary);
	}

	@Test
	public void testUpdateExitCode() {
		this.resultSummary.updateExitCode(255);
		Assert.assertEquals(255, this.resultSummary.getExitCode());
		this.resultSummary.updateExitCode(3);
		Assert.assertEquals(3, this.resultSummary.getExitCode());
		this.resultSummary.updateExitCode(0);
		Assert.assertEquals(0, this.resultSummary.getExitCode());
	}

	@Test
	public void testBuild() throws URISyntaxException, IOException {
		// This test is not need here as long as all getters are testeds :-)
	}

	@Test
	public void testPrettyPrint() {
		// Simply call the method and expect there is not exception.
		ResultSummary.prettyPrint(this.resultSummary);
	}

	@Test
	public void testGetTestSuitesCount() {
		Assert.assertEquals(1, this.resultSummary.getTestSuitesCount());
	}

	@Test
	public void testGetTestCasesCount() {
		Assert.assertEquals(3, this.resultSummary.getTestCasesCount());
	}

	@Test
	public void testGetFailedTestCasesCount() {
		Assert.assertEquals(0, this.resultSummary.getFailedTestCasesCount());
	}

	@Test
	public void testGetTestStepsCount() {
		Assert.assertEquals(4, this.resultSummary.getTestStepsCount());
	}

	@Test
	public void testGetRequestAsserstionsCount() {
		Assert.assertEquals(5, this.resultSummary.getRequestAsserstionsCount());
	}

	@Test
	public void testGetFailedAssertionsCount() {
		Assert.assertEquals(6, this.resultSummary.getFailedAssertionsCount());
	}

	@Test
	public void testGetExportedResultsCount() {
		Assert.assertEquals(9, this.resultSummary.getExportedResultsCount());
	}

	@Test
	public void testGetExitCode() {
		Assert.assertEquals(0, this.resultSummary.getExitCode());
	}

}
