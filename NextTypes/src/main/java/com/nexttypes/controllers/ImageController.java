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

package com.nexttypes.controllers;

import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.Auth;
import com.nexttypes.datatypes.Image;
import com.nexttypes.exceptions.ObjectException;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.KeyWords;
import com.nexttypes.system.Controller;

public class ImageController extends Controller {

	public final String RESIZE = "resize";
	public final String IMAGE_NOT_FOUND = "image_not_found";
	public final String IMAGE_SUCCESSFULLY_RESIZED = "image_successfully_resized";
	public final String IMAGES_SUCCESSFULLY_RESIZED = "images_successfully_resized";

	public ImageController(String type, Auth auth, Node nextNode) {
		super(type, auth, nextNode);
		
		actionsInfo = "/com/nexttypes/controllers/image-actions.json";
	}

	@Action(RESIZE)
	public ActionResult resize(String[] objects, Integer width, Integer height) {
		
		for (String id : objects) {
			Image image = getImageField(id, KeyWords.IMAGE);
			if (image != null) {
				updateField(id, KeyWords.IMAGE, image.resize(width, height));
			} else {
				throw new ObjectException(type, id, IMAGE_NOT_FOUND);
			}
		}

		String message = objects.length == 1 ? IMAGE_SUCCESSFULLY_RESIZED : IMAGES_SUCCESSFULLY_RESIZED;

		return new ActionResult(languageSettings.gts(type, message));
	}
}