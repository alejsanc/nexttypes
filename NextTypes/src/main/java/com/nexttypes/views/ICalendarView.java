/*
 * Copyright 2015-2021 Alejandro Sánchez <alex@nexttypes.com>
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

import java.util.LinkedHashMap;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.ICalendar;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;

public class ICalendarView extends View {

	public ICalendarView(HTTPRequest request) {
		super(request, Settings.ICALENDAR_SETTINGS);
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		String sql = typeSettings.gts(type, Constants.ICALENDAR_SELECT);
		Object[] parameters = null;

		if (sql != null) {
			if (ref != null) {
				sql += " where #=?";
				parameters = new Object[] { ref.getReferencingField(), ref.getReferencedId() };
			}

			Tuple[] events = nextNode.query(sql, parameters);
			ICalendar calendar = new ICalendar(request.getURLRoot() + "/" + type + "/", events);
			return new Content(calendar.toString(), Format.ICALENDAR);
		} else {
			throw new NXException(type, KeyWords.SELECT_STRING_NOT_FOUND);
		}

	}
}