package ru.clevertec.parsing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|\\d+(\\.\\d+)?|true|false|null|\\{[^{}]*\\}|\\[[^\\[\\]]*\\])");

    public static String serialize(Object obj) throws IllegalAccessException {
        Class<?> objClass = obj.getClass();
        Field[] declaredFields = objClass.getDeclaredFields();
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        boolean isFirst = true;
        for (Field field : declaredFields) {
            if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))) {
                if (Modifier.isPrivate(field.getModifiers())) field.setAccessible(true);
                if (!isFirst) {
                    jsonBuilder.append(",\n");
                }
                jsonBuilder.append(String.format("\"%s\":%s", field.getName(), serializeValue(field.get(obj))));
                isFirst = false;
            }
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private static String serializeValue(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String || obj instanceof UUID) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof OffsetDateTime || obj instanceof LocalDate) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Collection<?>) {
            return serializeCollection((Collection<?>) obj);
        }
        if (obj instanceof Map<?, ?>) {
            return serializeMap((Map<?, ?>) obj);
        }
        if (isCustomObject(obj)) {
            return serialize(obj);
        }
        return "\"" + obj + "\"";
    }

    private static String serializeCollection(Collection<?> collection) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        json.append("[");
        List<String> elements = new ArrayList<>();
        for (Object element : collection) {
            elements.add(serializeValue(element));
        }
        json.append(String.join(",\n", elements));
        json.append("]");
        return json.toString();
    }

    private static String serializeMap(Map<?, ?> map) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        json.append("{");
        List<String> elements = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            elements.add("\"" + entry.getKey().toString() + "\":" + serializeValue(entry.getValue()));
        }
        json.append(String.join(",", elements));
        json.append("}");
        return json.toString();
    }

    private static boolean isCustomObject(Object obj) {
        return !(obj.getClass().isPrimitive() ||
                obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Collection ||
                obj instanceof Map);
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        Matcher matcher = FIELD_PATTERN.matcher(json);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String valueString = matcher.group(2);
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = parseValue(valueString, field.getType(), field.getGenericType());
                field.set(instance, value);
            } catch (NoSuchFieldException e) {
                System.out.println("Поле не найдено: " + fieldName);
            }
        }

        return instance;
    }


    private static Object parseValue(String valueString, Class<?> fieldType, java.lang.reflect.Type genericType) throws Exception {
        if (fieldType == String.class) {
            return valueString.substring(1, valueString.length() - 1);
        }
        if (fieldType == UUID.class) {
            return UUID.fromString(valueString.substring(1, valueString.length() - 1));
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(valueString);
        }
        if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(valueString);
        }
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.parseBoolean(valueString);
        }
        if (fieldType == BigDecimal.class) {
            return new BigDecimal(valueString);
        }
        if (fieldType == OffsetDateTime.class) {
            return OffsetDateTime.parse(valueString.substring(1, valueString.length() - 1));
        }
        if (fieldType == LocalDate.class) {
            return LocalDate.parse(valueString.substring(1, valueString.length() - 1));
        }
        if (Collection.class.isAssignableFrom(fieldType)) {
            return parseCollection(valueString, genericType);
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            return parseMap(valueString, genericType);
        }
        return deserialize(valueString, fieldType);
    }

    private static Object parseCollection(String valueString, java.lang.reflect.Type genericType) throws Exception {
        String jsonArray = valueString.substring(1, valueString.length() - 1);
        List<Object> items = new ArrayList<>();
        Class<?> itemType = (Class<?>) ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments()[0]; // Тип элементов коллекции

        String[] elements = jsonArray.split(",(?=\\s*\\{)");
        for (String element : elements) {
            items.add(parseValue(element.trim(), itemType, itemType));
        }
        return items;
    }

    private static Object parseMap(String valueString, java.lang.reflect.Type genericType) throws Exception {
        valueString = valueString.substring(1, valueString.length() - 1); // Убираем { и }
        Map<Object, Object> map = new LinkedHashMap<>();

        String[] entries = valueString.split(",(?![^\\{\\[]*[\\}\\]])");
        Class<?> keyType = (Class<?>) ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments()[0];
        Class<?> valueType = (Class<?>) ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments()[1];

        for (String entry : entries) {
            String[] keyValue = entry.split(":");
            Object key = parseValue(keyValue[0].trim(), keyType, keyType);
            Object value = parseValue(keyValue[1].trim(), valueType, valueType);
            map.put(key, value);
        }

        return map;
    }
}

