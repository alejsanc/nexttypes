package com.nexttypes.datatypes;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nexttypes.system.KeyWords;

@JsonPropertyOrder({ KeyWords.COUNT, KeyWords.ITEMS })
public class Names {
	protected LinkedHashMap<String, String> items;
	protected Long count;
	
	public Names(LinkedHashMap<String, String> items, Long count) {
		this.items = items;
		this.count = count;
	}
	
	@JsonProperty(KeyWords.ITEMS)
	public LinkedHashMap<String, String> getItems() {
		return items;
	}
	
	@JsonProperty(KeyWords.COUNT)
	public Long getCount() {
		return count;
	}
}
