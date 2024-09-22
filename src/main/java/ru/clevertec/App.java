package ru.clevertec;

import ru.clevertec.jsonLib.JsonConverter;
import ru.clevertec.jsonLib.JsonLib;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        Product product = new Product(UUID.randomUUID(), "hako", 23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123), UUID.randomUUID(), BigDecimal.valueOf(435)));
        Order order = new Order(UUID.randomUUID(), List.of(product), OffsetDateTime.now());
        Customer customer = new Customer(UUID.randomUUID(), "Pasha", "Kosovich", LocalDate.ofYearDay(2004, 204), List.of(order));
        JsonConverter parser = new JsonLib();
        try {
            System.out.println("Json for Order:");
            System.out.println(parser.serialize(order));
            System.out.println("Json for Product:");
            System.out.println(parser.serialize(product));
            System.out.println("Json for Customer:");
            System.out.println(parser.serialize(customer));
            String json1 = "{\"id\":\"a5d1c04a-eee3-43e5-a7ca-9cb6d7c47617\",\n" +
                    "\"products\":[\n" +
                    "{\"id\":\"989be0a6-a122-4d38-ae80-9d6f8bbc8ebe\",\n" +
                    "\"name\":\"hako\",\n" +
                    "\"price\":23.6,\n" +
                    "\"count\":{\n" +
                    "\"5aea2ea3-5ea4-4219-9001-03561c2edb07\":435,\"0821b35d-8c1a-4be7-988f-69b52c605427\":123\n" +
                    "}}\n" +
                    "],\n" +
                    "\"createDate\":\"2024-09-22T11:10:46.8566492+03:00\"}";
            String json2 = "{\"id\":\"82311989-678c-4882-a7e5-2f7d964856ec\",\n" +
                    "\"name\":\"hako\",\n" +
                    "\"price\":23.6,\n" +
                    "\"count\":{\n" +
                    "\"242c464d-8d47-4b72-aa91-f1732ab22a29\":123,\"67a3541c-5b38-4aa5-bc88-66480ea5e60a\":435\n" +
                    "}}";
            String json3 = "{\"id\":\"fc5102b7-7d73-45bb-bd8c-e8eb79f4ace6\",\n" +
                    "\"firstName\":\"Pasha\",\n" +
                    "\"lastName\":\"Kosovich\",\n" +
                    "\"dateBirth\":\"2004-07-22\",\n" +
                    "\"orders\":[\n" +
                    "{\"id\":\"14db0c63-b35f-42e4-a206-c9842488db8d\",\n" +
                    "\"products\":[\n" +
                    "{\"id\":\"47358a5d-e5ab-44c1-babc-b72908b35b6e\",\n" +
                    "\"name\":\"hako\",\n" +
                    "\"price\":23.6,\n" +
                    "\"count\":{\n" +
                    "\"526792ed-c6f0-46f8-9d3d-9cdef2db10c9\":435,\"16bbc891-5b5d-4665-afdc-bba1d4fee5cc\":123\n" +
                    "}}\n" +
                    "],\n" +
                    "\"createDate\":\"2024-09-22T11:12:49.2995607+03:00\"}\n" +
                    "]}";
            Order order1 = parser.deserialize(json1, Order.class);
            Product product1 = parser.deserialize(json2, Product.class);
            Customer customer1 = parser.deserialize(json3, Customer.class);
            System.out.println("Order:");
            System.out.println(order1);
            System.out.println("Product:");
            System.out.println(product1);
            System.out.println("Customer:");
            System.out.println(customer1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}