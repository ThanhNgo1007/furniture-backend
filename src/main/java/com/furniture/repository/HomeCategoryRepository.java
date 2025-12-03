package com.furniture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.domain.HomeCategorySection;
import com.furniture.modal.HomeCategory;

public interface HomeCategoryRepository extends JpaRepository<HomeCategory, Long> {
    List<HomeCategory> findBySectionAndIsActiveOrderByDisplayOrder(HomeCategorySection section, Boolean isActive);
    List<HomeCategory> findBySection(HomeCategorySection section);
    List<HomeCategory> findByIsActiveOrderByDisplayOrder(Boolean isActive);
}
