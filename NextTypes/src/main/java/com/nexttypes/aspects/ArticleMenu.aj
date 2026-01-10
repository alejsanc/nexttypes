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

package com.nexttypes.aspects;
 
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.settings.TypeSettings;
import com.nexttypes.system.Action;
import com.nexttypes.system.KeyWords;
import com.nexttypes.views.ArticleView;
import com.nexttypes.views.HTMLView;

public aspect ArticleMenu {
	public final String ARTICLE_MENU = "aspects.article_menu";

	before(String type, String lang, String view) : execution(* HTMLView.menu(..)) && args(type, lang, view) {

		HTMLView target = (HTMLView) thisJoinPoint.getTarget();

		TypeSettings typeSettings = target.getTypeSettings();
		Boolean articleMenu = typeSettings.getTypeBoolean(type, ARTICLE_MENU);

		if (articleMenu != null && articleMenu) {

			Element menuElement = target.getDocument().getElementById(KeyWords.MENU);

			if (menuElement != null) {

				menuElement.appendElement(HTML.DIV).addClass(HTMLView.MENU_TITLE)
						.appendText(target.getLanguageSettings().gts(type, ArticleView.CATEGORIES) + ":");

				String sql = "select name, category from category_language where language = ? order by name";
				Tuple[] tuples = target.getNextNode().query(sql, lang);

				Element ul = menuElement.appendElement(HTML.UL);

				for (Tuple tuple : tuples) {
					ul.appendElement("li").appendElement(target.anchor(tuple.getString(KeyWords.NAME),
							target.hrefURL("/" + ArticleView.ARTICLE + "?" + Action.PREVIEW
							+ target.parameter(ArticleView.CATEGORY, tuple.getString(ArticleView.CATEGORY)),
							lang, view)));
				}
			}
		}
	}
}