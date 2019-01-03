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

package com.nexttypes.datatypes;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class ObjectInfo {
	protected String id;
	protected ZonedDateTime udate;

	public ObjectInfo(String id, ZonedDateTime udate) {
		this.id = id;
		this.udate = udate;
	}

	public ObjectInfo(String id, Timestamp udate) {
		this(id, Tuple.parseUTCDateTime(udate));
	}

	public String getId() {
		return id;
	}

	public ZonedDateTime getUDate() {
		return udate;
	}
}
