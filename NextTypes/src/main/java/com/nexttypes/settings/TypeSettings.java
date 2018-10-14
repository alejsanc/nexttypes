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

package com.nexttypes.settings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Properties;

import com.nexttypes.datatypes.FieldRange;
import com.nexttypes.datatypes.PT;
import com.nexttypes.datatypes.Tuple;
import com.nexttypes.datatypes.TypeField;
import com.nexttypes.system.Constants;
import com.nexttypes.system.Utils;

public class TypeSettings extends Settings {

	public TypeSettings(ArrayList<Properties> settings) {
		super(settings);
	}

	public String gts(String setting) {
		return gts(null, setting);
	}

	public String gts(String type, String setting) {
		return getTypeString(type, setting);
	}

	public String gts(String type, String setting, String defaultValue) {
		return getTypeString(type, setting, defaultValue);
	}

	public String getTypeString(String type, String setting) {
		return getTypeString(type, new String[] { setting });
	}

	public String getTypeString(String type, String[] setting) {
		String value = null;

		if (type == null) {
			value = getString("*", setting);
		} else {
			String prefix = type;

			value = getString(prefix, setting);

			if (value == null) {
				while (true) {
					value = getString(prefix + "*", setting);
					if (value != null) {
						break;
					}

					int index = prefix.lastIndexOf("_");

					if (index != -1) {
						prefix = prefix.substring(0, index);
					} else {
						value = getString("*", setting);
						break;
					}
				}
			}
		}

		return value;
	}

	public String getTypeString(String type, String setting, String defaultValue) {
		String value = getTypeString(type, setting);
		return value != null ? value : defaultValue;
	}

	public String getFieldString(String type, String field, String setting) {
		return getTypeString(type, new String[] {
				Constants.FIELDS + "." + field + "." + setting,
				Constants.FIELDS + "." + setting
		});
	}

	public String getActionString(String type, String action, String setting) {
		return getTypeString(type, new String[] {
				Constants.ACTIONS + "." + action + "." + setting,
				Constants.ACTIONS + "." + setting
		});
	}

	public String getActionFieldString(String type, String action, String field, String setting) {
		return getTypeString(type, new String[] {
				Constants.ACTIONS + "." + action + "." + Constants.FIELDS + "." + field + "." + setting,
				Constants.ACTIONS + "." + action + "." + Constants.FIELDS + "." + setting,
				Constants.ACTIONS + "." + Constants.FIELDS + "." + field + "." + setting,
				Constants.ACTIONS + "." + Constants.FIELDS + "." + setting
		});
	}

	public String getFieldString(String type, String field, String setting, String defaultValue) {
		return getTypeString(type, Constants.FIELDS + "." + field + "." + setting, defaultValue);
	}

	public String getActionString(String type, String action, String setting, String defaultValue) {
		return getTypeString(type, Constants.ACTIONS + "." + action + "." + setting, defaultValue);
	}

	public String getActionFieldString(String type, String action, String field, String setting, String defaultValue) {
		return getTypeString(type, Constants.ACTIONS + "." + action + "." + Constants.FIELDS + "."
				+ field + "." + setting, defaultValue);
	}

	public String[] getTypeStringArray(String type, String setting) {
		return Utils.split(getTypeString(type, setting));
	}

	public String[] getTypeStringArray(String type, String[] settings) {
		return Utils.split(getTypeString(type, settings));
	}

	public String[] getTypeStringArray(String type, String setting, String[] defaultValues) {
		String[] values = getTypeStringArray(type, setting);
		return values != null ? values : defaultValues;
	}

	public Short getTypeInt16(String type, String setting) {
		return Tuple.parseInt16(getTypeString(type, setting));
	}

	public Integer getTypeInt32(String type, String setting) {
		return Tuple.parseInt32(getTypeString(type, setting));
	}

	public Long getTypeInt64(String type, String setting) {
		return Tuple.parseInt64(getTypeString(type, setting));
	}

	public Float getTypeFloat32(String type, String setting) {
		return Tuple.parseFloat32(getTypeString(type, setting));
	}

	public Double getTypeFloat64(String type, String setting) {
		return Tuple.parseFloat64(getTypeString(type, setting));
	}

	public BigDecimal getTypeNumeric(String type, String setting) {
		return Tuple.parseNumeric(getTypeString(type, setting));
	}

	public Boolean getTypeBoolean(String type, String setting) {
		return Tuple.parseBoolean(getTypeString(type, setting));
	}

	public Tuple getTypeTuple(String type, String setting) {
		return Tuple.parseTuple(getTypeString(type, setting));
	}

	public String getView(String type, String view) {
		return getTypeString(type, Constants.VIEWS + "." + view);
	}

