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

import java.time.ZonedDateTime;
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
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.XML.Element;
import com.nexttypes.datatypes.JSONLD;
import com.nexttypes.enums.Order;
import com.nexttypes.enums.Component;
import com.nexttypes.enums.Format;
import com.nexttypes.protocol.http.HTTPRequest;
import com.nexttypes.system.Action;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;

public class ArticleView extends HTMLView {

	public static final String ARTICLE = "article";
	public static final String ARTICLE_DISCUSSION = "article_discussion";
	public static final String[] ARTICLE_DISCUSSION_FIELDS = new String[] {"title", "link"};
	public static final String AUTHORS = "authors";
	public static final String CATEGORY = "category";
	public static final String CATEGORIES = "categories";
	public static final String SHOW_ARTICLE_DISCUSSIONS = "show_article_discussions";
	public static final String SHOW_CATEGORIES = "show_categories";
	public static final String SHOW_AUTHORS = "show_authors";
	public static final String PUBLISHER = "publisher";
	
	protected String category;
	protected String categoryParameter;

	public ArticleView(String type, HTMLView view) {
		super(type, view);
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
						+ " al.text,"
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
					+ " left join image_link_language ill on (a.image_link = ill.image_link and ill.language = ?)"

				+ " where"
					+ " a.id = ?");

		String typeFilters = typeSettings.gts(type, KeyWords.FILTERS);
		if (typeFilters != null) {
			sql.append(" and " + typeFilters);
		}

		Tuple tuple = nextNode.getTuple(sql.toString(), lang, lang, id);

		loadTemplate(type, lang, view);
		
		if (tuple == null) {
			return objectNotFound(type, id, lang, view);
		}

		Element article = main.appendElement(HTML.ARTICLE);

		String title = tuple.getString(KeyWords.TITLE);
		document.getTitle().appendText(title);
		article.appendElement(HTML.H1).appendText(title);
		
		HTMLFragment text = tuple.getHTML(KeyWords.TEXT, lang,
				typeSettings.getFieldString(type, KeyWords.TEXT, KeyWords.HTML_ALLOWED_TAGS));
		if (text != null) {
			article.appendFragment(text);
		}

		ZonedDateTime cdate = tuple.getUTCDateTime(KeyWords.CDATE);
		ZonedDateTime udate = tuple.getUTCDateTime(KeyWords.UDATE);
		
		main.appendElement(dates(type, cdate, udate));
		
		Boolean showAuthors = typeSettings.getTypeBoolean(type, SHOW_AUTHORS);
				
		String authorsSQL =
				"select"
						+ " u.id,"
						+ " u.first_name || ' ' || u.second_name as name"
						
				+ " from"
					+ " \"user\" u"
					+ " join article_author aa on u.id = aa.author"
					
				+ " where"
					+ " aa.article = ?";
		
		Tuple[] authors = nextNode.query(authorsSQL, id);
		
		if (showAuthors && authors != null && authors.length > 0) {
			main.appendElement(listFieldOutput(type, strings.gts(type, AUTHORS), authors,
					KeyWords.USER, lang, view));
		}
		
		Boolean showCategories = typeSettings.getTypeBoolean(type, SHOW_CATEGORIES);
		
		if (showCategories) {
			
			String categoriesSQL =
					"select"
							+ " c.id,"
							+ " cl.name"
					
					+ " from"
						+ " category c"
						+ " left join category_language cl on (c.id = cl.category and cl.language = 'es')"
						+ " join article_category ac on c.id = ac.category"
						
					+ " where ac.article = ?";
			
			Tuple[] categories = nextNode.query(categoriesSQL, id);
			
			if (categories != null && categories.length > 0) {
				main.appendElement(categoriesListOutput(type, categories, lang, view));
			}
		}
		
		Boolean showArticleDiscussions = typeSettings.getTypeBoolean(type, SHOW_ARTICLE_DISCUSSIONS);
		
