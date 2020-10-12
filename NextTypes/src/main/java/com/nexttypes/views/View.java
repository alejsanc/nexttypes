/*
 * Copyright 2015-2020 Alejandro Sánchez <alex@nexttypes.com>
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

import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.ActionReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.NodeMode;
import com.nexttypes.enums.Order;
import com.nexttypes.enums.Component;
import com.nexttypes.exceptions.NotFoundException;
import com.nexttypes.exceptions.NotImplementedException;
import com.nexttypes.exceptions.UnauthorizedException;
import com.nexttypes.nodes.Node;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.protocol.http.HTTPStatus;
import com.nexttypes.settings.Settings;
import com.nexttypes.settings.Strings;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Context;
import com.nexttypes.system.Loader;
import com.nexttypes.system.Module;
import com.nexttypes.system.Constants;

public abstract class View extends Module {

	protected Context context;
	protected HTTPRequest request;
	protected Node nextNode;
	protected Settings settings;
	protected TypeSettings typeSettings;
	protected Strings strings;
	protected Auth auth;

	public View() {

	}

	public View(HTTPRequest request, String settings) {
		this.request = request;
		this.context = request.getContext();
		this.settings = context.getSettings(settings);

		nextNode = Loader.loadNode(this.settings.getString(KeyWords.NEXT_NODE), request, NodeMode.READ);

		FieldReference ref = request.getRef();
		
		if (ref != null) {
			ref.setReferencedType(nextNode.getFieldType(request.getType(), ref.getReferencingField()));
		}
		
		typeSettings = request.getTypeSettings();
		strings = request.getStrings();

		auth = request.getAuth();
	}
	
	public Content getVersion() {
		return new Content(Constants.VERSION, Format.TEXT);
	}

	public Content notFound(String type, String lang, String view, NotFoundException e) {
		return new Content(e.getMessage(request.getStrings()), Format.TEXT, HTTPStatus.NOT_FOUND);
	}

	public Content unauthorized(String type, String lang, String view, UnauthorizedException e) {
		return new Content(e.getMessage(request.getStrings()), Format.TEXT, HTTPStatus.UNAUTHORIZED);
	}

	@Override
	public Node getNextNode() {
		return nextNode;
	}

	@Override
	public void close() {
		if (nextNode != null) {
			nextNode.close();
		}
	}

	@Override
	public Auth getAuth() {
		return auth;
	}

	@Override
	public Context getContext() {
		return request.getContext();
	}

	@Override
	public Strings getStrings() {
		return request.getStrings();
	}

	@Override
	public TypeSettings getTypeSettings() {
		return typeSettings;
	}

	public Content getTypesName(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content getTypesInfo(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content getType(String type, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content get(String type, String id, String lang, String view, String etag) {
		throw new NotImplementedException();
	}
	
	public Content getNames(String type, String lang, String view, ActionReference aref,
			String search, Long offset) {
		throw new NotImplementedException();
	}

	public Content getReferences(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
		throw new NotImplementedException();
	}

	public Content preview(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
		throw new NotImplementedException();
	}

	public Content calendar(String type, String lang, String view, FieldReference ref, Year year, Month month) {
		throw new NotImplementedException();
	}

	public Content createForm(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content alterForm(String type, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content insertForm(String type, String lang, String view, FieldReference ref) {
		throw new NotImplementedException();
	}

	public Content updateForm(String type, String id, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content executeActionForm(String type, String id, String action, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content getField(String type, String id, String field, String etag) {
		throw new NotImplementedException();
	}
	
	public Content getFieldDefault(String type, String field) {
		throw new NotImplementedException();
	}

	public Content getElement(String type, String id, String field, String element, String lang, String view,
			String etag) {
		throw new NotImplementedException();
	}

	public Content updateIdForm(String type, String id, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content importTypesForm(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content importObjectsForm(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content updatePasswordForm(String type, String id, String field, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content loginForm(String lang, String view) {
		throw new NotImplementedException();
	}

	public Content renameForm(String type, String lang, String view) {
		throw new NotImplementedException();
	}

	public Content filterComponent(String type, String field, String lang, String view, int count) {
		throw new NotImplementedException();
	}

	public Content selectComponent(String type, String lang, String view, FieldReference ref,
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset,
			Long limit, Component component) {
		
		throw new NotImplementedException();
	}
}