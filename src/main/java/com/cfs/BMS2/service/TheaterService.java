package com.cfs.BMS2.service;


import com.cfs.BMS2.dto.TheaterRequest;
import com.cfs.BMS2.entity.City;
import com.cfs.BMS2.entity.Theater;
import com.cfs.BMS2.repository.CityRepository;
import com.cfs.BMS2.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.CharacterIterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityService cityService;


    @CacheEvict(value = {"theaters", "theater", "theatersByCity"}, allEntries = true)
    public Theater addTheater(TheaterRequest request)
    {
        City city=cityService.getCityById(request.getCityId());
        Theater theater=Theater.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(city)
                .build();
        return theaterRepository.save(theater);
    }

    @Cacheable("theaters")
    public List<Theater> getAllTheaters()
    {
        return theaterRepository.findAll();
    }

    @Cacheable(value = "theater", key = "#id")
    public Theater getTheaterById(Long id)
    {
        return theaterRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Theater not found with id: "+id));

    }

    @Cacheable(value = "theatersByCity", key = "#cityId")
    public List<Theater> getTheaterByCity(Long cityId)
    {
        return theaterRepository.findByCityId(cityId);
    }
}