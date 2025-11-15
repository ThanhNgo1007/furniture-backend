package com.furniture.controller;

import com.furniture.modal.Home;
import com.furniture.modal.HomeCategory;
import com.furniture.repository.HomeCategoryRepository;
import com.furniture.service.HomeCategoryService;
import com.furniture.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeCategoryController {

    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;

    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(
            @RequestBody List<HomeCategory> homeCategories
    ) {
        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategories);
        Home home = homeService.createHomePageData(categories);

        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }

    @GetMapping("/admin/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory(
    ) {

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

}
