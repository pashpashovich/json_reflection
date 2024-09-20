package ru.clevertec;

import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;
import ru.clevertec.parsing.Parser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        Product product = new Product(UUID.randomUUID(),"hako",23.6, Map.of(UUID.randomUUID(), BigDecimal.valueOf(123),UUID.randomUUID(),BigDecimal.valueOf(435)));
        Order order = new Order(UUID.randomUUID(), List.of(product), OffsetDateTime. now());
        Customer customer = new Customer(UUID.randomUUID(),"Pasha","Kosovich", LocalDate.ofYearDay(2004,203),List.of(order));
        try {
            System.out.println(Parser.serialize(order));
            System.out.println(Parser.serialize(product));
            System.out.println(Parser.serialize(customer));
            String json = "{\"id\" : \"b7048c52-2c2d-424a-8f2f-6b78f66cc970\",\n" +
                    "\"products\" : [{\"id\" : \"902bf7ae-5898-4ed1-8e4b-80d311a8ca99\",\n" +
                    "\"name\" : \"hako\",\n" +
                    "\"price\" : 23.6,\n" +
                    "\"count\" : {\"8c3c723b-4038-460d-84e5-8b1ee2638de7\":123,\"a8c813e6-7ca6-4138-83e6-0beed418c34e\":435}}],\n" +
                    "\"createDate\" : \"2024-09-20T22:06:34.169835+03:00\"}";
              Order order1= Parser.deserialize(json, Order.class);
            System.out.println(order1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}