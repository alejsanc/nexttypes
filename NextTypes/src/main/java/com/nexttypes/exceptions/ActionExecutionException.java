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

package com.nexttypes.exceptions;

import com.nexttypes.settings.Strings;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

public class ActionExecutionException extends ActionException {
	private static final long serialVersionUID = 1L;

	public ActionExecutionException(String type, String action, Exception parentException) {
		super(type, action, Constants.ACTION_EXECUTION_ERROR);
		this.parentException = parentException;
	}

	@Override
	public String getMessage(Strings strings) {
		String typeName = strings.getTypeName(type);
		String actionName = strings.getActionName(type, action);
		
		String message = parentException.getMessage();
		
		if (message == null) {
			Throwable cause = parentException.getCause();

			if (cause != null) {
				message = Utils.getExceptionMessage(cause);
			} else {
				message = Utils.getExceptionMessage(parentException);
			}
		}

		return strings.gts(type, Constants.ACTION_EXECUTION_ERROR) + ": "
			+ typeName + "::" + actionName + ": " + message;
	}
}