		if (showArticleDiscussions) {
			
			FieldReference articleReference = new FieldReference(ARTICLE, ARTICLE, id);
			
			main.appendElement(HTML.H2).appendText(strings.getReferenceName(ARTICLE_DISCUSSION,
					articleReference));
			
			String discussionsSQL =
					"select"
						+ " title,"
						+ " link"
						
					+ " from"
						+ " article_discussion"
						
					+ " where"
						+ " article = ?"
						+ " and published = true";
			
			Tuple[] discussions = nextNode.query(discussionsSQL, id);
		
			for (Tuple discussion : discussions) {
				URL link = discussion.getURL(KeyWords.LINK);
			
				main.appendElement(HTML.P).appendElement(anchor(link.getHost() + " - "
					+ discussion.getString(HTML.TITLE), link));
			}

			main.appendElement(insertForm(ARTICLE_DISCUSSION, ARTICLE_DISCUSSION_FIELDS, lang, view,
				articleReference, false, false, false, false, false));
		}
		
		String image = request.getURLRoot() + imageURL(tuple);
				
		String publisherLogo = request.getURLRoot() + typeSettings.gts(type, KeyWords.LOGO);
		
		head.appendElement(HTML.SCRIPT).setAttribute(HTML.TYPE, Format.JSON_LD.getContentType())
			.appendText(new JSONLD().article(title, image, authors, strings.gts(type, PUBLISHER),
					publisherLogo, cdate, udate));
		
