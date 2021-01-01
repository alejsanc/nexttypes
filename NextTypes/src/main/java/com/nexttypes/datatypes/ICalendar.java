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

package com.nexttypes.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;

import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.Constants;
import com.nexttypes.system.KeyWords;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.util.RandomUidGenerator;

public class ICalendar {

	protected Calendar calendar;
	
	protected ICalendar() {
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl",
				MapTimeZoneCache.class.getName());
	}

	public ICalendar(String url, Tuple... events) {
		
		this();
		
		calendar = new Calendar();
		calendar.getProperties().add(new ProdId(KeyWords.NEXTTYPES));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		try {
			for (Tuple event : events) {
				String id = event.getString(KeyWords.ID); 
				String summary = event.getString(KeyWords.SUMMARY);
				String startDate = event.getDateTime(KeyWords.START_DATE)
						.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT));
				String endDate = event.getDateTime(KeyWords.END_DATE)
						.format(DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT));
				String description = event.getString(KeyWords.DESCRIPTION);

				DateTime startDateTime = new DateTime(startDate, Constants.DATETIME_FORMAT, null);

				VEvent vevent = null;

				if (endDate == null) {
					vevent = new VEvent(startDateTime, summary);
				} else {
					DateTime endDateTime = new DateTime(endDate, Constants.DATETIME_FORMAT, null);
					vevent = new VEvent(startDateTime, endDateTime, summary);
				}

				if (description != null) {
					vevent.getProperties().add(new Description(description));
				}

				vevent.getProperties().add(new Attach(new URI(url + id)));

				vevent.getProperties().add(new RandomUidGenerator().generateUid());

				calendar.getComponents().add(vevent);
			}

			calendar.validate();

		} catch (ParseException | URISyntaxException  e) {
			throw new NXException(e);
		}
	}

	public ICalendar(byte[] data) {
		
		this();
		
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

	public ComponentList<CalendarComponent> getComponents() {
		return calendar.getComponents();
	}

	@Override
	public String toString() {
		return calendar.toString();
	}
}