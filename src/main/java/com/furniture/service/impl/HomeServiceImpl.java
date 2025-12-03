package com.furniture.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.furniture.domain.HomeCategorySection;
import com.furniture.modal.Deal;
import com.furniture.modal.Home;
import com.furniture.modal.HomeCategory;
import com.furniture.repository.DealRepository;
import com.furniture.service.HomeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;

    @Override
    public Home createHomePageData(List<HomeCategory> allCategories) {

        List<HomeCategory> gridCategories = allCategories.stream()
                .filter(category -> category.getSection() == HomeCategorySection.GRID)
                .collect(Collectors.toList());

        List<HomeCategory> bestSeller = allCategories.stream()
                .filter(category -> category.getSection() == HomeCategorySection.BEST_SELLER)
                .collect(Collectors.toList());

        // Just fetch existing deals from repository
        // Deals are now managed through admin interface with product categories
        List<Deal> createdDeals = dealRepository.findAll();

        Home home = new Home();
        home.setGrid(gridCategories);
        home.setBestSeller(bestSeller);
        home.setDeals(createdDeals);

        return home;

    }
}