		return render(type);
	}
	
	@Override
	public Content select(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		Content content = null;
		
		if (KeyWords.RSS.equals(view)) {
			StringBuilder sql = new StringBuilder(typeSettings.gts(type, Constants.RSS_SELECT));
					
			String category = request.getParameters().getString(CATEGORY);
			String typeFilters = typeSettings.gts(type, KeyWords.FILTERS);
			String title = strings.getTypeName(ARTICLE);
			ArrayList<Object> parameters = new ArrayList<>();
			parameters.add(lang);
			
			if (category != null) {
				title += "::" + categoryName(category, lang);
				sql.append(" left join article_category ac on (a.id = ac.article and ac.category = ?)");
				parameters.add(category);
			}
			
			if (typeFilters != null) {
				sql.append(" where " + typeFilters);
			}
			
			sql.append(" order by a.cdate desc limit 10");
				
			Tuple[] tuples = nextNode.query(sql.toString(), parameters.toArray());
			RSS rss = new RSS(title, strings.gts(type, KeyWords.DESCRIPTION), type, lang,
					request.getURLRoot(), tuples);
			content = new Content(rss.toString(), Format.RSS);
			
		} else {
			content = super.select(type, lang, view, ref, filters, search, order, offset, limit);
		}
		
		return content;
	}

	@Override
	public Content preview(String type, String lang, String view, FieldReference ref, Filter[] filters,
			String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {

		Boolean showAuthors = typeSettings.getTypeBoolean(type, SHOW_AUTHORS);
		Boolean showCategories = typeSettings.getTypeBoolean(type, SHOW_CATEGORIES);
		String categoryFilter = null;
		
		StringBuilder sql = new StringBuilder(
				"select"
						+ " a.id,"
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
						+ " end as image_id");
		
		StringBuilder fromSQL = new StringBuilder(
				" from"
					+ " article a"
					+ " left join article_language al on (a.id = al.article and al.language = ?)"
					+ " left join image_link_language ill on (a.image_link = ill.image_link and ill.language = ?)");
		
		if (showAuthors) {
			sql.append(", aas.authors");
			
			fromSQL.append(" left join ("
					+ "select"
						+ " aa.article,"
						+ " array_agg(array[u.id, u.first_name || ' ' || u.second_name])"
							+ " filter (where u.id is not null) as authors"
					
					+ " from"
						+ " \"user\" u"
						+ " join article_author aa on u.id = aa.author"
						
					+ " group by aa.article) aas on a.id = aas.article");
					
		}
		
		if (showCategories) {
			sql.append(", acs.categories");
			
			fromSQL.append(" left join ("
					+ "select"
						+ " ac.article,"
						+ " array_agg(array[c.id, cl.name]) filter (where c.id is not null) as categories"
					
					+ " from"
						+ " category c"
						+ " join article_category ac on c.id = ac.category"
						+ " left join category_language cl on (c.id = cl.category and cl.language = ?)"
						
					+ " group by ac.article) acs on a.id = acs.article");
		}

		ArrayList<Object> parameters = new ArrayList<>();
		parameters.add(lang);
		parameters.add(lang);
		parameters.add(lang);

		String previewTitle = strings.gts(type, KeyWords.PREVIEW_TITLE);
		
		category = request.getParameters().getString(CATEGORY);
		
		if (category != null) {
			categoryParameter = parameter(CATEGORY, category);
			
			loadTemplate(type, lang, view);
			
			Element searchForm = document.getElementById(KeyWords.SEARCH);
			searchForm.appendElement(input(HTML.HIDDEN, CATEGORY, CATEGORY, category));
			
			String categoryName = categoryName(category, lang);
			
			previewTitle += ": " + categoryName;
			main.appendElement(HTML.H1).appendText(categoryName);
			
			fromSQL.append(" join article_category ac on a.id = ac.article");
			
			categoryFilter = " ac.category = ?";
			
			parameters.add(category);
		} else {
			loadTemplate(type, lang, view);
		}

		document.getTitle().appendText(previewTitle);
				
		if (search != null) {
			main.appendElement(searchOutput(type, lang, view, ref, filters, search, order));
		}
		
		sql.append(fromSQL);

		Tuples tuples = nextNode.select(type, sql, parameters, categoryFilter, search,
				new String[] { "al.title", "al.text" }, null, "cdate desc", offset, limit);
		
		if (tuples.getCount() > 0) {
		
			for (Tuple tuple : tuples.getItems()) {
				String id = tuple.getString(KeyWords.ID);
				String title = tuple.getString(KeyWords.TITLE);
				String url = url(type, id, lang, view);

				Element article = main.appendElement(HTML.DIV).addClass(KeyWords.PREVIEW);
				article.appendElement(imageAnchor(title, url, tuple.getString(KeyWords.IMAGE_TYPE),
					tuple.getString(KeyWords.IMAGE_ID), IMAGE));
				article.appendElement(time(tuple.getDateTime(KeyWords.CDATE)));
				article.appendElement(HTML.H2).appendText(title);
				
				String text = tuple.getHTMLText(KeyWords.TEXT);
				
				if (text != null) {
					article.appendElement(HTML.P).appendText(text + " ... ")
						.appendElement(anchor(strings.gts(type, KeyWords.READ_MORE), url));
				}
				
				if (showAuthors) {
					String[][] authors = (String[][]) tuple.getArray(AUTHORS);
					
					if (authors != null) {
						article.appendElement(listFieldOutput(type, strings.gts(type, AUTHORS), authors,
								KeyWords.USER, lang, view));
					}
				}
				
				if (showCategories) {
					String[][] categories = (String[][]) tuple.getArray(CATEGORIES);
					
					if (categories != null) {
						article.appendElement(categoriesListOutput(type, categories, lang, view));
					}
				}
			}

			main.appendElement(selectTableIndex(type, lang, view, ref, filters, search, order,
					tuples.getCount(), tuples.getOffset(), tuples.getLimit(), tuples.getMinLimit(),
					tuples.getMaxLimit(), tuples.getLimitIncrement(), Component.TYPE));
		} else {
			main.appendElement(HTML.P).appendText(strings.gts(KeyWords.NO_OBJECTS_FOUND));
		}

		return render(type);
	}
	
	@Override
	public String rssURL(String type, String lang, FieldReference ref) {
		return categoryParameter(super.rssURL(type, lang, ref));
	}
	
	@Override
	public String selectTableURL(String type, String lang, String view, FieldReference ref, 
			Filter[] filters, String search, LinkedHashMap<String, Order> order, Long offset, Long limit) {
	
		return categoryParameter(super.selectTableURL(type, lang, view, ref, filters, search, order,
				offset, limit));
	}
	
	@Override
	public String deleteSearchURL(String type, String lang, String view, FieldReference ref,
			Filter[] filters, LinkedHashMap<String, Order> order) {
		return categoryParameter(super.deleteSearchURL(type, lang, view, ref, filters, order));
	}
	
	public String categoryParameter(String url) {
		if (categoryParameter != null) {
			url += categoryParameter;
		}
		
		return url;
	}
	
	public Element categoriesListOutput(String type, Object[] categories, String lang, String view) {
		return listFieldOutput(type, strings.gts(type, CATEGORIES), categories,
				category -> url(type, lang, view) + "&" + Action.PREVIEW + parameter(CATEGORY, category));
	}
	
	public String categoryName(String category, String lang) {
		return nextNode.getString("select name from category_language"
				+ " where category = ? and language = ?", category, lang);
	}
}