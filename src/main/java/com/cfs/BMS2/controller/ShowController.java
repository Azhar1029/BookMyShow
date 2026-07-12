package com.cfs.BMS2.controller;


import com.cfs.BMS2.dto.ShowRequest;
import com.cfs.BMS2.entity.Show;
import com.cfs.BMS2.entity.Theater;
import com.cfs.BMS2.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<Show> addShow(@RequestBody ShowRequest request)
    {
        return ResponseEntity.ok(showService.addShow(request));
    }

    @GetMapping
    public ResponseEntity<List<Show>> getAllShows()
    {
        return ResponseEntity.ok(showService.getAllShow());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Show> getShowById(@PathVariable Long id)
    {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getshowByMovie(@PathVariable Long movieId)
    {
        return ResponseEntity.ok(showService.getShowByMovie(movieId));
    }
    @GetMapping("/movie/{movieId}/date")
    public ResponseEntity<List<Show>>
    getShowByMovieAndDate(@PathVariable Long movieId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        return ResponseEntity.ok(showService.getShowByMovieAndDate(movieId,date));
    }
}