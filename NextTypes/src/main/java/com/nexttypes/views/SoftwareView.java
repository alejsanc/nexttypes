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

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.settings.Settings;
import com.nexttypes.system.KeyWords;

public class SoftwareView extends HTMLView {
	protected final String SETTINGS_ROOT = "software.";
	protected final String LICENSE = SETTINGS_ROOT + "license";
	protected final String LAST_RELEASE = SETTINGS_ROOT + "last_release";
	protected final String ALL_RELEASES = SETTINGS_ROOT + "all_releases";
	protected final String NOT_RELEASED = SETTINGS_ROOT + "not_released";
	protected final String DESCRIPTION = SETTINGS_ROOT + "description";
	protected final String DOCUMENTATION = SETTINGS_ROOT + "documentation";
	protected final String SOURCE_CODE = SETTINGS_ROOT + "source_code";
	protected final String SOFTWARE_RELEASE = "software_release";
	protected final String VERSION = "version";

	public SoftwareView(String type, HTMLView view) {
		super(type, view);
	}

	public SoftwareView(HTTPRequest request) {
		super(request);
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {
		String version_order = typeSettings.getFieldString(SOFTWARE_RELEASE, VERSION, KeyWords.ORDER);

		String sql =
				"select"
						+ " s.id,"
						+ " s.source_code,"
						+ " s.api,"
						+ " s.cdate,"
						+ " greatest(s.udate, sl.udate, sli.udate, sr.udate) as udate,"
						+ " coalesce(sl.title, s.id) as title,"
						+ " sl.description,"
						+ " sli.id as license_id,"
						+ " sli.name as license_name,"
						+ " sr.version,"
						+ " sr.link"

				+ " from"
					+ " software s"
					+ " left join software_language sl on (s.id=sl.software and sl.language=?)"
					+ " left join software_license sli on s.license=sli.id "
					+ " left join software_release sr on s.id=sr.software"

				+ " where"
					+ " s.id=?"
					+ " and (sr.id is null or sr.id=(select id from software_release order by "
						+ version_order + "desc limit 1))";

		Tuple tuple = nextNode.getTuple(sql, lang, id);

		loadTemplate(type, lang, view);

		if (tuple == null) {
			return objectNotFound(type, id, lang, view);
		}

		Element article = main.appendElement(HTML.ARTICLE);

		String title = tuple.getString("title");
		document.getTitle().appendText(title);
		article.appendElement(HTML.H1).appendText(title);

		article.appendElement(fieldOutput(languageSettings.getString(LICENSE), anchor(tuple.getString("license_name"),
				url("software_license", tuple.getString("license_id"), lang, view))));

		String version = tuple.getString("version");
		String lastRelease = languageSettings.getString(LAST_RELEASE);
		if (version != null) {
			article.appendElement(fieldOutput(lastRelease, anchor(version, tuple.getString("link")), " ",
					anchor(" - " + languageSettings.getString(ALL_RELEASES),
							url("software_release", lang, view) + "&ref=software:" + id + "&order=version:desc")));
		} else {
			article.appendElement(fieldOutput(lastRelease, languageSettings.getString(NOT_RELEASED)));
		}

		String sourceCode = tuple.getString("source_code");
		if (sourceCode != null) {
			article.appendElement(fieldOutput(languageSettings.getString(SOURCE_CODE), anchor(sourceCode)));
		}
		
		String api = tuple.getString("api");
		if (api != null) {
			article.appendElement(fieldOutput("API", anchor(api)));
		}

		HTMLFragment description = tuple.getHTML("description", lang,
				typeSettings.getFieldString(type, "description", Settings.HTML_ALLOWED_TAGS));
		if (description != null) {
			article.appendElement(fieldOutput(languageSettings.getString(DESCRIPTION), description));
		}

		main.appendElement(dates(type, tuple.getUTCDateTime(KeyWords.CDATE), tuple.getUTCDateTime(KeyWords.UDATE)));

		return render(type);
	}
}