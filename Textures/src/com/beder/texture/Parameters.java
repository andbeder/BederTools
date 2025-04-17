package com.beder.texture;

import java.util.TreeMap;

public class Parameters extends TreeMap<String, Double> {

	public double get(String s, double def) {
		if (containsKey(s)) {
			return get(s);
		}
		return def;
	}
	
	public void put(String key, String val) {
		double d = Double.parseDouble(val);
		put(key, d);
	}
}
