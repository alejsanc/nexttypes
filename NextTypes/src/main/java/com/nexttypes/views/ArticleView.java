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

package com.nexttypes.views;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.nexttypes.datatypes.Content;
import com.nexttypes.datatypes.FieldReference;
import com.nexttypes.datatypes.Filter;
import com.nexttypes.datatypes.HTML;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.RSS;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.Tuples;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.enums.Order;
import com.nexttypes.enums.Component;
import com.nexttypes.enums.Format;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.system.Constants;

public class ArticleView extends HTMLView {

	public static final String CATEGORY = "category";
	public static final String CATEGORIES = "categories";
	
	protected String category;
	protected String categoryParameter;

	public ArticleView(HTMLView view) {
		super(view);
	}

	public ArticleView(HTTPRequest request) {
		super(request);
	}

	@Override
	public Content get(String type, String id, String lang, String view, String etag) {

		StringBuilder sql = new StringBuilder(
				"select"
						+ " a.id,"
						+ " a.cdate,"
						+ " coalesce(al.udate, a.udate) as udate,"
						+ " coalesce(al.title, a.id) as title,"
						+ " al.text"

				+ " from"
					+ " article a left join article_language al on (a.id=al.article and al.language=?)"

				+ " where"
					+ " a.id=?");

		String typeFilters = typeSettings.gts(type, Constants.FILTERS);
		if (typeFilters != null) {
			sql.append(" and " + typeFilters);
		}

		Tuple tuple = nextNode.getTuple(sql.toString(), lang, id);

		loadTemplate(type, lang, view);

		if (tuple == null) {
			return objectNotFound(type, id, lang, view);
		}

		Element article = main.appendElement(HTML.ARTICLE);

		String title = tuple.getString(Constants.TITLE);
		document.getTitle().appendText(title);
		article.appendElement(HTML.H1).appendText(title);

		HTMLFragment text = tuple.getHTML(Constants.TEXT, lang,
				typeSettings.getFieldString(type, Constants.TEXT, Constants.HTML_ALLOWED_TAGS));
		if (text != null) {
			article.appendFragment(text);
		}

		main.appendElement(dates(type, tuple.getUTCDatetime(Constants.CDATE), tuple.getUTCDatetime(Constants.UDATE)));

		return render(type);
	}
	
	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		Content content = null;
		
		if (Constants.RSS.equals(view)) {
			StringBuilder sql = new StringBuilder(typeSettings.gts(type, Constants.RSS_SELECT));
					
			String category = request.getParameters().getString(CATEGORY);
			String typeFilters = typeSettings.gts(type, Constants.FILTERS);
			ArrayList<Object> parameters = new ArrayList<>();
			parameters.add(lang);
			
			if (category != null) {
				sql.append(" join article_category ac on (a.id = ac.article and ac.category = ?)");
				parameters.add(category);
			}
			
			if (typeFilters != null) {
				sql.append(" where " + typeFilters);
			}
			
			sql.append(" order by a.cdate desc");
				
			Tuple[] tuples = nextNode.query(sql.toString(), parameters.toArray());
			RSS rss = new RSS("article::" + category, "article::" + category, type, lang,
						request.getURIRoot(), tuples);
			content = new Content(rss.toString(), Format.RSS);
			
		} else {
			content = super.select(type, lang, view, ref, filters, search, order, offset, limit);
		}
		
		return content;
	}

	@Override
	public Content preview(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		category = request.getParameters().getString(CATEGORY);
		
		if (category != null) {
			categoryParameter = parameter(CATEGORY, category);
		}
		
		loadTemplate(type, lang, view);
			
		StringBuilder sql = new StringBuilder(
				"select"
						+ " a.id,"
						+ " a.image_link,"
						+ " a.cdate,"
						+ " coalesce(al.title, a.id) as title,"
						+ " left(al.text, 300) as text,"
						+ " case"
							+ " when ill.image is null then 'image_link'"
							+ " else 'image_link_language'"
						+ " end as image_type,"
						+ " case"
							+ " when ill.image is null then a.image_link"
							+ " else ill.id"
						+ " end as image_id"

				+ " from"
					+ " article a"
					+ " left join article_language al on (a.id = al.article and al.language = ?)"
					+ " left join image_link_language ill on (a.image_link = ill.image_link and ill.language = ?)");

		ArrayList<Object> parameters = new ArrayList<>();
		parameters.add(lang);
		parameters.add(lang);

		if (category != null) {
			setTitle(nextNode.getString("select name from category_language where category = ?"
					+ " and language = ?", category, lang));
			
			sql.append(" join article_category ac on (a.id = ac.article and ac.category = ?)");
			parameters.add(category);
		} 
		
		if (search != null) {
			main.appendElement(searchOutput(type, lang, view, ref, filters, search, order));
		}

		Tuples tuples = nextNode.select(type, sql, parameters, null, search,
				new String[] { "al.title", "al.text" }, "cdate desc", offset, limit);

		if (tuples.getCount() > 0) {
		
			for (Tuple tuple : tuples.getItems()) {
				String id = tuple.getString(Constants.ID);
				String title = tuple.getString(Constants.TITLE);
				String uri = uri(type, id, lang, view);

				Element article = main.appendElement(HTML.DIV).addClass(Constants.PREVIEW);
				article.appendElement(imageAnchor(title, uri, tuple.getString(Constants.IMAGE_TYPE),
					tuple.getString(Constants.IMAGE_ID), IMAGE));
				article.appendElement(time(tuple.getDatetime(Constants.CDATE)));
				article.appendElement(HTML.H2).appendText(title);
				article.appendElement(HTML.P).appendText(tuple.getHTMLText(Constants.TEXT) + " ... ")
					.appendElement(anchor(strings.gts(type, Constants.READ_MORE), uri));
			}

			main.appendElement(selectTableIndex(type, lang, view, ref, filters, search, order,
					tuples.getCount(), tuples.getOffset(), tuples.getLimit(), tuples.getMinLimit(),
					tuples.getMaxLimit(), tuples.getLimitIncrement(), Component.TYPE));
		} else {
			main.appendElement(HTML.P).appendText(strings.gts(Constants.NO_OBJECTS_FOUND));
		}

		return render(type);
	}
	
	@Override
	public String rssURI(String type, String lang, FieldReference ref) {
		String uri = super.rssURI(type, lang, ref);
		
		if (categoryParameter != null) {
			uri += categoryParameter;
		}
		
		return uri;
	}
	
	@Override
	public String selectTableURI(String type, String lang, String view, FieldReference ref, 
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
	
		String uri = super.selectTableURI(type, lang, view, ref, filters, search, order, offset, limit);
		
		if (categoryParameter != null) {
			uri += categoryParameter;
		}
		
		return uri;
	}
}