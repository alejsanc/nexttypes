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

package com.nexttypes.system;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.nexttypes.settings.Settings;

public class TasksServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;

	protected ArrayList<Task> tasks = new ArrayList<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Context context = Context.get(getServletContext());
		Settings settings = context.getSettings(Settings.TASKS_SETTINGS);
		String[] taskNames = settings.getStringArray(KeyWords.TASKS);

		for (String taskName : taskNames) {
			Task task = Loader.loadTask(taskName, context);
			task.start();
			tasks.add(task);
		}
	}

	@Override
	public void destroy() {
		for (Task task : tasks) {
			task.finish();
		}
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {

	}
}
