/*
 * Copyright 2015-2026 Alejandro Sánchez <alex@nexttypes.com>
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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.Menu;
import com.nexttypes.datatypes.MenuSection;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.datatypes.TypeIndex;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.logging.Logger;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Permissions;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.LanguageSettings;
import com.nexttypes.settings.TypeSettings;

public class Context {

	public static final String CONTEXT = "com.nexttypes.context";

	protected Settings settings;
	protected ServletContext context;
	protected String directory;
	protected Logger logger;
	protected ConcurrentHashMap<String, DBConnection.DBConnectionPool> connectionPools
		= new ConcurrentHashMap<>();
	protected TypesCache typesCache = new TypesCache();
	protected ConcurrentHashMap<String, Properties> resources = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Properties> files = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Properties> resourcesAndFiles[]
		= new ConcurrentHashMap[] { resources, files };
	protected ConcurrentHashMap<String, HTML> templates = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, byte[]> defaults = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Menu> menus = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, String> styles = new ConcurrentHashMap<>();
	protected ConcurrentHashMap<String, Image> images = new ConcurrentHashMap<>();
	
	public Context(String directory) {
		this.directory = Utils.readDirectory(directory);
		init();
	}

	public Context(ServletContext context) {
		this.context = context;
		directory = Utils.readDirectory(context.getInitParameter(Settings.SETTINGS_DIRECTORY));
		context.setAttribute(CONTEXT, this);
		init();

		for (String className : settings.getStringArray(KeyWords.NODES)) {
			Loader.initNode(className, this);
		}
	}

	protected void init() {
		settings = getSettings(Settings.CONTEXT_SETTINGS);
		logger = new Logger(this);
	}

	public static Context get(ServletContext servletContext) {
		return (Context) servletContext.getAttribute(Context.CONTEXT);
	}

	public static void close(ServletContext servletContext) {
		Context context = get(servletContext);

		context.logger.close();

		for (Map.Entry<String, DBConnection.DBConnectionPool> entry
				: context.connectionPools.entrySet()) {
			
			entry.getValue().close();
		}

		context.context.removeAttribute(CONTEXT);
	}

	public DBConnection.DBConnectionPool getDatabaseConnectionPool(String name) {
		return connectionPools.get(name);
	}

	public void putDBConnectionPool(String name,
			DBConnection.DBConnectionPool connectionPool) {
		
		connectionPools.putIfAbsent(name, connectionPool);
	}
	
	public void removeDBConnectionPool(String name) {
		connectionPools.remove(name);		
	}
	
	public Logger getLogger() {
		return logger;
	}

	public TypeSettings getTypeSettings(Auth auth) {
		ArrayList<Properties> properties = new ArrayList<>();

		String [] groups = auth.getGroups();
		
		properties.addAll(0, getProperties(Settings.TYPES_SETTINGS));

		if (groups != null) {
			for (String group : groups) {
				properties.addAll(0, getProperties(KeyWords.GROUPS + "/" + group + ".properties"));
			}
		}

		return new TypeSettings(properties);
	}

	public LanguageSettings getLanguageSettings(String lang) {
		return new LanguageSettings(getProperties(KeyWords.LANG + "/" + lang + ".properties"));
	}
	
	public Permissions getPermissions(Module module) {
		return getPermissions(null, module);
	}
	
	public Permissions getPermissions(String type, Module module) {
		return getPermissions(type, module.getAuth(), module.getTypeSettings(), 
				module.getNextNode());
	}

	public Permissions getPermissions(String type, Auth auth, TypeSettings typeSettings,
			Node nextNode) {
		
		Permissions permissions = null;
		
		ArrayList<Properties> settings = getProperties(Settings.PERMISSIONS_SETTINGS);
		
		String className = typeSettings.gts(type, KeyWords.PERMISSIONS);
			
		if (className != null) {
			permissions = Loader.loadPermissions(className, settings, auth, nextNode);
		} else {
			permissions = new Permissions(settings, auth, nextNode);
		}
		
		return permissions;
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
					} else if (directory != null) {
						File filePath = new File(directory + file);
						if (filePath.isFile()) {
							stream = new FileInputStream(filePath);
						}
					}

					if (stream != null) {

						try (InputStream s = stream) {
							try (InputStreamReader reader = new InputStreamReader(s,
									Constants.UTF_8_CHARSET)) {
								
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

	public InputStream getFile(String file) {
		File filePath = null;
		InputStream stream = null;
				
		if (directory != null) {
			filePath = new File(directory + file);
		}		

		try {
			if (filePath != null && filePath.isFile()) {
				stream = new FileInputStream(filePath);
			} else {
				stream = getClass().getResourceAsStream(Settings.DEFAULT_SETTINGS + file);
			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		return stream;
	}
	
	public HTML getTemplate(String file, String lang) {
		HTML document = templates.get(file);
		HTML documentClone = null;
		
		if (document == null) {
			document = new HTML(getFile(KeyWords.TEMPLATES + "/" + file), lang);
			templates.putIfAbsent(file, document);
			documentClone = document.clone();
		} else {
			documentClone = document.clone();
			documentClone.setLang(lang);
		}

		return documentClone;
	}
	
	public byte[] getDefault(String file) {
		byte[] value = defaults.get(file);
		
		if (value == null) {
			try {
				value = IOUtils.toByteArray(getFile(KeyWords.DEFAULTS + "/" + file));
			} catch (IOException e) {
				throw new NXException(e);
			}
			
			defaults.putIfAbsent(file, value);
		}
		
		return value;
	}
	
	public Menu getMenu(String file) {
		Menu menu = menus.get(file);
		
		if (menu == null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				MenuSection[] sections;
				sections = mapper.readValue(getFile(KeyWords.MENUS + "/" + file),
						TypeFactory.defaultInstance().constructArrayType(MenuSection.class));
				menu = new Menu(sections);
				
				menus.putIfAbsent(file, menu);
			} catch (IOException e) {
				throw new NXException(e);
			}
		}
		
		return menu;
	}
	
	public String getStyle(String file) {
		String style = styles.get(file);
		
		if (style == null) {
			style = Utils.toString(context.getResourceAsStream(file));
			styles.putIfAbsent(file, style);
		}
		
		return style;
	}
	
	public Image getImage(String file) {
		Image image = images.get(file);
		
		if (image == null) {
			try {
				image = new Image(IOUtils.toByteArray(context.getResourceAsStream(file)));
				images.putIfAbsent(file, image);
			} catch (IOException e) {
				throw new NXException(e);
			}
		}
		
		return image;
	}

	public TypesCache getTypesCache() {
		return typesCache;
	}

	public class TypesCache {
		protected ConcurrentHashMap<String, LinkedHashMap<String, TypeField>> fields
			= new ConcurrentHashMap<>();
		protected ConcurrentHashMap<String, LinkedHashMap<String, TypeIndex>> indexes
			= new ConcurrentHashMap<>();
		protected ConcurrentHashMap<String, LinkedHashMap<String, String>> contentTypes
			= new ConcurrentHashMap<>();

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