package com.furniture.service;

import java.util.List;

import com.furniture.domain.HomeCategorySection;
import com.furniture.modal.HomeCategory;

public interface HomeCategoryService {

    HomeCategory createHomeCategory(HomeCategory homeCategory);
    List<HomeCategory> createCategories(List<HomeCategory> homeCategories);
    HomeCategory updateHomeCategory(HomeCategory category, Long id) throws Exception;
    List<HomeCategory> getAllHomeCategories();
    List<HomeCategory> getHomeCategoriesBySection(HomeCategorySection section);
    List<HomeCategory> getAllActiveCategories();
    void deleteHomeCategory(Long id) throws Exception;
}
