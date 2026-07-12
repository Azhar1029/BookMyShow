package com.cfs.BMS2.controller;


import com.cfs.BMS2.dto.ScreenRequest;
import com.cfs.BMS2.entity.Screen;
import com.cfs.BMS2.entity.Seat;
import com.cfs.BMS2.service.ScreenService;
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
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping
    public ResponseEntity<Screen> addScreen(@RequestBody ScreenRequest request)
    {
        return ResponseEntity.ok(screenService.addScreen(request));
    }

    @GetMapping
    public ResponseEntity<List<Screen>> getAllScreens()
    {
        return ResponseEntity.ok(screenService.getAllScreen());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreenById(@PathVariable Long id)
    {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Screen>>  getScreenByTheaterId(@PathVariable Long theaterId)
    {
        return ResponseEntity.ok(screenService.getScreenByTheater(theaterId));
    }
}