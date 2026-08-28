package com.cfs.BMS2.service;

import com.cfs.BMS2.entity.City;
import com.cfs.BMS2.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @CacheEvict(value = {"cities", "city"}, allEntries = true)
    public City addCity(City city)
    {
        return cityRepository.save(city);
    }

    @Cacheable("cities")
    public List<City> getAllCities()
    {
        return cityRepository.findAll();
    }

    @Cacheable(value = "city", key = "#id")
    public City getCityById(Long id)
    {
        return cityRepository.findById(id)
                .orElseThrow(()->new RuntimeException("City not found with id: "+id));
    }
}