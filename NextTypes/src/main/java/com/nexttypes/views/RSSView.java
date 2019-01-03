/*
 * Copyright 2015-2019 Alejandro Sánchez <alex@nexttypes.com>
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
import com.nexttypes.datatypes.RSS;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.enums.Format;
import com.nexttypes.enums.Order;
import com.nexttypes.exceptions.NXException;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;

public class RSSView extends View {

	public RSSView(HTTPRequest request) {
		super(request, Settings.RSS_SETTINGS);
	}

	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		String sql = typeSettings.gts(type, Constants.RSS_SELECT);

		if (sql != null) {
			Tuple[] tuples = nextNode.query(sql, lang);
			RSS rss = new RSS("NextTypes " + type, "NextTypes " + type, type, lang,
					request.getURLRoot(), tuples);
			Content content = new Content(rss.toString(), Format.RSS);
			return content;
		} else {
			throw new NXException(type, KeyWords.SELECT_STRING_NOT_FOUND);
		}
	}
}