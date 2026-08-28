package com.cfs.BMS2.service;


import com.cfs.BMS2.entity.Movie;
import com.cfs.BMS2.entity.Theater;
import com.cfs.BMS2.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie addMove(Movie movie)
    {
        return movieRepository.save(movie);
    }

    @Cacheable("movies")
    public List<Movie> getAllMovies()
    {
        return movieRepository.findAll();
    }

    @Cacheable(value = "movie", key = "#id")
    public Movie getMovieById(Long id)
    {
        return movieRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Movie not found with id: "+id));

    }

    public List<Movie> searchByTitle(String title){
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> getByGenre(String genre){
        return movieRepository.findByGenre(genre);
    }

    public List<Movie> getByLanguage(String language){
        return movieRepository.findByLanguage(language);
    }

    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public Movie updateMovie(Long id, Movie updated) {
        Movie movie = getMovieById(id);
        movie.setTitle(updated.getTitle());
        movie.setDescription(updated.getDescription());
        movie.setGenre(updated.getGenre());
        movie.setLanguage(updated.getLanguage());
        movie.setDurationMinutes(updated.getDurationMinutes());
        movie.setRating(updated.getRating());
        movie.setReleaseDate(updated.getReleaseDate());
        movie.setPosterUrl(updated.getPosterUrl());
        return movieRepository.save(movie);
    }

    @CacheEvict(value = {"movies", "movie"}, allEntries = true)
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

}