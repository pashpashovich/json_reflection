package ru.clevertec.jsonLib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;
import ru.clevertec.util.CustomOffsetDateTimeDeserializer;
import ru.clevertec.util.Reader;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ParserTest {

    ObjectMapper objectMapper;
    JsonConverter parser = new JsonLib();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldParseJsonIntoCustomer() throws Exception {
        // given
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(module);
        String json = Reader.readFileAsString("src/test/resources/customer.json");
        Customer actualCustomer = parser.deserialize(json, Customer.class);
        //when
        Customer expectedCustomer = objectMapper.readValue(json, Customer.class);
        //then
        assertThat(actualCustomer).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedCustomer);
    }

    @Test
    void shouldParseJsonIntoOrder() throws Exception {
        // given
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(module);
        String json = Reader.readFileAsString("src/test/resources/order.json");
        Order actualOrder = parser.deserialize(json, Order.class);
        //when
        Order expectedOrder = objectMapper.readValue(json, Order.class);
        //then
        assertThat(actualOrder).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedOrder);
    }

    @Test
    void shouldParseJsonIntoProduct() throws Exception {
        // given
        String json = Reader.readFileAsString("src/test/resources/product.json");
        Product actualProduct = parser.deserialize(json, Product.class);
        //when
        Product expectedProduct = objectMapper.readValue(json, Product.class);
        //then
        assertThat(actualProduct).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedProduct);
    }

}