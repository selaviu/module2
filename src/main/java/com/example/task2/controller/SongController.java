package com.example.task2.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.task2.dto.FileUploadResultDto;
import com.example.task2.dto.RestResponse;
import com.example.task2.dto.SaveSongDto;
import com.example.task2.dto.SongFilter;
import com.example.task2.dto.SongInfoDto;
import com.example.task2.service.SongService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/song")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createSong(@Valid @RequestBody SaveSongDto saveSongDto){
        UUID id = songService.saveSong(saveSongDto);
        return new RestResponse(id.toString());
    }

    @GetMapping("/{id}")
    public SongInfoDto getSong(@PathVariable UUID id){
        return songService.getSongDetailedInfo(id);
    }

    @PutMapping("/{id}")
    public RestResponse updateSong(@PathVariable UUID id, @Valid @RequestBody SaveSongDto saveSongDto){
        UUID songId = songService.updateSong(id, saveSongDto);
        return new RestResponse(songId.toString());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSong(@PathVariable UUID id){
        songService.deleteSong(id);
    }

    @PostMapping("/_list")
    public Page<SongInfoDto> getSongs(Pageable pageable, @RequestBody SongFilter songFilter){
        
        return songService.getSongs(pageable, songFilter);
    }

    @PostMapping(value = "/_report", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void generateReport(HttpServletResponse httpServletResponse, @RequestBody SongFilter songFilter) throws IOException{
        try{
            songService.generateReport(httpServletResponse, songFilter);
        }catch(IOException e){
            httpServletResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to generate report: " + e.getMessage());
        }
        
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public FileUploadResultDto uploadFromFile(@RequestParam("file") MultipartFile multipart){
        FileUploadResultDto fileUpload = songService.uploadFromFile(multipart);
        return fileUpload;
    }

}
