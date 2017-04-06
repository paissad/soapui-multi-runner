package net.paissad.tools.soapui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.Getter;
import net.paissad.tools.soapui.exception.ProcessBuilderException;

public class PropertiesBuilder {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(PropertiesBuilder.class);

	@Getter
	private List<String>		result;

	@Getter(value = AccessLevel.PRIVATE)
	private StringBuilder		prettyResult;

	public PropertiesBuilder() {
		this.result = new LinkedList<>();
		this.prettyResult = new StringBuilder();
	}

	public PropertiesBuilder buildProjectProperties(final Path propertiesPath) throws ProcessBuilderException {
		return buildProperties(propertiesPath, "-P");
	}

	public PropertiesBuilder buildGlobalProperties(final Path propertiesPath) throws ProcessBuilderException {
		return buildProperties(propertiesPath, "-G");
	}

	public PropertiesBuilder buildSystemProperties(final Path propertiesPath) throws ProcessBuilderException {
		return buildProperties(propertiesPath, "-D");
	}

	private PropertiesBuilder buildProperties(final Path propertiesPath, final String prefix) throws ProcessBuilderException {

		try (final InputStream in = Files.newInputStream(propertiesPath)) {

			final Properties p = new Properties();
			p.load(in);
			Iterator<Entry<Object, Object>> iterator = p.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<Object, Object> entry = iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();

				getResult().add(prefix);
				getPrettyResult().append(prefix);

				getResult().add(key + "=" + value);
				getPrettyResult().append(key).append("=").append(value).append(" ");
			}

			return this;

		} catch (IOException ioe) {
			String errMsg = "Error while processing properties file " + propertiesPath.normalize().toString();
			LOGGER.error(errMsg, ioe);
			throw new ProcessBuilderException(errMsg, ioe);
		}
	}

	public String prettyPrint() {
		return getPrettyResult().toString();
	}

}
