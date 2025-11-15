package com.furniture.service;

import com.furniture.modal.Home;
import com.furniture.modal.HomeCategory;

import java.util.List;

public interface HomeService {

    public Home createHomePageData(List<HomeCategory> allCategories);
}
