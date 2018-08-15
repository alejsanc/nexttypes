/*
 * Copyright 2015-2018 Alejandro Sánchez <alex@nexttypes.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nexttypes.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.Menu;
import com.nexttypes.datatypes.MenuSection;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.enums.Format;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.logging.Logger;
import com.nexttypes.settings.Permissions;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;

public class Context {

	public static final String CONTEXT = "com.nexttypes.context";

	protected ServletContext context;
	protected String directory;
	protected Logger logger;
	protected DatabaseConnection.DatabaseConnectionPool connectionPool;
	protected TypesCache typesCache = new TypesCache();
	protected ConcurrentHashMap<String, Properties> resources = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Properties> files = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Properties> resourcesAndFiles[] = new ConcurrentHashMap[] { resources, files };
	protected ConcurrentHashMap<String, LinkedHashMap<String, TypeField>> fields = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, LinkedHashMap<String, TypeIndex>> indexes = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, HTML> templates = new ConcurrentHashMap<>();

	public Context(String directory) {
		this.directory = Utils.readDirectory(directory);
		init();
	}

	public Context(ServletContext context) {
		this.context = context;
		directory = Utils.readDirectory(context.getInitParameter(Constants.SETTINGS_DIRECTORY));
		context.setAttribute(CONTEXT, this);
		init();
	}

	protected void init() {
		logger = new Logger(this);
	}

	public static Context get(ServletContext servletContext) {
		return (Context) servletContext.getAttribute(Context.CONTEXT);
	}

	public static void close(ServletContext servletContext) {
		Context context = get(servletContext);
		context.logger.close();
				
		if (context.connectionPool != null) {
			context.connectionPool.close();
		}
		
		context.context.removeAttribute(CONTEXT);
	}

	public HTML getTemplate(String template, String lang) {
		HTML document = null;

		if (templates.containsKey(template)) {
			document = templates.get(template).clone();
		} else {
			document = new HTML(context.getResourceAsStream(
					"/" + Constants.TEMPLATES + "/" + template + "." + Format.XHTML.getExtension()), lang);
			templates.putIfAbsent(template, document.clone());
		}

		return document;
	}

	public DatabaseConnection.DatabaseConnectionPool getConnectionPool() {
		return connectionPool;
	}

	public void setConnectionPool(DatabaseConnection.DatabaseConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	public Logger getLogger() {
		return logger;
	}

	public ConcurrentHashMap<String, LinkedHashMap<String, TypeField>> getFieldsCache() {
		return fields;
	}

	public ConcurrentHashMap<String, LinkedHashMap<String, TypeIndex>> getIndexesCache() {
		return indexes;
	}

	public TypeSettings getTypeSettings(String[] groups) {
		ArrayList<Properties> properties = new ArrayList<>();

		properties.addAll(0, getProperties(Settings.TYPES_SETTINGS));

		if (groups != null) {
			for (String group : groups) {
				properties.addAll(0, getProperties(Constants.GROUPS + "/" + group + ".properties"));
			}
		}

		return new TypeSettings(properties);
	}

	public Strings getStrings(String lang) {
		return new Strings(getProperties(Constants.LANG + "/" + lang + ".properties"));
	}

	public Permissions getPermissions(String user, String[] groups) {
		return new Permissions(getProperties(Settings.PERMISSIONS_SETTINGS), user, groups);
	}

	public Settings getSettings(String file) {
		return new Settings(getProperties(file));
	}

	protected ArrayList<Properties> getProperties(String file) {
		ArrayList<Properties> propertiesList = new ArrayList<>();

		try {

			for (ConcurrentHashMap<String, Properties> cache : resourcesAndFiles) {

				Properties properties = cache.get(file);

				if (properties != null) {

					propertiesList.add(0, properties);

				} else {

					InputStream stream = null;

					if (cache == resources) {
						stream = getClass().getResourceAsStream(Settings.DEFAULT_SETTINGS + file);
					} else {
						File filePath = new File(directory + file);
						if (filePath.isFile()) {
							stream = new FileInputStream(filePath);
						}
					}

					if (stream != null) {

						try (InputStream s = stream) {
							try (InputStreamReader reader = new InputStreamReader(s, Constants.UTF_8_CHARSET)) {
								properties = new Properties();
								properties.load(reader);
								cache.putIfAbsent(file, properties);
								propertiesList.add(0, properties);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		return propertiesList;
	}

	public Menu getMenu(String file) {
		MenuSection[] sections = null;
		File filePath = new File(directory + file);
		InputStream stream = null;

		try {
			if (filePath.isFile()) {
				stream = new FileInputStream(filePath);
			} else {
				stream = getClass().getResourceAsStream(Settings.DEFAULT_SETTINGS + file);
			}

			try (InputStream s = stream) {
				ObjectMapper mapper = new ObjectMapper();
				sections = mapper.readValue(s, TypeFactory.defaultInstance().constructArrayType(MenuSection.class));

			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		return new Menu(sections);
	}

	public TypesCache getTypesCache() {
		return typesCache;
	}

	public class TypesCache {
		protected ConcurrentHashMap<String, LinkedHashMap<String, TypeField>> fields = new ConcurrentHashMap<>();
		protected ConcurrentHashMap<String, LinkedHashMap<String, TypeIndex>> indexes = new ConcurrentHashMap<>();
		protected ConcurrentHashMap<String, LinkedHashMap<String, String>> contentTypes = new ConcurrentHashMap<>();

		public void clear() {
			fields.clear();
			indexes.clear();
		}

		public LinkedHashMap<String, TypeField> getFields(String type) {
			return fields.get(type);
		}

		public LinkedHashMap<String, TypeIndex> getIndexes(String type) {
			return indexes.get(type);
		}

		public LinkedHashMap<String, String> getContentTypes(String type) {
			return contentTypes.get(type);
		}

		public ConcurrentHashMap<String, LinkedHashMap<String, TypeField>> getFields() {
			return fields;
		}

		public ConcurrentHashMap<String, LinkedHashMap<String, TypeIndex>> getIndexes() {
			return indexes;
		}

		public ConcurrentHashMap<String, LinkedHashMap<String, String>> getContentTypes() {
			return contentTypes;
		}

		public void addFields(String type, LinkedHashMap<String, TypeField> fields) {
			this.fields.putIfAbsent(type, fields);
		}

		public void addIndexes(String type, LinkedHashMap<String, TypeIndex> indexes) {
			this.indexes.putIfAbsent(type, indexes);
		}

		public void addContentTypes(String type, LinkedHashMap<String, String> contentTypes) {
			this.contentTypes.putIfAbsent(type, contentTypes);
		}
	}
}