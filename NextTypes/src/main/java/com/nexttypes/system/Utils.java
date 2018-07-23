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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Formatter;
import java.util.LinkedHashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Node;

import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;

public class Utils {

	public static InputStream toInputStream(String input) {
		InputStream output = null;

		if (input != null && input.length() > 0) {
			try {
				output = org.apache.commons.io.IOUtils.toInputStream(input, Constants.UTF_8_CHARSET);
			} catch (IOException e) {
				throw new NXException(e);
			}
		}

		return output;
	}

	public static String toString(InputStream input) {
		String output = null;

		if (input != null) {
			try {
				output = org.apache.commons.io.IOUtils.toString(input, Constants.UTF_8_CHARSET);
			} catch (IOException e) {
				throw new NXException(e);
			}
		}

		return output;
	}
	
	public static String toString(Node input) {
		try {
			input = input.cloneNode(true);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(input);
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Exception e) {
			throw new NXException(e);
		}
	}

	public static String etag(ZonedDateTime date) {
		String etag = null;

		if (date != null) {
			etag = DigestUtils.md5Hex(date.toString());
		}

		return etag;
	}

	public static String base64decode(String input) {
		String output = null;

		if (input != null && input.length() > 0) {
			try {
				output = new String(Base64.getDecoder().decode(input), Constants.UTF_8_CHARSET);
			} catch (UnsupportedEncodingException e) {
				throw new NXException(e);
			}
		}

		return output;
	}

	public static String hexEncode(byte[] input) {
		String output = null;

		if (input != null && input.length > 0) {
			output = "\\\\x" + Hex.encodeHexString(input);
		}

		return output;
	}

	public static byte[] hexDecode(String input) {
		byte[] output = null;

		if (input != null && input.length() > 0) {
			try {
				output = Hex.decodeHex(input.substring(4, input.length() - 1).toCharArray());
			} catch (DecoderException e) {
				throw new NXException(e);
			}
		}

		return output;
	}

	public static String[] split(String input, String regex) {
		String[] output = null;

		if (input != null && input.length() > 0) {
			output = input.split(regex);

			for (int x = 0; x < output.length; x++) {
				output[x] = output[x].trim();
			}
		}

		return output;
	}
	
	public static String trim(String input) {
		String output = null;
		
		if (input != null && input.length() > 0) {
			String value = input.trim();
			
			if (value.length() > 0) {
				output = value;
			}
		}
		
		return output;
	}
	
	public static String[] trim(String[] input) {
		String[] output = null;
		
		if (input != null && input.length > 0) {
			ArrayList<String> values = new ArrayList<>();
			
			for (String value : input) {
				value = trim(value);
				
				if (value != null) {
					values.add(value);
				}
			}
			
			if (values.size() > 0) {
				output = values.toArray(new String[] {});
			}
		}
		
		return output;
	}
	
	public static String format(String text, Object... parameters) {
		if (text != null && text.length() > 0) {
			try (Formatter formatter = new Formatter()) {
				text = formatter.format(text, parameters).toString();
			}
		}

		return text;
	}

	public static LinkedHashMap<String, Order> parserOrderString(String order) {
		LinkedHashMap<String, Order> fieldOrders = null;

		if (order != null && order.length() > 0) {
			String[] orderValues = order.split(",");
			fieldOrders = new LinkedHashMap<>();

			for (int x = 0; x < orderValues.length; x++) {
				String[] fieldOrder = orderValues[x].split(":");
				String fieldValue = fieldOrder[0];
				Order orderValue = null;

				if (fieldOrder.length > 1) {
					orderValue = Order.valueOf(fieldOrder[1].toUpperCase());
				}

				fieldOrders.put(fieldValue, orderValue);
			}
		}

		return fieldOrders;
	}

	public static String readDirectory(String directory) {
		if (directory != null && !directory.endsWith(File.separator)) {
			directory = directory + File.separator;
		}
		return directory;
	}
	
	public static String getExceptionMessage(Throwable e) {
		String message = e.getMessage();
		
		if (message == null) {
			message = e.getClass().getName();
		} else {
			message = e.getClass().getName() + ": " + message;
		}
		
		return message;
	}
}
