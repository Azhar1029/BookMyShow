package com.cfs.BMS2.repository;

import com.cfs.BMS2.entity.Movie;
import com.cfs.BMS2.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.*;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByGenre(String genre);
    List<Movie> findByLanguage(String language);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
