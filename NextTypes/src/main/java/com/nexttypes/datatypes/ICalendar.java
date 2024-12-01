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

package com.nexttypes.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.KeyWords;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;

public class ICalendar {

	protected Calendar calendar;
	
	public ICalendar(String url, Tuple... events) {
		
		calendar = new Calendar();
		calendar.add(new ProdId(KeyWords.NEXTTYPES));
		
		Version version = new Version();
		version.setValue(Version.VALUE_2_0);
		calendar.add(version);
		
		calendar.add(new CalScale(CalScale.VALUE_GREGORIAN));

		try {
			for (Tuple event : events) {
				String id = event.getString(KeyWords.ID); 
				String summary = event.getString(KeyWords.SUMMARY);
				LocalDateTime startDateTime = event.getDateTime(KeyWords.START_DATE);
				LocalDateTime endDateTime = event.getDateTime(KeyWords.END_DATE);
				String description = event.getString(KeyWords.DESCRIPTION);

				VEvent vevent = null;

				if (endDateTime == null) {
					vevent = new VEvent(startDateTime, summary);
				} else {
					vevent = new VEvent(startDateTime, endDateTime, summary);
				}

				if (description != null) {
					vevent.add(new Description(description));
				}

				vevent.add(new Attach(new URI(url + id)));

				vevent.add(new RandomUidGenerator().generateUid());

				calendar.add(vevent);
			}

			calendar.validate();

		} catch (URISyntaxException  e) {
			throw new NXException(e);
		}
	}

	public ICalendar(byte[] data) {
		
		try {
			CalendarBuilder builder = new CalendarBuilder();
			calendar = builder.build(new ByteArrayInputStream(data));
			calendar.validate();
		} catch (ParserException | IOException e) {
			throw new NXException(e);
		}
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public VEvent getFirstEvent() {
		return (VEvent) calendar.getComponents("VEVENT").get(0);
	}

	public List<CalendarComponent> getComponents() {
		return calendar.getComponents();
	}

	@Override
	public String toString() {
		return calendar.toString();
	}
}