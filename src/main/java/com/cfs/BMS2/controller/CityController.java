package com.cfs.BMS2.controller;


import com.cfs.BMS2.entity.City;
import com.cfs.BMS2.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<City> addCity(@RequestBody City city)
    {
        return ResponseEntity.ok(cityService.addCity(city));
    }

    @GetMapping
    public ResponseEntity<List<City>> getAllCities()
    {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id)
    {
        return ResponseEntity.ok(cityService.getCityById(id));
    }
}