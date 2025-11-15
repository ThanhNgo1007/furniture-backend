package com.furniture.controller;

import com.furniture.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ApiResponse HomeControllerHandle(){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Hello World, Welcome to Furniture Selling Platform");
        return apiResponse;
    }
}
