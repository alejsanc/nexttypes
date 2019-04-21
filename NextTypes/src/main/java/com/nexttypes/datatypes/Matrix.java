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


