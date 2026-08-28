package com.cfs.BMS2.service;


import com.cfs.BMS2.dto.ShowRequest;
import com.cfs.BMS2.entity.Movie;
import com.cfs.BMS2.entity.Screen;
import com.cfs.BMS2.entity.Show;
import com.cfs.BMS2.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieService movieService;
    private final ScreenService screenService;

    //addshow
    @CacheEvict(value = {"shows", "show", "showsByMovie", "showsByMovieAndDate"}, allEntries = true)
    public Show addShow(ShowRequest request)
    {
        Movie movie =movieService.getMovieById(request.getMovieId());
        Screen screen=screenService.getScreenById(request.getScreenId());
        Show show=Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .ticketPrice(request.getTicketPrice())
                .build();

        return showRepository.save(show);
    }

    @Cacheable("shows")
    public List<Show> getAllShow()
    {
        return showRepository.findAll();
    }

    @Cacheable(value = "show", key = "#id")
    public Show getShowById(Long id)
    {
        return showRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Show not found with id: "+id));

    }

    public Show getShowByIdForUpdate(Long id)
    {
        return showRepository.findByIdForUpdate(id)
                .orElseThrow(()->new RuntimeException("Show not found with id: "+id));
    }

    @Cacheable(value = "showsByMovie", key = "#movieId")
    public List<Show> getShowByMovie(Long movieId)
    {
        return showRepository.findByMovieId(movieId);
    }

    @Cacheable(value = "showsByMovieAndDate", key = "#movieId + '-' + #date")
    public List<Show> getShowByMovieAndDate(Long movieId, LocalDate date)
    {
        return showRepository.findByMovieIdAndShowDate(movieId,date);
    }

    @CacheEvict(value = {"shows", "show", "showsByMovie", "showsByMovieAndDate"}, allEntries = true)
    public Show updateShow(Long id, ShowRequest request)
    {
        Show show = getShowById(id);
        Movie movie = movieService.getMovieById(request.getMovieId());
        Screen screen = screenService.getScreenById(request.getScreenId());
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setTicketPrice(request.getTicketPrice());
        return showRepository.save(show);
    }

    @CacheEvict(value = {"shows", "show", "showsByMovie", "showsByMovieAndDate"}, allEntries = true)
    public void deleteShow(Long id)
    {
        if (!showRepository.existsById(id)) {
            throw new RuntimeException("Show not found with id: " + id);
        }
        showRepository.deleteById(id);
    }

    //getShowByScreen

}