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

package com.nexttypes.views;

import java.time.Month;
import java.time.Year;
import java.util.LinkedHashMap;

import org.openpdf.pdf.ITextRenderer;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.ViewNotFoundException;
import com.nexttypes.protocol.http.HTTPHeader;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.system.Loader;

public class PDFView extends View {
	
	protected HTMLView htmlView;
	
	public PDFView(HTTPRequest request) {
		this.request = request;
		context = request.getContext();
		typeSettings = request.getTypeSettings();
		String view = Format.HTML.toString();
				
		String className = typeSettings.getView(request.getType(), view);

		if (className != null) {
			request.setView(view);
			htmlView = (HTMLView)Loader.loadView(className, request);
			htmlView.setPrint(true);
			nextNode = htmlView.getNextNode();
		} else {
			throw new ViewNotFoundException(request.getType(), view);
		}
		
		languageSettings = request.getLanguageSettings();

		auth = request.getAuth();
	}
	
	public PDFView(HTMLView view) {
		this(view.getRequest());
	}
	
	@Override
	public Content getTypesInfo(String lang, String view) {
		htmlView.getTypesInfo(lang, view);
		return content();
	}
	
	@Override
	public Content getType(String type, String lang, String view) {
		htmlView.getType(type, lang, view);
		return content();
	}
	
	@Override
	public Content get(String type, String id, String lang, String view, String etag) {
		
		htmlView.get(type, id, lang, view, etag);
		return content();		
	}
	
	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
		
		htmlView.select(type, lang, view, ref, filters, search, order, offset, limit);
		return content();
	}

	@Override
	public Content preview(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
		
		htmlView.preview(type, lang, view, ref, filters, search, order, offset, limit);
		return content();
	}
	
	@Override
	public Content calendar(String type, String lang, String view, FieldReference ref, Year year, Month month) {
		
		htmlView.calendar(type, lang, view, ref, year, month);
		return content();
	}
	
	public Content content() {
		HTML document = htmlView.getDocument();
		document.removeAnchors();
		ITextRenderer renderer = new ITextRenderer();
		byte[] value = renderer.createPDF(document.getDocument());
		Content content = new Content(value, Format.PDF);
		content.setHeader(HTTPHeader.NEXTTYPES_TITLE, document.getTitle().getText());
		return content;
	}
}