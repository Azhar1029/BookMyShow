package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.City;
import com.cfs.BMS2.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {



}
