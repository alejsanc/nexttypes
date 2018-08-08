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

package com.nexttypes.datatypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexttypes.system.Constants;

public class Audio extends File {
	private static final long serialVersionUID = 1L;

	public Audio() {
		type = PT.AUDIO;
	}
	
	public Audio(File file) {
		super(file);
		
		type = PT.AUDIO;
	}
	
	public Audio(byte[] content) {
		this(null, content);
	}

	public Audio(String name, byte[] content) {
		super(name, content);
		
		type = PT.AUDIO;
	}

	@JsonCreator
	public Audio(@JsonProperty(Constants.CONTENT) byte[] content,
			@JsonProperty(Constants.CONTENT_TYPE) String contentType) {
		super(content, contentType);
		
		type = PT.AUDIO;
	}
}
