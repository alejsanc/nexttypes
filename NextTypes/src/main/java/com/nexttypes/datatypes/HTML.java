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

package com.nexttypes.datatypes;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HTML extends XML {
	private static final long serialVersionUID = 1L;
	
	public static final String[] INPUT_ATTRIBUTES = new String[] { HTML.NAME, HTML.FORM,
			HTML.DISABLED, HTML.VALUE };

	public static final String DOCTYPE = "<!DOCTYPE html>";

	public static final String HEAD = "head";
	public static final String TITLE = "title";
	public static final String BODY = "body";
	public static final String HEADER = "header";
	public static final String MAIN = "main";
	public static final String FOOTER = "footer";
	public static final String P = "p";
	public static final String AUTOCOMPLETE = "autocomplete";
	public static final String OFF = "off";
	public static final String STRONG = "strong";
	public static final String TEXT = "text";
	public static final String MAXLENGTH = "maxlength";
	public static final String SIZE = "size";
	public static final String H1 = "h1";
	public static final String H2 = "h2";
	public static final String H3 = "h3";
	public static final String H4 = "h4";
	public static final String H5 = "h5";
	public static final String TABLE = "table";
	public static final String TR = "tr";
	public static final String TH = "th";
	public static final String HIDDEN = "hidden";
	public static final String TD = "td";
	public static final String TBODY = "tbody";
	public static final String THEAD = "thead";
	public static final String MULTIPLE = "multiple";
	public static final String PASSWORD = "password";
	public static final String PRE = "pre";
	public static final String ARTICLE = "article";
	public static final String CHECKBOX = "checkbox";
	public static final String FORM = "form";
	public static final String ACTION = "action";
	public static final String DIV = "div";
	public static final String SELECT = "select";
	public static final String NAV = "nav";
	public static final String SPAN = "span";
	public static final String FILE = "file";
	public static final String URL = "url";
	public static final String EMAIL = "email";
	public static final String DATE = "date";
	public static final String TEL = "tel";
	public static final String TIME = "time";
	public static final String DATETIME_LOCAL = "datetime-local";
	public static final String COLOR = "color";
	public static final String NUMBER = "number";
	public static final String INPUT = "input";
	public static final String TYPE = "type";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String MIN = "min";
	public static final String MAX = "max";
	public static final String STEP = "step";
	public static final String ANY = "any";
	public static final String REQUIRED = "required";
	public static final String TEXTAREA = "textarea";
	public static final String OPTION = "option";
	public static final String SELECTED = "selected";
	public static final String STYLE = "style";
	public static final String A = "a";
	public static final String HREF = "href";
	public static final String ALT = "alt";
	public static final String IMG = "img";
	public static final String SRC = "src";
	public static final String BR = "br";
	public static final String CONTROLS = "controls";
	public static final String AUDIO = "audio";
	public static final String VIDEO = "video";
	public static final String MAILTO = "mailto";
	public static final String BUTTON = "button";
	public static final String DISABLED = "disabled";
	public static final String CHECKED = "checked";
	public static final String META = "meta";
	public static final String VIEWPORT = "viewport";
	public static final String CONTENT = "content";
	public static final String DESCRIPTION = "description";
	public static final String SCRIPT = "script";
	public static final String LINK = "link";
	public static final String REL = "rel";
	public static final String STYLESHEET = "stylesheet";
	public static final String SHORTCUT_ICON = "shortcut icon";
	public static final String UL = "ul";
	public static final String LI = "li";
	public static final String METHOD = "method";
	public static final String GET = "get";
	public static final String POST = "post";
	public static final String SEARCH = "search";
	public static final String SUBMIT = "submit";
	public static final String FIGURE = "figure";
	public static final String FIGCAPTION = "figcaption";
	public static final String ALTERNATE = "alternate";
	public static final String ACCEPT = "accept";
	public static final String BACKGROUND_COLOR = "background-color";
	public static final String RADIO = "radio";
	public static final String READONLY = "readonly";
	public static final String LIST = "list";
	public static final String DATALIST = "datalist";
	public static final String INPUT_GROUP = "input-group";
	public static final String LIST_INPUT = "list-input";
	public static final String NOSCRIPT = "noscript";
	public static final String SRCSET = "srcset";
	public static final String SIZES = "sizes";
	public static final String OBJECT = "object";
	public static final String DATA = "data";

	public HTML() {
		super();
		init();
	}

	protected HTML(Document document, String lang) {
		super(document, lang);
		init();
	}

	public HTML(String html, String lang) {
		super(html, lang);
		init();
		setLang(lang);
		setXMLNS();
	}

	public HTML(InputStream html, String lang) {
		super(html, lang);
		init();
		setLang(lang);
		setXMLNS();
	}

	public HTML clone() {
		return new HTML((Document) document.cloneNode(true), lang);
	}

	protected void init() {
		setDocType(DOCTYPE);
	}

	public void setLang(String lang) {
		getDocumentElement().setAttribute("lang", lang);
		getDocumentElement().setAttribute("xml:lang", lang);
	}

	protected void setXMLNS() {
		getDocumentElement().setAttribute(XMLNS, "http://www.w3.org/1999/xhtml");
	}

	public Element getHead() {
		return getElementByTagName(HEAD);
	}

	public Element getTitle() {
		return getElementByTagName(TITLE);
	}

	public Element getBody() {
		return getElementByTagName(BODY);
	}

	public Element getHeader() {
		return getElementByTagName(HEADER);
	}

	public Element getMain() {
		return getElementByTagName(MAIN);
	}

	public Element getFooter() {
		return getElementByTagName(FOOTER);
	}
	
	public void removeAnchors() {
		NodeList anchors = document.getElementsByTagName(A);
		int anchorsLength = anchors.getLength();
		
		for (int x = 0; x < anchorsLength; x++) {
			Node anchor = anchors.item(0);
			Node text = document.createTextNode(anchor.getTextContent());
			anchor.getParentNode().replaceChild(text, anchor);
		}
	}
	
	public class ListInput extends Element {
		
		protected Element input;
		protected Element dataList;
		
		public ListInput(Element input) {
			super(document.createElement(SPAN));
			addClass(LIST_INPUT);
			this.input = input;
			appendElement(input);
			dataList = appendElement(DATALIST);
			setListId(input.getAttribute(NAME));
		}
		
		@Override
		public Element setAttribute(String name, Object value) {
			if (ArrayUtils.contains(INPUT_ATTRIBUTES, name)) {
				input.setAttribute(name, value);
				if (HTML.NAME.equals(name)) {
					setListId(value);
				}				
			} else {
				super.setAttribute(name, value);
			}
			return this;
		}
		
		@Override
		public Element removeAttribute(String name) {
			if (ArrayUtils.contains(INPUT_ATTRIBUTES, name)) {
				input.removeAttribute(name);
			} else {
				super.removeAttribute(name);
			}
			return this;
		}
		
		protected void setListId(Object name) {
			String listId = name + "-list";
			dataList.setId(listId);
			input.setAttribute(LIST, listId);
		}
	}
	
	public ListInput createListInput(Element input) {
		return new ListInput(input);
	}
	
	public class InputGroup extends Element {
						
		protected ArrayList<Element> inputs = new ArrayList<>();
				
		public InputGroup() {
			super(document.createElement(HTML.SPAN));
			addClass(INPUT_GROUP);
		}
		
		@Override
		public Element setAttribute(String name, Object value) {
			if (ArrayUtils.contains(INPUT_ATTRIBUTES, name)) {
				for (Element input : inputs) {
					input.setAttribute(name, value);				
				}
			} else {
				super.setAttribute(name, value);
			}
			return this;
		}
		
		@Override
		public Element removeAttribute(String name) {
			if (ArrayUtils.contains(INPUT_ATTRIBUTES, name)) {
				for (Element input : inputs) {
					input.removeAttribute(name);
				}
			} else {
				super.removeAttribute(name);
			}
			return this;
		}
		
		public Element appendInput(Element input) {
			inputs.add(input);
			return appendElement(input);
		}
		
		public Element appendInput(String input) {
			return appendInput(createElement(input));
		}
		
		public Element[] getInputs() {
			return inputs.toArray(new Element[] {});
		}
	}
	
	public InputGroup createInputGroup() {
		return new InputGroup();
	}
}
