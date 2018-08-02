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

import java.io.IOException;

import org.apache.poi.util.HexDump;
import org.bouncycastle.util.Arrays;

import com.nexttypes.exceptions.NXException;

public class Debug {
	public static final String HTTP_REQUEST = "HTTP Request";
	public static final String HTTP_RESPONSE = "HTTP Response";
	public static final String BODY = "Body";
	public static final String PARAMETERS = "Parameters";
	public static final String HEADERS = "Headers";
	public static final String EXCEPTION = "Exception";
	
	public static void title(String title) {
		if (title != null) {
			System.out.println("\n--------------- " + title + "---------------");
		}
	}
	
	public static void subtitle(String subtitle) {
		if (subtitle != null) {
			System.out.println("\n---------- " + subtitle + " ----------");
		}
	}
	
	public static void text(String text) {
		if (text != null) {
			System.out.println(text);
		}
	}
	
	public static void binary(byte[] data, int limit) {
		if (data != null) {
			boolean truncated = false;
			
			if (data.length > limit) {
				data = Arrays.copyOf(data, limit);
				truncated = true;
			}
			
			try {
				HexDump.dump(data, 0, System.out, 0);
			} catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | IOException e) {
				throw new NXException(e);
			}
			
			if (truncated) {
				System.out.println("---- More ----");
			}
		}
	}
	
	public static void exception(Exception e) {
		exception(e.getMessage(), e);
	}
	
	public static void exception(String message, Exception e) {
		title(EXCEPTION);
		text(message);
		e.printStackTrace();
	}
	
	public static void httpRequest() {
		title(HTTP_REQUEST);
	}
	
	public static void httpResponse() {
		title(HTTP_RESPONSE);
	}
	
	public static void body() {
		subtitle(BODY);
	}
	
	public static void parameters() {
		subtitle(PARAMETERS);
	}
	
	public static void headers() {
		subtitle(HEADERS);
	}
}
