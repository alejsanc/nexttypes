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

package com.nexttypes.controllers;

import java.time.ZonedDateTime;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.cups4j.PrintRequestResult;

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.NXObject;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Controller;
import com.nexttypes.system.KeyWords;
import com.nexttypes.exceptions.PrinterException;

public class PrinterJobController extends Controller {
	
	public PrinterJobController(String type, Auth auth, Node nextNode) {
		super(type, auth, nextNode);
	}

	@Override
	public ZonedDateTime insert(NXObject object) {
		
		object.put(KeyWords.USER, auth.getUser());
		
		ZonedDateTime udate = nextNode.insert(object);
		
		String printer = object.getString(KeyWords.PRINTER);
		boolean color = KeyWords.COLOR.equals(object.getString(KeyWords.MODE));
		String pages = object.getString(KeyWords.PAGES);
		short copies = object.getInt16(KeyWords.COPIES);
		String name = object.getString(KeyWords.NAME);
		byte[] document = object.getBinary(KeyWords.DOCUMENT);
		
		PrintRequestResult printRequestResult = null;
		
		try {
			CupsClient cupsClient = new CupsClient();
			CupsPrinter cupsPrinter = cupsClient.getPrinter(printer);
			PrintJob printJob = new PrintJob.Builder(document)
					.color(color)
					.pageRanges(pages)
					.copies(copies)
					.jobName(name)
					.build();
			printRequestResult = cupsPrinter.print(printJob);
			
		} catch (Exception e) {
			throw new PrinterException(printer, e);
		}
		
		if (!printRequestResult.isSuccessfulResult()) {
			throw new PrinterException(printer, printRequestResult);
		}
		
		return udate;
	}
}