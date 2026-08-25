package com.dev.E_commerce.Mini.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_name", columnList = "name")
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    List<Product> products;
}
