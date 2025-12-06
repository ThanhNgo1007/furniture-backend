package com.furniture.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.domain.HomeCategorySection;
import com.furniture.modal.HomeCategory;
import com.furniture.repository.HomeCategoryRepository;
import com.furniture.service.HomeCategoryService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeCategoryServiceImpl implements HomeCategoryService {

    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public HomeCategory createHomeCategory(@NonNull HomeCategory homeCategory) {
        // Set default values if not provided
        if(homeCategory.getIsActive() == null) {
            homeCategory.setIsActive(true);
        }
        if(homeCategory.getDisplayOrder() == null) {
            homeCategory.setDisplayOrder(0);
        }
        return homeCategoryRepository.save(homeCategory);
    }

    @Override
    public List<HomeCategory> createCategories(@NonNull List<HomeCategory> homeCategories) {
        if(homeCategoryRepository.findAll().isEmpty()){
            // Set default values for each category
            for(HomeCategory category : homeCategories) {
                if(category.getIsActive() == null) {
                    category.setIsActive(true);
                }
                if(category.getDisplayOrder() == null) {
                    category.setDisplayOrder(0);
                }
            }
            return homeCategoryRepository.saveAll(homeCategories);
        }
        return homeCategoryRepository.findAll();
    }

    @Override
    public HomeCategory updateHomeCategory(@NonNull HomeCategory category, @NonNull Long id) throws Exception {
        HomeCategory existingCategory = homeCategoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Home Category not found"));

        if(category.getImage() != null){
            existingCategory.setImage(category.getImage());
        }
        if(category.getCategoryId() != null){
            existingCategory.setCategoryId(category.getCategoryId());
        }
        if(category.getName() != null){
            existingCategory.setName(category.getName());
        }
        if(category.getSection() != null){
            existingCategory.setSection(category.getSection());
        }
        if(category.getDisplayOrder() != null){
            existingCategory.setDisplayOrder(category.getDisplayOrder());
        }
        if(category.getIsActive() != null){
            existingCategory.setIsActive(category.getIsActive());
        }
        if (existingCategory != null) {
            return homeCategoryRepository.save(existingCategory);
        }
        throw new Exception("Home Category not found");
    }

    @Override
    public List<HomeCategory> getAllHomeCategories() {
        return homeCategoryRepository.findAll();
    }

    @Override
    public List<HomeCategory> getHomeCategoriesBySection(HomeCategorySection section) {
        return homeCategoryRepository.findBySectionAndIsActiveOrderByDisplayOrder(section, true);
    }

    @Override
    public List<HomeCategory> getAllActiveCategories() {
        return homeCategoryRepository.findByIsActiveOrderByDisplayOrder(true);
    }

    @Override
    public void deleteHomeCategory(@NonNull Long id) throws Exception {
        HomeCategory category = homeCategoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Home Category not found"));
        if (category != null) {
            homeCategoryRepository.delete(category);
        }
    }
}
