package com.example.cardealership.repository;

import com.example.cardealership.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository <Category, Long> {}