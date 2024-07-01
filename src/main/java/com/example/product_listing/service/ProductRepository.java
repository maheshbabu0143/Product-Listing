package com.example.product_listing.service;

import com.example.product_listing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository  extends JpaRepository<Product , Integer> {
}
