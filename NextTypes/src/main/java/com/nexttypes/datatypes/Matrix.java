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

import java.util.LinkedHashMap;
import java.util.TreeSet;

import com.nexttypes.exceptions.NXException;
import com.nexttypes.system.KeyWords;

public class Matrix {
	
	protected LinkedHashMap<String, Object> matrix = new LinkedHashMap<>();
	protected LinkedHashMap<String, TreeSet<String>> keys = new LinkedHashMap<>();
	
	public Matrix(Tuple[] tuples, String[] axes) {
		
		if (axes == null || axes.length == 0) {
			throw new NXException(KeyWords.EMPTY_AXES_LIST);
		}
		
		for (String axis : axes) {
			keys.put(axis, new TreeSet<String>());
		}
		
		LinkedHashMap<String, Object> parent = matrix;
		
		int lastAxis = axes.length - 1;
		
		for (Tuple tuple : tuples) {
			for (int x = 0; x <= lastAxis; x++) {
				
				String axis = axes[x];
				String axisValue = tuple.getString(axis);
				
				tuple.remove(axis);
				
				keys.get(axis).add(axisValue);
				
				if (x == lastAxis) {
					parent.put(axisValue, tuple);
					parent = matrix;
				} else {
					LinkedHashMap<String, Object> child 
						= (LinkedHashMap<String, Object>) parent.get(axisValue);
					
					if (child == null) {
						child = new LinkedHashMap<>();
						parent.put(axisValue, child);
					}
					
					parent = child;
				}
			}
		}
	}
	
	public Tuple get(String... axes) {
		Tuple tuple = null;
		LinkedHashMap<String, Object> parent = matrix;
		
		int lastAxis = axes.length - 1;
		
		for (int x = 0; x <= lastAxis; x++) {
			
			Object value = parent.get(axes[x]);
			
			if (x == lastAxis) {
				tuple = (Tuple) value;
			} else {
				parent = (LinkedHashMap<String, Object>) value;
			}
		}
		
		return tuple;
	}
	
	public LinkedHashMap<String, TreeSet<String>> getKeys() {
		return keys;
	}
	
	public String[] getKeys(String axis) {
		return keys.get(axis).toArray(new String[] {});
	}
}