	public Short getFieldInt16(String type, String field, String setting) {
		return Tuple.parseInt16(getFieldString(type, field, setting));
	}

	public Integer getFieldInt32(String type, String field, String setting) {
		return Tuple.parseInt32(getFieldString(type, field, setting));
	}

	public Long getFieldInt64(String type, String field, String setting) {
		return Tuple.parseInt64(getFieldString(type, field, setting));
	}

	public Float getFieldFloat32(String type, String field, String setting) {
		return Tuple.parseFloat32(getFieldString(type, field, setting));
	}

	public Double getFieldFloat64(String type, String field, String setting) {
		return Tuple.parseFloat64(getFieldString(type, field, setting));
	}

	public BigDecimal getFieldNumeric(String type, String field, String setting) {
		return Tuple.parseNumeric(getFieldString(type, field, setting));
	}
	
	public BigDecimal getFieldNumeric(String type, String field, String setting, BigDecimal min,
			BigDecimal max) {
		return Tuple.parseNumeric(getFieldString(type, field, setting), min, max);
	}
	
	public BigDecimal getFieldNumeric(String type, String field, String setting, TypeField typeField) {
		return Tuple.parseNumeric(getFieldString(type, field, setting), typeField);
	}
	
	public LocalDate getFieldDate(String type, String field, String setting) {
		return Tuple.parseDate(getFieldString(type, field, setting));
	}
	
	public LocalTime getFieldTime(String type, String field, String setting) {
		return Tuple.parseTime(getFieldString(type, field, setting));
	}
	
	public LocalDateTime getFieldDateTime(String type, String field, String setting) {
		return Tuple.parseDateTime(getFieldString(type, field, setting));
	}

	public String[] getActionStringArray(String type, String action, String setting) {
		return Utils.split(getActionString(type, action, setting));
	}

	public Boolean getActionBoolean(String type, String action, String setting) {
		return Tuple.parseBoolean(getActionString(type, action, setting));
	}

	public Integer getActionInt32(String type, String action, String setting) {
		return Tuple.parseInt32(getActionString(type, action, setting));
	}

	public Long getActionInt64(String type, String action, String setting) {
		return Tuple.parseInt64(getActionString(type, action, setting));
	}

	public BigDecimal getActionNumeric(String type, String action, String setting) {
		return Tuple.parseNumeric(getActionString(type, action, setting));
	}

	public String[] getFieldStringArray(String type, String field, String setting) {
		return Utils.split(getFieldString(type, field, setting));
	}

	public String[] getFieldStringArray(String type, String field, String setting, String[] defaultValues) {
		String[] values = getFieldStringArray(type, field, setting);
		return values != null ? values : defaultValues;
	}

	public String[] getActionFieldStringArray(String type, String action, String field, String setting) {
		return Utils.split(getActionFieldString(type, action, field, setting));
	}

	public Short getActionFieldInt16(String type, String action, String field, String setting) {
		return Tuple.parseInt16(getActionFieldString(type, action, field, setting));
	}

	public Integer getActionFieldInt32(String type, String action, String field, String setting) {
		return Tuple.parseInt32(getActionFieldString(type, action, field, setting));
	}

	public Long getActionFieldInt64(String type, String action, String field, String setting) {
		return Tuple.parseInt64(getActionFieldString(type, action, field, setting));
	}

	public Float getActionFieldFloat32(String type, String action, String field, String setting) {
		return Tuple.parseFloat32(getActionFieldString(type, action, field, setting));
	}

	public Double getActionFieldFloat64(String type, String action, String field, String setting) {
		return Tuple.parseFloat64(getActionFieldString(type, action, field, setting));
	}

	public BigDecimal getActionFieldNumeric(String type, String action, String field, String setting) {
		return Tuple.parseNumeric(getActionFieldString(type, action, field, setting));
	}
	
	public BigDecimal getActionFieldNumeric(String type, String action, String field, String setting,
			BigDecimal min, BigDecimal max) {
		return Tuple.parseNumeric(getActionFieldString(type, action, field, setting), min, max);
	}
	
	public BigDecimal getActionFieldNumeric(String type, String action, String field, String setting,
			TypeField typeField) {
		return Tuple.parseNumeric(getActionFieldString(type, action, field, setting), typeField);
	}
	
	public LocalDate getActionFieldDate(String type, String action, String field, String setting) {
		return Tuple.parseDate(getActionFieldString(type, action, field, setting));
	}
	
	public LocalTime getActionFieldTime(String type, String action, String field, String setting) {
		return Tuple.parseTime(getActionFieldString(type, action, field, setting));
	}
	
	public LocalDateTime getActionFieldDateTime(String type, String action, String field, String setting) {
		return Tuple.parseDateTime(getActionFieldString(type, action, field, setting));
	}
}
