package ru.clevertec.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Order {

    private UUID id;
    private List<Product> products;
    private OffsetDateTime createDate;
}
