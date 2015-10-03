package io.yope.utils;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Serializer.class);
	
	// switch for debug/performance
	private static final Gson gsonDeserializer = deserializer();
	private static final Gson gsonSerializer = filteredSerailizer();
	
	public static String json(final Object entity) {
		return gsonSerializer.toJson(entity);
	}

	private static Gson filteredSerailizer() {
		GsonBuilder builder = new GsonBuilder();
		return builder.create();
	}

	private static Gson deserializer() {
		GsonBuilder builder = standardGsonBuilder();
		return builder.create();
	}

	private static GsonBuilder standardGsonBuilder() {
		return new GsonBuilder();
	}

	public static String json(final Object entity, final Set<String> fields) {
		Gson partialGson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes fat) {
				return !fields.contains(fat.getName());
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		}).create();

		return partialGson.toJson(entity);
	}

	public static <T> T createFromForm(Class<T> clazz, Map<String, ? extends List<String>> query) throws ReflectiveOperationException {
		try {
			T object = clazz.newInstance();

			return object;
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("Create From form exception", (e));
			throw e;
		}
	}

	public static <T> T createFromJson(Class<T> clazz, String json) {
		T obj = gsonDeserializer.fromJson(json, clazz);
		return obj;
	}

	public static <T> T createFromJsonStream(Type clazz, InputStreamReader json) {
		T obj = gsonDeserializer.fromJson(json, clazz);
		return obj;
	}
}
