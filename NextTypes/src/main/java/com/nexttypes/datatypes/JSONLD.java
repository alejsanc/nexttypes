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

package com.nexttypes.datatypes;

import java.time.ZonedDateTime;

public class JSONLD extends JSON {

	private static final long serialVersionUID = 1L;
	
	public static final String SCHEMA_ORG = "http://schema.org/";
	
	public static final String LD_TYPE = "@type";
	public static final String LD_CONTEXT = "@context";
	
	public static final String ARTICLE = "Article";
	public static final String PERSON = "Person";
	public static final String ORGANIZATION = "Organization";
	public static final String IMAGE_OBJECT = "ImageObject";
	
	public static final String HEADLINE = "headline";
	public static final String IMAGE = "image";
	public static final String AUTHOR = "author";
	public static final String NAME = "name";
	public static final String PUBLISHER = "publisher";
	public static final String LOGO = "logo";
	public static final String URL = "url";
	public static final String DATE_PUBLISHED = "datePublished";
	public static final String DATE_MODIFIED = "dateModified";
	
	public JSONObject createObject(boolean context) {
		JSONObject object = super.createObject();
		
		if (context) {
			object.put(LD_CONTEXT, SCHEMA_ORG);
		}
		
		return object;
	}
	
	public JSONObject article(String headline, String image, Tuple[] authors, String publisher, 
			String publisherLogo, ZonedDateTime datePublished, ZonedDateTime dateModified) {
		return article(headline, image, authors, publisher, publisherLogo, datePublished,
				dateModified, true);
	}

	protected JSONObject article(String headline, String image, Tuple[] authors, String publisher, 
			String publisherLogo, ZonedDateTime datePublished, ZonedDateTime dateModified,
			boolean context) {
	
		JSONObject article = createObject(context)
			.put(LD_TYPE, ARTICLE)
			.put(HEADLINE, headline)
			.put(IMAGE, image);

		if (authors != null && authors.length > 0) {
			JSONArray array = article.putArray(AUTHOR);

			for (Tuple author : authors) {
				array.add(person(author.getString(NAME), false));
			}
		}
		
		article.put(PUBLISHER, organization(publisher, publisherLogo, false))
			.put(DATE_PUBLISHED, datePublished)
			.put(DATE_MODIFIED, dateModified);
		
		return article;
	}
	
	public JSONObject person(String name) {
		return person(name, true);
	}
	
	protected JSONObject person(String name, boolean context) {
		return createObject(context)
				.put(LD_TYPE, PERSON)
				.put(NAME, name);
	}
	
	public JSONObject organization(String name, String logo) {
		return organization(name, logo, true);
	}
	
	protected JSONObject organization(String name, String logo, boolean context) {
		return createObject(context)
				.put(LD_TYPE, ORGANIZATION)
				.put(NAME, name)
				.put(LOGO, imageObject(logo, false));
	}
	
	public JSONObject imageObject(String url) {
		return imageObject(url, true);
	}
	
	protected JSONObject imageObject(String url, boolean context) {
		return createObject(context)
				.put(LD_TYPE, IMAGE_OBJECT)
				.put(URL, url);
	}
}