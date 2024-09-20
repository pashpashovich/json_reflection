package ru.clevertec.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;
import ru.clevertec.util.OffsetDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {


    @Test
    void shouldSerializeDataWhichIncludesListAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer()));
        Product product1 = new Product(UUID.randomUUID(), "eggs", 23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123), UUID.randomUUID(), BigDecimal.valueOf(435)));
        Product product2 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        Order order = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.now());
        String expectedJson = objectMapper.writeValueAsString(order);
        String actualJson = Parser.serialize(order);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //when,then
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void shouldSerializeDataWhichIncludesMapAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        Product product1 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        String expectedJson = objectMapper.writeValueAsString(product1);
        String actualJson = Parser.serialize(product1);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //when,then
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void shouldSerializeDataWhichIncludesLocalDateMapAsJacksonDo() throws JsonProcessingException, IllegalAccessException {
        //// given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        Product product1 = new Product(UUID.randomUUID(), "eggs", 23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123), UUID.randomUUID(), BigDecimal.valueOf(435)));
        Product product2 = new Product(UUID.randomUUID(), "meat", 40.3, Map.of(UUID.randomUUID(), BigDecimal.valueOf(765), UUID.randomUUID(), BigDecimal.valueOf(987)));
        Order order1 = new Order(UUID.randomUUID(), List.of(product1, product2), OffsetDateTime.now());
        Order order2 = new Order(UUID.randomUUID(), List.of(product1), OffsetDateTime.now());
        Customer customer = new Customer(UUID.randomUUID(),"Pasha","Kosovich", LocalDate.ofYearDay(2004,221),List.of(order1,order2));
        String expectedJson = objectMapper.writeValueAsString(customer);
        String actualJson = Parser.serialize(customer);
        actualJson = actualJson.replace("\n", "").replace("\r", "");
        //when,then
        expectedJson = expectedJson.replaceAll("\\.([0-9]{6})[0-9]+", ".$1");
        actualJson = actualJson.replaceAll("\\.([0-9]{6})[0-9]+", ".$1");
        assertEquals(expectedJson, actualJson);
    }

    @Test
    void deserialize() {
    }
}