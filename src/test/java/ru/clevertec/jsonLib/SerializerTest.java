package ru.clevertec.jsonLib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializerTest {
    ObjectMapper objectMapper;
    JsonConverter serializer = new JsonLib();


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSerializeDataWhichIncludesListAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        // given
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        Product product1 = new Product(UUID.randomUUID(), "eggs", 23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123), UUID.randomUUID(), BigDecimal.valueOf(435)));
        Product product2 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        Order order = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.now());
        //when
        String expectedJson = objectMapper.writeValueAsString(order);
        String actualJson = serializer.serialize(order);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //then
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void shouldSerializeDataWhichIncludesMapAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        // given
        Product product1 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        //when
        String expectedJson = objectMapper.writeValueAsString(product1);
        String actualJson = serializer.serialize(product1);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //then
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void shouldSerializeDataWhichIncludesLocalDateMapAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        // given
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        Product product1 = new Product(UUID.randomUUID(), "eggs", 23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123), UUID.randomUUID(), BigDecimal.valueOf(435)));
        Product product2 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        Order order1 = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.now());
        Order order2 = new Order(UUID.randomUUID(), List.of(product1), OffsetDateTime.now());
        Customer customer = new Customer(UUID.randomUUID(), "Pasha", "Kosovich", LocalDate.ofYearDay(2004, 221), List.of(order1, order2));
        //when
        String expectedJson = objectMapper.writeValueAsString(customer);
        String actualJson = serializer.serialize(customer);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //then
        assertEquals(expectedJson, actualJson);
    }

}