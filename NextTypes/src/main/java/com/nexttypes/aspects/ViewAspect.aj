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

package com.nexttypes.aspects;

import org.aspectj.lang.JoinPoint;

import com.nexttypes.datatypes.Content;
import com.nexttypes.nodes.Node;
import com.nexttypes.views.View;
import com.nexttypes.protocol.http.HTTPStatus;

public aspect ViewAspect {
	Content around(String type, String id, String lang, String view, String etag) :
		execution(* View.get(..)) && args(type, id, lang, view, etag) {

		Content content = checkModification(thisJoinPoint, type, id, etag);

		if (content == null) {
			content = proceed(type, id, lang, view, etag);
		}

		return content;
	}

	Content around(String type, String id, String field, String etag) :
		execution(* View.getField(..)) && args(type, id, field, etag) {

		Content content = checkModification(thisJoinPoint, type, id, etag);

		if (content == null) {
			content = proceed(type, id, field, etag);
		}

		return content;
	}

	Content around(String type, String id, String field, String element, String lang, String view, String etag) :
	execution(* View.getElement(..)) && args(type, id, field, element, lang, view, etag) {

		Content content = checkModification(thisJoinPoint, type, id, etag);

		if (content == null) {
			content = proceed(type, id, field, element, lang, view, etag);
		}

		return content;
	}

	Content checkModification(JoinPoint joinPoint, String type, String id, String etag) {
		Content content = null;

		if (etag != null) {
			View viewObject = ((View) joinPoint.getTarget());
			Node nextNode = viewObject.getNextNode();

			String nodeETag = nextNode.getETag(type, id);

			if (nodeETag != null && nodeETag.equals(etag)) {
				content = new Content(HTTPStatus.NOT_MODIFIED);
			}
		}

		return content;
	}
}