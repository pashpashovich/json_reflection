package ru.clevertec.jsonLib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class JsonLib implements JsonConverter {

    private static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX");
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String serialize(Object obj) throws IllegalAccessException {
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

    private String serializeValue(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String || obj instanceof UUID) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof OffsetDateTime dateTime) {
            return "\"" + dateTime.format(OFFSET_DATE_TIME_FORMATTER) + "\"";
        }
        if (obj instanceof LocalDate date) {
            return "\"" + date.format(LOCAL_DATE_FORMATTER) + "\"";
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

    private String serializeCollection(Collection<?> collection) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        List<String> elements = new ArrayList<>();
        for (Object element : collection) {
            elements.add(serializeValue(element));
        }
        json.append(String.join(",\n", elements));
        json.append("\n]");
        return json.toString();
    }

    private String serializeMap(Map<?, ?> map) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        List<String> elements = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            elements.add("\"" + entry.getKey().toString() + "\":" + serializeValue(entry.getValue()));
        }
        json.append(String.join(",", elements));
        json.append("\n}");
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

    public <T> T deserialize(String jsonString, Class<T> clazz) throws Exception {
        Map<String, Object> jsonMap = parseJson(jsonString);
        return deserializeObject(jsonMap, clazz);
    }

    private static Map<String, Object> parseJson(String jsonString) {
        String trimmed = jsonString.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return extractJsonMap(trimmed);
    }

    private static Map<String, Object> extractJsonMap(String jsonString) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false, parsingKey = true;
        int braceCount = 0;

        for (char currentChar : jsonString.toCharArray()) {
            if (currentChar == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes) {
                braceCount = updateBraceCount(currentChar, braceCount);
                if (shouldSwitchToValue(currentChar, parsingKey, braceCount)) {
                    parsingKey = false;
                    continue;
                }
                if (shouldAddToMap(currentChar, braceCount)) {
                    jsonMap.put(currentKey.toString().trim(), parseJsonValue(currentValue.toString().trim()));
                    resetBuffers(currentKey, currentValue);
                    parsingKey = true;
                    continue;
                }
            }
            appendChar(parsingKey ? currentKey : currentValue, currentChar);
        }

        if (currentKey.length() > 0) {
            jsonMap.put(currentKey.toString().trim(), parseJsonValue(currentValue.toString().trim()));
        }

        return jsonMap;
    }

    private static int updateBraceCount(char currentChar, int braceCount) {
        if (currentChar == '{' || currentChar == '[') {
            braceCount++;
        } else if (currentChar == '}' || currentChar == ']') {
            braceCount--;
        }
        return braceCount;
    }

    private static boolean shouldSwitchToValue(char currentChar, boolean parsingKey, int braceCount) {
        return currentChar == ':' && parsingKey && braceCount == 0;
    }

    private static boolean shouldAddToMap(char currentChar, int braceCount) {
        return currentChar == ',' && braceCount == 0;
    }

    private static void appendChar(StringBuilder buffer, char currentChar) {
        buffer.append(currentChar);
    }

    private static void resetBuffers(StringBuilder currentKey, StringBuilder currentValue) {
        currentKey.setLength(0);
        currentValue.setLength(0);
    }

    private static <T> T deserializeObject(Map<String, Object> jsonMap, Class<T> clazz) throws Exception {
        T object = clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                if (Modifier.isPrivate(field.getModifiers())) field.setAccessible(true);
                processField(field, jsonMap, object);
            }
        }
        return object;
    }

    private static void processField(Field field, Map<String, Object> jsonMap, Object object) throws Exception {
        String name = field.getName();
        if (jsonMap.containsKey(name)) {
            Object value = jsonMap.get(name);
            value = convertIfNecessary(value, field.getType());

            if (isTypePrimitive(field.getType())) {
                field.set(object, convertToType(value, field.getType()));
            } else if (field.getType() == List.class) {
                setListField(field, object, value);
            } else if (field.getType() == Set.class) {
                setSetField(field, object, value);
            } else if (field.getType() == Map.class) {
                setMapField(field, object, value);
            } else if (field.getType().isArray()) {
                setArrayField(field, object, value);
            } else {
                field.set(object, deserializeComplexField(value, field.getType()));
            }
        }
    }

    private static Object convertIfNecessary(Object value, Class<?> fieldType) {
        if (value instanceof String && !fieldType.equals(String.class)) {
            return convertStringToType((String) value, fieldType);
        }
        return value;
    }

    private static void setListField(Field field, Object object, Object value) throws Exception {
        Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        field.set(object, deserializeList((List<?>) value, (Class<?>) genericType));
    }

    private static void setSetField(Field field, Object object, Object value) throws Exception {
        Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        field.set(object, new LinkedHashSet<>(deserializeList((List<?>) value, (Class<?>) genericType)));
    }

    private static void setMapField(Field field, Object object, Object value) throws Exception {
        Type[] genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        field.set(object, deserializeMap((Map<?, ?>) value, (Class<?>) genericTypes[0], (Class<?>) genericTypes[1]));
    }

    private static void setArrayField(Field field, Object object, Object value) throws Exception {
        Class<?> componentType = field.getType().getComponentType();
        field.set(object, deserializeArray((List<?>) value, componentType));
    }

    private static Object deserializeComplexField(Object value, Class<?> fieldType) throws Exception {
        if (value instanceof Map) {
            return deserializeObject((Map<String, Object>) value, fieldType);
        }
        return value;
    }

    private static Object convertStringToType(String value, Class<?> fieldType) {
        value = value.replace("{", "").replace("}", "");
        if (fieldType == UUID.class) {
            return UUID.fromString(value);
        } else if (fieldType == LocalDate.class) {
            return LocalDate.parse(value);
        } else if (fieldType == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        } else if (fieldType == OffsetDateTime.class) {
            return OffsetDateTime.parse(value);
        } else if (Enum.class.isAssignableFrom(fieldType)) {
            return Enum.valueOf((Class<Enum>) fieldType, value);
        }
        throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
    }

    private static <T> T[] deserializeArray(List<?> jsonArray, Class<T> componentType) throws Exception {
        T[] array = (T[]) Array.newInstance(componentType, jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            array[i] = isTypePrimitive(componentType) ? convertToType(jsonArray.get(i), componentType)
                    : deserializeObject((Map<String, Object>) jsonArray.get(i), componentType);
        }
        return array;
    }

    private static <K, V> Map<K, V> deserializeMap(Map<?, ?> jsonMap, Class<K> keyClass, Class<V> valueClass) throws Exception {
        Map<K, V> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : jsonMap.entrySet()) {
            K key = convertToKey(entry.getKey(), keyClass);
            V value = convertToValue(entry.getValue(), valueClass);
            map.put(key, value);
        }
        return map;
    }

    private static <K> K convertToKey(Object keyObject, Class<K> keyClass) {
        if (keyClass == UUID.class) {
            return keyClass.cast(UUID.fromString(keyObject.toString().replace("\"", "")));
        }
        return convertToType(keyObject, keyClass);
    }

    private static <V> V convertToValue(Object valueObject, Class<V> valueClass) throws Exception {
        if (valueClass == BigDecimal.class) {
            return (V) new BigDecimal(valueObject.toString());
        } else if (isTypePrimitive(valueClass)) {
            return convertToType(valueObject, valueClass);
        } else if (valueObject instanceof Map) {
            return deserializeObject((Map<String, Object>) valueObject, valueClass);
        }
        return (V) valueObject;
    }

    private static boolean isTypePrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == Integer.class || clazz == Long.class ||
                clazz == Boolean.class || clazz == Double.class || clazz == Float.class ||
                clazz == Character.class || clazz == Byte.class || clazz == Short.class;
    }

    private static <T> List<T> deserializeList(List<?> jsonArray, Class<?> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        for (Object element : jsonArray) {
            list.add(element instanceof Map ? (T) deserializeObject((Map<String, Object>) element, clazz)
                    : convertToType(element, clazz));
        }
        return list;
    }

    private static <T> T convertToType(Object value, Class<?> clazz) {
        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(value.toString());
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(value.toString());
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(value.toString());
        } else if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(value.toString());
        } else if (clazz == Character.class || clazz == char.class) {
            return (T) Character.valueOf(value.toString().charAt(0));
        } else if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(value.toString());
        } else if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf(value.toString());
        } else {
            return (T) value;
        }
    }

    private static Object parseJsonValue(String value) {
        if ("null".equals(value)) {
            return null;
        } else if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        } else if (value.matches("-?\\d+(\\.\\d+)?")) {
            return value.contains(".") ? Double.parseDouble(value) : Long.parseLong(value);
        } else if (value.startsWith("{") && value.endsWith("}")) {
            return parseJson(value);
        } else if (value.startsWith("[") && value.endsWith("]")) {
            return parseJsonArray(value);
        } else {
            return value;
        }
    }

    private static List<Object> parseJsonArray(String jsonArrayString) {
        String trimmed = jsonArrayString.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        List<Object> jsonArray = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        int braceCount = 0;

        for (char currentChar : trimmed.toCharArray()) {
            if (currentChar == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes) {
                braceCount = updateBraceCount(currentChar, braceCount);
                if (currentChar == ',' && braceCount == 0) {
                    jsonArray.add(parseJsonValue(currentValue.toString().trim()));
                    currentValue.setLength(0);
                    continue;
                }
            }
            currentValue.append(currentChar);
        }

        if (currentValue.length() > 0) {
            jsonArray.add(parseJsonValue(currentValue.toString().trim()));
        }

        return jsonArray;
    }
}
