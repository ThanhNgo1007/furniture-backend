package com.furniture.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.domain.HomeCategorySection;
import com.furniture.modal.Home;
import com.furniture.modal.HomeCategory;
import com.furniture.service.HomeCategoryService;
import com.furniture.service.HomeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HomeCategoryController {

    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;

    // ========== PUBLIC ENDPOINTS ==========
    
    @GetMapping("/api/home-categories")
    public ResponseEntity<List<HomeCategory>> getActiveHomeCategories() {
        List<HomeCategory> categories = homeCategoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/api/home-categories/section/{section}")
    public ResponseEntity<List<HomeCategory>> getHomeCategoriesBySection(
            @PathVariable HomeCategorySection section
    ) {
        List<HomeCategory> categories = homeCategoryService.getHomeCategoriesBySection(section);
        return ResponseEntity.ok(categories);
    }

    // ========== ADMIN ENDPOINTS ==========

    @PostMapping("/admin/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories
    ) {
        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategories);
        Home home = homeService.createHomePageData(categories);

        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }

    @GetMapping("/admin/home-category")
    public ResponseEntity<List<HomeCategory>> getAllHomeCategoriesAdmin() {
        List<HomeCategory> categories = homeCategoryService.getAllHomeCategories();
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/admin/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory category
    ) throws Exception {
        HomeCategory updatedCategory = homeCategoryService.updateHomeCategory(category, id);
        return ResponseEntity.ok(updatedCategory);
    }

    @PostMapping("/admin/home-category")
    public ResponseEntity<HomeCategory> createHomeCategory(
            @RequestBody HomeCategory homeCategory
    ) {
        HomeCategory category = homeCategoryService.createHomeCategory(homeCategory);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/home-category/{id}")
    public ResponseEntity<Void> deleteHomeCategory(@PathVariable Long id) throws Exception {
        homeCategoryService.deleteHomeCategory(id);
        return ResponseEntity.ok().build();
    }
}
