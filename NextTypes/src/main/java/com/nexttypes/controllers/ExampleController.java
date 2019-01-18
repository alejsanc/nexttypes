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

package com.nexttypes.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import javax.mail.internet.InternetAddress;

import com.nexttypes.datatypes.ActionResult;
import com.nexttypes.datatypes.Audio;
import com.nexttypes.datatypes.Color;
import com.nexttypes.datatypes.Document;
import com.nexttypes.datatypes.HTMLFragment;
import com.nexttypes.datatypes.File;
import com.nexttypes.datatypes.Image;
import com.nexttypes.datatypes.JSON;
import com.nexttypes.datatypes.URL;
import com.nexttypes.datatypes.Video;
import com.nexttypes.datatypes.XML;
import com.nexttypes.nodes.Node;
import com.nexttypes.system.Action;
import com.nexttypes.system.Controller;

public class ExampleController extends Controller {

	public static final String TRY = "try";
	public static final String OK = "OK";

	public ExampleController(String type, String user, String[] groups, Node nextNode) {
		super(type, user, groups, nextNode);
		
		actionsInfo = "/com/nexttypes/controllers/example-actions.json";
	}

	@Action(TRY)
	public ActionResult exampleAction(String[] objects, Short int16, Integer int32, Long int64, Float float32, Double float64,
			BigDecimal numeric, Boolean bool, String string, String text, HTMLFragment html, JSON json, XML xml,
			URL url, InternetAddress email, String tel, LocalDate date, LocalTime time, LocalDateTime dateTime,
			ZoneId timezone, Color color, byte[] binary, File file, Image image, Audio audio, Video video, Document document,
			String password, String article) {

		return new ActionResult(OK);
	}
}