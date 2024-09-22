package ru.clevertec.jsonLib;

public interface JsonConverter {
    String serialize(Object obj) throws IllegalAccessException;

    <T> T deserialize(String jsonString, Class<T> clazz) throws Exception;
}
