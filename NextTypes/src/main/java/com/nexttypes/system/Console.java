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

import java.util.LinkedHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Serial;
import com.nexttypes.datatypes.URI;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.ImportAction;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.interfaces.Node;
import com.nexttypes.interfaces.ObjectsStream;
import com.nexttypes.interfaces.TypesStream;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;

public class Console {
	public static final String PROGRAM_NAME = "com.nexttypes.system.Console";
	public static final String EXPORT_TYPES = "export-types";
	public static final String EXPORT_OBJECTS = "export-objects";
	public static final String IMPORT_TYPES = "import-types";
	public static final String IMPORT_OBJECTS = "import-objects";
	public static final String EXISTING_TYPES_ACTION = "existing-types-action";
	public static final String EXISTING_OBJECTS_ACTION = "existing-objects-action";
	public static final String INCLUDE_OBJECTS = "include-objects";

	protected Options options;
	protected Context context;
	protected Settings settings;
	protected Strings strings;

	public Console(String args[]) {
		try {

			options = new Options();

			OptionGroup methods = new OptionGroup();
			methods.addOption(new Option("b", Constants.BACKUP, false, "Backup types and objects."));
			methods.addOption(new Option("it", IMPORT_TYPES, false, "Import types."));
			methods.addOption(new Option("io", IMPORT_OBJECTS, false, "Import objects."));
			methods.addOption(Option.builder("et").longOpt(EXPORT_TYPES).desc("Export types.").optionalArg(true)
					.hasArgs().valueSeparator(',').argName(Constants.TYPES).build());
			methods.addOption(Option.builder("eo").longOpt(EXPORT_OBJECTS).desc("Export objects.").optionalArg(true)
					.hasArgs().valueSeparator(',').argName(Constants.OBJECTS).build());
			methods.addOption(new Option("h", Constants.HELP, false, "Help."));
			methods.setRequired(true);
			options.addOptionGroup(methods);

			options.addOption(Option.builder("s").longOpt(Constants.SETTINGS).hasArg().desc("Settings directory.")
					.argName(Constants.SETTINGS).build());
			options.addOption("f", Constants.FULL, false, "Make a full backup.");
			options.addOption(Option.builder("eta").longOpt(EXISTING_TYPES_ACTION).hasArg()
					.desc("Existing types action.").argName(Constants.ACTION).build());
			options.addOption(Option.builder("eoa").longOpt(EXISTING_OBJECTS_ACTION).hasArg()
					.desc("Existing objects action.").argName(Constants.ACTION).build());
			options.addOption(Option.builder("t").longOpt(Constants.TYPE).hasArg().desc("Type name.")
					.argName(Constants.TYPE).build());
			options.addOption("ino", INCLUDE_OBJECTS, false, "Include objects.");
			options.addOption(Option.builder("o").longOpt(Constants.ORDER).hasArg().desc("Query order.")
					.argName(Constants.ORDER).build());
			options.addOption(Option.builder("l").longOpt(Constants.LANG).hasArg().desc("Language.")
					.argName(Constants.LANG).build());
			String method = null;

			CommandLineParser parser = new DefaultParser();
			CommandLine command = parser.parse(options, args);

			if (command.hasOption(Constants.HELP)) {
				printHelp();
				return;
			} else if (command.hasOption(Constants.BACKUP)) {
				method = Constants.BACKUP;
			} else if (command.hasOption(IMPORT_TYPES)) {
				method = IMPORT_TYPES;
			} else if (command.hasOption(EXPORT_TYPES)) {
				method = EXPORT_TYPES;
			} else if (command.hasOption(IMPORT_OBJECTS)) {
				method = IMPORT_OBJECTS;
			} else if (command.hasOption(EXPORT_OBJECTS)) {
				method = EXPORT_OBJECTS;
			} else {
				printHelp();
				return;
			}

			context = new Context(command.getOptionValue(Constants.SETTINGS));
			settings = context.getSettings(Settings.CONSOLE_SETTINGS);
			String lang = command.hasOption(Constants.LANG) ? command.getOptionValue(Constants.LANG)
					: settings.getString(Constants.DEFAULT_LANG);
			strings = context.getStrings(lang);
			NodeMode mode = null;

			switch (method) {
			case Constants.BACKUP:
			case EXPORT_TYPES:
			case IMPORT_OBJECTS:
			case EXPORT_OBJECTS:
				mode = NodeMode.WRITE;
				break;

			case IMPORT_TYPES:
				mode = NodeMode.ADMIN;
				break;
			}

			try (Node nextNode = Loader.loadNode(settings.getString(Constants.NEXT_NODE), Auth.CONSOLE, null, mode,
					lang, URI.LOCALHOST, context, false)) {

				Object result = null;

				ImportAction existingObjectsAction = null;

				switch (method) {
				case Constants.BACKUP:
					boolean full = command.hasOption(Constants.FULL);

					try (TypesStream types = nextNode.backup(lang, full)) {
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

					try (TypesStream typesExport = nextNode.exportTypes(types, lang, includeObjects)) {
						writeResult(typesExport);
					}

					break;

				case IMPORT_OBJECTS:
					existingObjectsAction = ImportAction
							.valueOf(command.getOptionValue(EXISTING_OBJECTS_ACTION).toUpperCase());
					result = nextNode.importObjects(System.in, existingObjectsAction);
					break;

				case EXPORT_OBJECTS:
					String[] objects = command.getOptionValues(EXPORT_OBJECTS);
					String type = command.getOptionValue(Constants.TYPE);
					LinkedHashMap<String, Order> order = Utils
							.parserOrderString(command.getOptionValue(Constants.ORDER));

					try (ObjectsStream objectsExport = nextNode.exportObjects(type, objects, lang, order)) {
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
			System.out.println(e.getMessage(strings));
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelp();
		}
	}

	protected void writeResult(Object object) {
		new Serial(object, Format.JSON).write(System.out);
	}

	protected void printHelp() {

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(PROGRAM_NAME, options, true);

	}

	public static void main(String[] args) {
		new Console(args);
	}
}