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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.nexttypes.exceptions.DisallowedAttributeException;
import com.nexttypes.exceptions.DisallowedTagException;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.exceptions.XMLException;
import com.nexttypes.system.Utils;

public class XML extends PGobject {
	private static final long serialVersionUID = 1L;
	public static final String ID = "id";
	public static final String CLASS = "class";
	public static final String DEFAULT_LANG = "en";
	public static final String XMLNS = "xmlns";

	protected Document document;
	protected String docType;
	protected String lang;
	protected LinkedHashMap<String, String[]> allowedTags;
	protected boolean xmlDeclaration = false;
	
	public XML() {
		type = PT.XML;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new NXException(e);
		}
	}

	protected XML(Document document, String lang) {
		this(document, lang, null);
	}

	protected XML(Document document, String lang, LinkedHashMap<String, String[]> allowedTags) {
		type = PT.XML;
		this.document = document;
		this.lang = lang;
		this.allowedTags = allowedTags;
	}

	public XML(String xml, String lang) {
		this(Utils.toInputStream(xml), lang, (LinkedHashMap<String, String[]>) null);
	}

	public XML(String xml, String lang, String allowedTags) {
		this(Utils.toInputStream(xml), lang, parseAllowedTags(allowedTags));
	}
	
	public XML(byte[] xml, String lang) {
		this(new ByteArrayInputStream(xml), lang, (LinkedHashMap<String, String[]>) null);
	}
	
	public XML(byte[] xml, String lang, String allowedTags) {
		this(new ByteArrayInputStream(xml), lang, parseAllowedTags(allowedTags));
	}

	public XML(InputStream xml, String lang) {
		this(xml, lang, (LinkedHashMap<String, String[]>) null);
	}

	public XML(InputStream xml, String lang, String allowedTags) {
		this(xml, lang, parseAllowedTags(allowedTags));
	}

	public XML(String xml, String lang, LinkedHashMap<String, String[]> allowedTags) {
		this(Utils.toInputStream(xml), lang, allowedTags);
	}
	
	public XML(byte[] xml, String lang, LinkedHashMap<String, String[]> allowedTags) {
		this(new ByteArrayInputStream(xml), lang, allowedTags);
	}

	public XML(InputStream xml, String lang, LinkedHashMap<String, String[]> allowedTags) {
		type = PT.XML;
		this.lang = lang;
		this.allowedTags = allowedTags;

		if (lang.equals(DEFAULT_LANG)) {
			lang = "";
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setAttribute("http://apache.org/xml/properties/locale", new Locale(lang));
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {

				@Override
				public void warning(SAXParseException e) throws SAXException {
					throwException(e);
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
					throwException(e);
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException {
					throwException(e);
				}

			});

			document = builder.parse(xml);

		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new NXException(e);
		}

		checkElement(document.getDocumentElement());

	}

	protected void throwException(SAXParseException e) {
		throw new XMLException(e);
	}

	public static String getText(String xml) {
		StringBuilder text = new StringBuilder();
		StringReader reader = new StringReader(xml);
		int c;
		boolean isText = true;

		try {
			while ((c = reader.read()) != -1) {
				if (c == '<') {
					isText = false;
				} else if (c == '>') {
					isText = true;
				} else if (isText) {
					text.append((char) c);
				}
			}
		} catch (IOException e) {
			throw new NXException(e);
		}

		return text.toString();
	}

	public void setXMLDeclaration(boolean xmlDeclaration) {
		this.xmlDeclaration = xmlDeclaration;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	protected void checkElement(org.w3c.dom.Element element) {
		if (allowedTags != null) {
			String tag = element.getTagName();

			if (!allowedTags.containsKey(tag)) {
				throw new DisallowedTagException(tag);
			} else {
				String[] attributes = allowedTags.get(tag);
				NamedNodeMap childAttributes = element.getAttributes();
				for (int y = 0; y < childAttributes.getLength(); y++) {
					checkAttribute(tag, childAttributes.item(y).getNodeName(), attributes);
				}
			}
		}

		if (element.hasAttribute(ID)) {
			element.setIdAttribute(ID, true);
		}

		NodeList nodes = element.getChildNodes();
		for (int x = 0; x < nodes.getLength(); x++) {
			Node node = nodes.item(x);
			if (node instanceof org.w3c.dom.Element) {
				checkElement((org.w3c.dom.Element) node);
			}
		}
	}

	protected void checkAttribute(String tag, String attribute) {
		if (allowedTags != null) {
			String[] attributes = allowedTags.get(tag);
			if (attributes != null) {
				checkAttribute(tag, attribute, attributes);
			}
		}
	}

	protected void checkAttribute(String tag, String attribute, String[] attributes) {
		if (!ArrayUtils.contains(attributes, attribute)) {
			throw new DisallowedAttributeException(tag, attribute);
		}
	}

	protected static LinkedHashMap<String, String[]> parseAllowedTags(String allowedTags) {
		LinkedHashMap<String, String[]> parsedAllowedTags = null;

		if (allowedTags != null) {
			parsedAllowedTags = new LinkedHashMap<>();

			for (String tag : allowedTags.split(";")) {
				String[] nameAttributes = tag.split(":");
				String name = nameAttributes[0];

				String[] attributes = null;
				if (nameAttributes.length > 1) {
					attributes = nameAttributes[1].split(",");
				}
				parsedAllowedTags.put(name, attributes);
			}

		}

		return parsedAllowedTags;
	}

	public Element createElement(String tag) {
		org.w3c.dom.Element element = document.createElement(tag);
		checkElement(element);
		return new Element(element);
	}

	public Element setDocumentElement(String tag) {
		Element element = createElement(tag);
		document.appendChild(element.getElement());
		return element;
	}

	public Document getDocument() {
		return document;
	}
	
	public Element getDocumentElement() {
		org.w3c.dom.Element element = document.getDocumentElement();
		return element != null ? new Element(element) : null;
	}

	public Element getElementById(String id) {
		org.w3c.dom.Element element = document.getElementById(id);
		return element != null ? new Element(element) : null;
	}

	public Element getElementByTagName(String tag) {
		NodeList nodes = document.getElementsByTagName(tag);
		return nodes != null && nodes.getLength() > 0 ? new Element((org.w3c.dom.Element) nodes.item(0)) : null;
	}

	public Element[] getElementsByClassName(String className) {
		ArrayList<Element> elements = new ArrayList<>();

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			XPathExpression expression = path.compile("//*[contains(concat(' ', @class, ' '), ' " + className + " ')]");
			NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			for (int x = 0; x < nodes.getLength(); x++) {
				elements.add(new Element((org.w3c.dom.Element) nodes.item(x)));
			}
		} catch (XPathExpressionException e) {
			throw new NXException(e);
		}

		return elements.toArray(new Element[] {});
	}

	public XML clone() {
		return new XML((Document) document.cloneNode(true), lang, allowedTags);
	}

	protected String nodeToString(org.w3c.dom.Node node) {
		StringBuilder value = new StringBuilder();

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();

			if (!xmlDeclaration) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

			if (docType != null) {
				value.append(docType + "\n");
			}
			
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);
			value.append(result.getWriter().toString());
		} catch (TransformerException e) {
			throw new NXException(e);
		}
		
		return value.toString();
	}

	public class Element {
		protected org.w3c.dom.Element element;
		protected ArrayList<String> classes = new ArrayList<>();

		protected Element(org.w3c.dom.Element element) {
			this.element = element;
		}

		protected org.w3c.dom.Element getElement() {
			return element;
		}

		protected void setElement(org.w3c.dom.Element element) {
			this.element = element;
		}

		public String getTagName() {
			return element.getTagName();
		}

		public String getNamespace() {
			return element.getNamespaceURI();
		}

		public String getId() {
			return element.getAttribute(ID);
		}

		public Element setId(String id) {
			element.setAttribute(ID, id);
			element.setIdAttribute(ID, true);
			return this;
		}

		public Element appendElement(Element element) {
			element = importElement(element);
			this.element.appendChild(element.getElement());
			return element;
		}

		public Element[] appendElements(Element[] elements) {
			for (Element element : elements) {
				appendElement(element);
			}
			return elements;
		}

		public Element prependElement(Element element) {
			element = importElement(element);
			this.element.insertBefore(element.getElement(), this.element.getFirstChild());
			return element;
		}

		public Element importElement(Element element) {
			org.w3c.dom.Node node = element.getElement();
			Document nodeDocument = node.getOwnerDocument();
			if (nodeDocument != document) {
				node = document.importNode(node, true);
				checkElement((org.w3c.dom.Element) node);
				element.setElement((org.w3c.dom.Element) node);
			}
			return element;
		}

		public Element appendElement(String tag) {
			Element element = createElement(tag);
			this.element.appendChild(element.getElement());
			return element;
		}
		
		public Element appendText(Object text) {
			if (text != null) {
				element.appendChild(document.createTextNode(text.toString()));
			}
			return this;
		}
		
		public String getText() {
			return element.getTextContent();
		}

		public Element prependText(Object text) {
			if (text != null) {
				element.insertBefore(document.createTextNode(text.toString()), element.getFirstChild());
			}
			return this;
		}
		
		public Element appendFragment(XMLFragment fragment) {
			NodeList nodes = fragment.getDocumentElement().getElement().getChildNodes();
			for (int x = 0; x < nodes.getLength(); x++) {
				Node node = document.importNode(nodes.item(x), true);

				if (node instanceof org.w3c.dom.Element) {
					checkElement((org.w3c.dom.Element) node);
				}

				element.appendChild(node);
			}
			return this;
		}

		public Element prependFragment(XMLFragment fragment) {
			Node firstNode = element.getFirstChild();

			NodeList nodes = fragment.getDocumentElement().getElement().getChildNodes();
			for (int x = 0; x < nodes.getLength(); x++) {
				Node node = document.importNode(nodes.item(x), true);

				if (node instanceof org.w3c.dom.Element) {
					checkElement((org.w3c.dom.Element) node);
				}

				element.insertBefore(node, firstNode);
			}
			return this;
		}

		public Element appendFragment(String fragment) {
			return appendFragment(new XMLFragment(fragment, lang, allowedTags));
		}

		public Element prependFragment(String fragment) {
			return prependFragment(new XMLFragment(fragment, lang, allowedTags));
		}

		public Element replace(Element element) {
			element = importElement(element);
			this.element.getParentNode().replaceChild(element.getElement(), this.element);
			this.element = element.getElement();
			return this;
		}

		public Element setAttribute(String name, Object value) {
			checkAttribute(element.getTagName(), name);
			element.setAttribute(name, value.toString());
			if (name.equals(ID)) {
				element.setIdAttribute(ID, true);
			}
			return this;
		}

		public Element setAttribute(String name) {
			return setAttribute(name, name);
		}

		public String getAttribute(String name) {
			return element.getAttribute(name);
		}

		public Element removeAttribute(String name) {
			element.removeAttribute(name);
			return this;
		}
		
		public Element addClass(String className) {
			if (!classes.contains(className)) {
				classes.add(className);
				updateClassAttribute();
			}
			return this;
		}
		
		public Element addClasses(String... classes) {
			boolean updateClassAttribute = false;
			
			for (String className : classes) {
				if (!this.classes.contains(className)) {
					this.classes.add(className);
					updateClassAttribute = true;
				}
			}
			
			if (updateClassAttribute) {
				updateClassAttribute();
			}
			
			return this;
		}
		
		protected void updateClassAttribute() {
			if (classes.size() == 0) {
				removeAttribute(CLASS);
			} else {
				setAttribute(CLASS, StringUtils.join(classes, " "));
			}
		}

		public Element setClasses(String... classes) {
			this.classes.clear();
			this.classes.addAll(Arrays.asList(classes));
			updateClassAttribute();
			return this;
		}

		public Element removeClass(String className) {
			if (classes.contains(className)) {
				classes.remove(className);
				updateClassAttribute();
			}
			return this;
		}
		
		public Element removeClasses(String... classes) {
			boolean updateClassAttribute = false;
			
			for (String className : classes) {
				if (this.classes.contains(className)) {
					this.classes.remove(className);
					updateClassAttribute = true;
				}
			}
			
			if (updateClassAttribute) {
				updateClassAttribute();
			}
			
			return this;
		}
		
		public Element removeClasses() {
			classes.clear();
			removeAttribute(CLASS);
			return this;
		}

		public Element[] getChildElements() {
			ArrayList<Element> elements = new ArrayList<>();
			NodeList nodes = element.getChildNodes();
			for (int x = 0; x < nodes.getLength(); x++) {
				Node node = nodes.item(x);
				if (node instanceof org.w3c.dom.Element) {
					elements.add(new Element((org.w3c.dom.Element) node));
				}
			}
			return elements.toArray(new Element[] {});
		}

		public Element getFirstChildElement() {
			Element firstChild = null;
			Node node = element.getFirstChild();
			if (node != null) {
				firstChild = new Element((org.w3c.dom.Element) node);
			}
			return firstChild;
		}
		
		public Element getElementByTagName(String tag) {
			NodeList nodes = element.getElementsByTagName(tag);
			return nodes != null && nodes.getLength() > 0 ? 
					new Element((org.w3c.dom.Element) nodes.item(0)) : null;
		}
		
		public Element getElementByClassName(String className) {
			try {
				XPathFactory factory = XPathFactory.newInstance();
				XPath path = factory.newXPath();
				XPathExpression expression = path.compile("//*[contains(concat(' ', @class, ' '), ' " + className + " ')]");
				NodeList nodes = (NodeList) expression.evaluate(element, XPathConstants.NODESET);
				return nodes != null && nodes.getLength() > 0 ? new Element((org.w3c.dom.Element) nodes.item(0)) : null;
			} catch (XPathExpressionException e) {
				throw new NXException(e);
			}
		}

		public Element clone() {
			return new Element((org.w3c.dom.Element) element.cloneNode(true));
		}

		public void remove() {
			element.getParentNode().removeChild(element);
		}

		public Element getParent() {
			return new Element((org.w3c.dom.Element) element.getParentNode());
		}

		@Override
		public String toString() {
			return nodeToString(element);
		}
	}

	@Override
	public String getValue() {
		return toString();
	}

	@Override
	public String toString() {
		return nodeToString(document);
	}
}
