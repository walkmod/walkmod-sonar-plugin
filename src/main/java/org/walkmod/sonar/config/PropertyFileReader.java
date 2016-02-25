/*
 * SYSTEMi Copyright © 2015, MetricStream, Inc. All rights reserved.
 * 
 * Walkmod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Walkmod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mohanasundar N(mohanasundar.n@metricstream.com)
 * created 05/01/2015
 */

package org.walkmod.sonar.config;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

/**
 * The Class PropertyFileReader.
 * 
 * @author mohanasundar.n
 *
 */
public class PropertyFileReader {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(PropertyFileReader.class.getName());

	/** The Constant cacheMap. */
	private static final Map<String, Properties> cacheMap = new WeakHashMap<String, Properties>();

	/**
	 * Gets the properties.
	 *
	 * @param propertyFileName
	 *            the property file name
	 * @return the properties
	 */
	public static Properties getProperties(String propertyFileName) {
		if (cacheMap.containsKey(propertyFileName)) {
			return cacheMap.get(propertyFileName);
		}
		if (propertyFileName == null) {
			return null;
		}
		propertyFileName += ".properties";
		Properties properties = new Properties();
		try {
			properties.load(PropertyFileReader.class.getResourceAsStream(propertyFileName));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		cacheMap.put(propertyFileName, properties);
		return properties;
	}

	/**
	 * Gets the property value.
	 *
	 * @param propertyFileName
	 *            the property file name
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 */
	public static String getPropertyValue(String propertyFileName, String propertyName) {
		Properties properties = getProperties(propertyFileName);
		if (properties == null) {
			return null;
		}
		return properties.getProperty(propertyName);
	}
}
