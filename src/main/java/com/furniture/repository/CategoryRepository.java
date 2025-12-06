package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByCategoryId(String categoryId);
}
