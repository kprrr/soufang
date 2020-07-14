package com.migu.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JSONUtils {

	private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

	private static final GsonBuilder builder = new GsonBuilder();

	private static final Gson gs = new Gson();

	static {
		builder.setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls();
	}

	public static String toString(Object obj) {
		return builder.excludeFieldsWithoutExposeAnnotation().create().toJson(obj);
	}

	public static String toStringByGs(Object obj) {
		return gs.toJson(obj);
	}

	public static <T> T toBean(String json, Class<T> clz) {
		T t = null;
		try {
			t = builder.create().fromJson(json, clz);
		} catch (Exception e) {
			logger.error("error json format:" + json, e);
		}
		return t;
	}

	public static <T> List<T> toList(String jsonArray, Class<T> clz) {
		List<T> rslt = null;
		try {
			rslt = builder.create().fromJson(jsonArray, new TypeToken<List<T>>() {
			}.getType());
		} catch (Exception e) {
			logger.error("error json format:" + jsonArray, e);
		}
		return rslt;
	}
}