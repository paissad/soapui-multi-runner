package net.paissad.tools.soapui;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.paissad.tools.soapui.exception.ProcessBuilderException;

public class PropertiesBuilderTest {

	private PropertiesBuilder	examplePropsBuilder;

	private PropertiesBuilder	emptyPropsBuilder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		examplePropsBuilder = new PropertiesBuilder();
		emptyPropsBuilder = new PropertiesBuilder();
	}

	private Path getExamplePropsPath() throws URISyntaxException {
		return Paths.get(PropertiesBuilderTest.class.getResource("/props/example.properties").toURI());
	}

	private Path getEmptyPropsPath() throws URISyntaxException {
		return Paths.get(PropertiesBuilderTest.class.getResource("/props/empty.properties").toURI());
	}

	@Test
	public void testBuildProjectProperties() throws ProcessBuilderException, URISyntaxException {

		String expectedResult = "-Pkey3= -Pkey2=val2 -Pkey1=val1 ";
		Assert.assertEquals(expectedResult, examplePropsBuilder.buildProjectProperties(getExamplePropsPath()).prettyPrint());
		Assert.assertTrue(examplePropsBuilder.getResult().size() == 6);

		Assert.assertTrue(emptyPropsBuilder.buildProjectProperties(getEmptyPropsPath()).prettyPrint().isEmpty());
		Assert.assertTrue(emptyPropsBuilder.getResult().isEmpty());
	}

	@Test
	public void testBuildGlobalProperties() throws ProcessBuilderException, URISyntaxException {

		String expectedResult = "-Gkey3= -Gkey2=val2 -Gkey1=val1 ";
		Assert.assertEquals(expectedResult, examplePropsBuilder.buildGlobalProperties(getExamplePropsPath()).prettyPrint());
		Assert.assertTrue(examplePropsBuilder.getResult().size() == 6);

		Assert.assertTrue(emptyPropsBuilder.buildProjectProperties(getEmptyPropsPath()).prettyPrint().isEmpty());
		Assert.assertTrue(emptyPropsBuilder.getResult().isEmpty());
	}

	@Test
	public void testBuildSystemProperties() throws ProcessBuilderException, URISyntaxException {

		String expectedResult = "-Dkey3= -Dkey2=val2 -Dkey1=val1 ";
		Assert.assertEquals(expectedResult, examplePropsBuilder.buildSystemProperties(getExamplePropsPath()).prettyPrint());
		Assert.assertTrue(examplePropsBuilder.getResult().size() == 6);

		Assert.assertTrue(emptyPropsBuilder.buildProjectProperties(getEmptyPropsPath()).prettyPrint().isEmpty());
		Assert.assertTrue(emptyPropsBuilder.getResult().isEmpty());
	}

}
