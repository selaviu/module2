package com.example.task2.controller;

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

import com.example.task2.dto.AlbumInfoDto;
import com.example.task2.dto.RestResponse;
import com.example.task2.dto.SaveAlbumDto;
import com.example.task2.service.AlbumService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/{id}")
    public AlbumInfoDto getAlbumById(@PathVariable UUID id){
        return albumService.getAlbumDetails(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createAlbum(@Valid @RequestBody SaveAlbumDto saveAlbumDto){
        UUID id =  albumService.save(saveAlbumDto);
        return new RestResponse(id.toString());
    }

    @PutMapping("/{id}")
    public RestResponse updateAlbum(@PathVariable UUID id, @Valid @RequestBody SaveAlbumDto saveAlbumDto){
        albumService.update(id, saveAlbumDto);
        return new RestResponse(id.toString());
    }   

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlbum(@PathVariable UUID id){
        albumService.delete(id);
    }

}
