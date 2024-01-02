/*
 * Copyright 2015-2024 Alejandro Sánchez <alex@nexttypes.com>
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

package com.nexttypes.protocol.http;

public enum HTTPStatus {
	OK(200),
	MULTI_STATUS(207),
	MOVED_PERMANENTLY(301),
	FOUND(302),
	NOT_MODIFIED(304),
	UNAUTHORIZED(401),
	NOT_FOUND(404),
	METHOD_NOT_ALLOWED(405),
	TOO_MANY_REQUESTS(429),
	INTERNAL_SERVER_ERROR(500);

	protected int status;

	private HTTPStatus(int status) {
		this.status = status;
	}

	public int toInt32() {
		return status;
	}
}