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

import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.URL;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.nodes.Node;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.LanguageSettings;

public class Console {
	public static final String PROGRAM_NAME = "com.nexttypes.system.Console";
	public static final String EXPORT_TYPES = "export-types";
	public static final String EXPORT_OBJECTS = "export-objects";
	public static final String IMPORT_TYPES = "import-types";
	public static final String IMPORT_OBJECTS = "import-objects";
	public static final String EXISTING_TYPES_ACTION = "existing-types-action";
	public static final String EXISTING_OBJECTS_ACTION = "existing-objects-action";
	public static final String INCLUDE_OBJECTS = "include-objects";
	public static final String SCAN_VIRUS = "scan-virus";

	protected Options options;
	protected Context context;
	protected Settings settings;
	protected LanguageSettings languageSettings;

	public Console(String args[]) {
		try {

			options = new Options();

			OptionGroup actions = new OptionGroup();
			actions.addOption(new Option("b", KeyWords.BACKUP, false, "Backup types and objects."));
			actions.addOption(new Option("it", IMPORT_TYPES, false, "Import types."));
			actions.addOption(new Option("io", IMPORT_OBJECTS, false, "Import objects."));
			
			actions.addOption(Option.builder("et")
					.longOpt(EXPORT_TYPES)
					.hasArgs()
					.valueSeparator(',')
					.optionalArg(true)
					.desc("Export types.")
					.argName(KeyWords.TYPES)
					.get()
			);
			
			actions.addOption(Option.builder("eo")
					.longOpt(EXPORT_OBJECTS)
					.hasArgs()
					.valueSeparator(',')
					.optionalArg(true)
					.desc("Export objects.")
					.argName(KeyWords.OBJECTS)
					.get()
			);
			
			actions.addOption(new Option("sv", SCAN_VIRUS, false, "Scan virus."));
			actions.addOption(new Option("h", KeyWords.HELP, false, "Help."));
			actions.setRequired(true);
			options.addOptionGroup(actions);

			options.addOption(Option.builder("s")
					.longOpt(KeyWords.SETTINGS)
					.hasArg()
					.desc("Settings directory.")
					.argName(KeyWords.SETTINGS)
					.get()
			);
			
			options.addOption("f", KeyWords.FULL, false, "Make a full backup.");
			
			options.addOption(Option.builder("eta")
					.longOpt(EXISTING_TYPES_ACTION)
					.hasArg()
					.desc("Existing types action.")
					.argName(KeyWords.ACTION)
					.get()
			);
			
			options.addOption(Option.builder("eoa")
					.longOpt(EXISTING_OBJECTS_ACTION)
					.hasArg()
					.desc("Existing objects action.")
					.argName(KeyWords.ACTION)
					.get()
			);
			
			options.addOption(Option.builder("t")
					.longOpt(KeyWords.TYPE)
					.hasArg()
					.desc("Type name.")
					.argName(KeyWords.TYPE)
					.get()
			);
			
			options.addOption(Option.builder("o")
					.longOpt(KeyWords.OBJECTS)
					.hasArgs()
					.valueSeparator(',')
					.desc("Objects Id.")
					.argName(KeyWords.OBJECTS)
					.get()
			);
			
			options.addOption("ino", INCLUDE_OBJECTS, false, "Include objects.");
			
			options.addOption(Option.builder("or")
					.longOpt(KeyWords.ORDER)
					.hasArg()
					.desc("Query order.")
					.argName(KeyWords.ORDER)
					.get()
			);
			
			options.addOption(Option.builder("l")
					.longOpt(KeyWords.LANG)
					.hasArg()
					.desc("Language.")
					.argName(KeyWords.LANG)
					.get()
			);
			
			String method = null;

			CommandLineParser parser = new DefaultParser();
			CommandLine command = parser.parse(options, args);

			if (command.hasOption(KeyWords.HELP)) {
				printHelp();
				return;
			} else if (command.hasOption(KeyWords.BACKUP)) {
				method = KeyWords.BACKUP;
			} else if (command.hasOption(IMPORT_TYPES)) {
				method = IMPORT_TYPES;
			} else if (command.hasOption(EXPORT_TYPES)) {
				method = EXPORT_TYPES;
			} else if (command.hasOption(IMPORT_OBJECTS)) {
				method = IMPORT_OBJECTS;
			} else if (command.hasOption(EXPORT_OBJECTS)) {
				method = EXPORT_OBJECTS;
			} else if (command.hasOption(SCAN_VIRUS)) {
				method = SCAN_VIRUS;
			} else {
				printHelp();
				return;
			}

			context = new Context(command.getOptionValue(KeyWords.SETTINGS));
			settings = context.getSettings(Settings.CONSOLE_SETTINGS);
			String lang = command.hasOption(KeyWords.LANG) ? command.getOptionValue(KeyWords.LANG)
					: settings.getString(Settings.DEFAULT_LANG);
			languageSettings = context.getLanguageSettings(lang);
			NodeMode mode = null;

			switch (method) {
			case SCAN_VIRUS:
				mode = NodeMode.READ;
				break;
				
			case KeyWords.BACKUP:
			case EXPORT_TYPES:
			case IMPORT_OBJECTS:
			case EXPORT_OBJECTS:
				mode = NodeMode.WRITE;
				break;

			case IMPORT_TYPES:
				mode = NodeMode.ADMIN;
				break;
			}

			try (Node nextNode = Loader.loadNode(settings.getString(Settings.NEXT_NODE),
					new Auth(Auth.CONSOLE), mode, lang, URL.LOCALHOST, context, false)) {

				Object result = null;

				ImportAction existingObjectsAction = null;
				String type = null;
				String[] objects = null;

				switch (method) {
				case SCAN_VIRUS:
					type = command.getOptionValue(KeyWords.TYPE);
					objects = command.getOptionValues(KeyWords.OBJECTS);
					
					nextNode.scanVirus(type, objects);
					break;
					
				case KeyWords.BACKUP:
					boolean full = command.hasOption(KeyWords.FULL);

					try (TypesStream types = nextNode.backup(full)) {
						writeResult(types);
					}

					break;

				case IMPORT_TYPES:
					ImportAction existingTypesAction = ImportAction
							.valueOf(command.getOptionValue(EXISTING_TYPES_ACTION).toUpperCase());
					existingObjectsAction = ImportAction
							.valueOf(command.getOptionValue(EXISTING_OBJECTS_ACTION).toUpperCase());

					result = nextNode.importTypes(System.in, existingTypesAction, existingObjectsAction);

					break;

				case EXPORT_TYPES:
					String[] types = command.getOptionValues(EXPORT_TYPES);
					boolean includeObjects = command.hasOption(INCLUDE_OBJECTS);

					try (TypesStream typesExport = nextNode.exportTypes(types, includeObjects)) {
						writeResult(typesExport);
					}

					break;

				case IMPORT_OBJECTS:
					existingObjectsAction = ImportAction
							.valueOf(command.getOptionValue(EXISTING_OBJECTS_ACTION).toUpperCase());
					result = nextNode.importObjects(System.in, existingObjectsAction);
					break;

				case EXPORT_OBJECTS:
					type = command.getOptionValue(KeyWords.TYPE);
					objects = command.getOptionValues(EXPORT_OBJECTS);
					LinkedHashMap<String, Order> order = Utils
							.parserOrderString(command.getOptionValue(KeyWords.ORDER));

					try (ObjectsStream objectsExport = nextNode.exportObjects(type, objects, order)) {
						writeResult(objectsExport);
					}

					break;
				}

				if (result != null) {
					writeResult(result);
				}

				nextNode.commit();
			}
		} catch (NXException e) {
			System.out.println(e.getMessage(languageSettings));
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelp();
		}
	}

	protected void writeResult(Object object) {
		new Serial(object, Format.JSON).write(System.out);
	}

	protected void printHelp() {

		try {
			HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
			formatter.printHelp(PROGRAM_NAME, null, options, null, true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args) {
		new Console(args);
	}
}