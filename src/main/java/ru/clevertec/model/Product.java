package ru.clevertec.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Product {

    private UUID id;
    private String name;
    private Double price;
    private Map<UUID, BigDecimal> count;
}
