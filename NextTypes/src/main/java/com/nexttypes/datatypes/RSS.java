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

package com.nexttypes.datatypes;

import com.nexttypes.system.Constants;

public class RSS extends XML {
	private static final long serialVersionUID = 1L;

	public static final String PUBDATE = "pubDate";

	public RSS(String title, String description, String type, String lang, String urlRoot, Tuple[] tuples) {
		Element rss = setDocumentElement(Constants.RSS).setAttribute(Constants.VERSION, "2.0");

		Element channel = rss.appendElement(Constants.CHANNEL);
		channel.appendElement(Constants.TITLE).appendText(title);
		channel.appendElement(Constants.DESCRIPTION).appendText(description);

		Element item;

		for (Tuple tuple : tuples) {
			item = channel.appendElement(Constants.ITEM);
			item.appendElement(Constants.TITLE).appendText(tuple.getString(Constants.TITLE));
			item.appendElement(Constants.DESCRIPTION).appendText(tuple.getHTMLText(Constants.DESCRIPTION));
			item.appendElement(Constants.LINK).appendText(
					urlRoot + "/" + type + "/" + tuple.getString(Constants.ID) + "?" + Constants.LANG + "=" + lang);
			item.appendElement(PUBDATE).appendText(tuple.getUTCDatetime(Constants.PUB_DATE).toString());
		}
	}
}