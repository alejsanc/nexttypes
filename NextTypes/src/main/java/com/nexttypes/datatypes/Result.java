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

package com.nexttypes.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.KeyWords;

@JsonPropertyOrder({ KeyWords.COUNT, KeyWords.OFFSET, KeyWords.LIMIT, KeyWords.MIN_LIMIT, KeyWords.MAX_LIMIT,
		KeyWords.LIMIT_INCREMENT, KeyWords.ITEMS })
public abstract class Result {

	protected Long count;
	protected Long offset;
	protected Long limit;
	protected Long minLimit;
	protected Long maxLimit;
	protected Long limitIncrement;

	public Result(Long count, Long offset, Long limit, Long minLimit, Long maxLimit, Long limitIncrement) {
		this.count = count;
		
		if (count > 0) {
			
			this.offset = offset;
			this.limit = limit;
			this.minLimit = minLimit;
			this.maxLimit = maxLimit;
			this.limitIncrement = limitIncrement;

			long limits = count / limitIncrement;
			long lastLimit = count % limitIncrement == 0 ? limits * limitIncrement : (limits + 1) * limitIncrement;

			if (limit > lastLimit) {
				this.limit = lastLimit;
			}

			if (maxLimit > lastLimit) {
				this.maxLimit = lastLimit;
			}
		}
	}

	@JsonProperty(KeyWords.COUNT)
	public Long getCount() {
		return count;
	}

	@JsonProperty(KeyWords.OFFSET)
	public Long getOffset() {
		return offset;
	}

	@JsonProperty(KeyWords.LIMIT)
	public Long getLimit() {
		return limit;
	}

	@JsonProperty(KeyWords.MIN_LIMIT)
	public Long getMinLimit() {
		return minLimit;
	}

	@JsonProperty(KeyWords.MAX_LIMIT)
	public Long getMaxLimit() {
		return maxLimit;
	}

	@JsonProperty(KeyWords.LIMIT_INCREMENT)
	public Long getLimitIncrement() {
		return limitIncrement;
	}
}