package com.example.task2.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.task2.dto.GenreInfoDto;
import com.example.task2.dto.RestResponse;
import com.example.task2.dto.SaveGenreDto;
import com.example.task2.service.GenreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/genre")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public List<GenreInfoDto> getAllGenres(){
        return genreService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createGenres(@Valid @RequestBody SaveGenreDto saveGenreDto){
        UUID id = genreService.save(saveGenreDto);
        return new RestResponse(id.toString());
    }

    @PutMapping("/{id}")
    public RestResponse createGenres(@PathVariable UUID id, @Valid @RequestBody SaveGenreDto saveGenreDto){
        genreService.update(id, saveGenreDto);
        return new RestResponse(id.toString());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenre(@PathVariable UUID id){
        genreService.delete(id);
    }
}
