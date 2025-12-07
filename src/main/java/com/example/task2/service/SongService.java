package com.example.task2.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.task2.dto.ArtistInfoDto;
import com.example.task2.dto.FileUploadResultDto;
import com.example.task2.dto.GenreInfoDto;
import com.example.task2.dto.SaveSongDto;
import com.example.task2.dto.SongFilter;
import com.example.task2.dto.SongInfoDto;
import com.example.task2.exception.custom.FileUploadProcessingException;
import com.example.task2.exception.custom.InvalidFileFormatException;
import com.example.task2.exception.custom.ResourceNotFoundException;
import com.example.task2.model.Album;
import com.example.task2.model.Song;
import com.example.task2.repository.SongRepository;
import com.example.task2.util.SongSpecification;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService {

    private final GenreService genreService;
    private final SongRepository songRepository;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final Gson gson;

    @Transactional
    public UUID saveSong(SaveSongDto saveSongDto){
        Song song = Song.builder().title(saveSongDto.getTitle())
        .album(getAlbumOrNull(saveSongDto.getAlbumId()))
        .artist(artistService.findById(saveSongDto.getArtistId()))
        .duration(saveSongDto.getDuration())
        .releaseYear(saveSongDto.getReleaseYear())
        .genres(genreService.findAllById(saveSongDto.getGenresId()))
        .build();

        return songRepository.save(song).getId();
    }

    public Song findById(UUID id){
        return songRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Song", id));
    }

    public SongInfoDto getSongDetailedInfo(UUID id){
        Song song = findById(id);
        return convertToDetails(song);
    }

    @Transactional
    public UUID updateSong(UUID id, SaveSongDto saveSongDto){
        Song songToUpdate = findById(id);
        updateFromDto(songToUpdate, saveSongDto);
        return songRepository.save(songToUpdate).getId();
    }

    public void deleteSong(UUID id){
        Song songToDelete = findById(id);
        songRepository.delete(songToDelete);
    }

    public Page<SongInfoDto> getSongs(Pageable pageable, SongFilter songFilter){

        Specification<Song> isAlbum = SongSpecification.isAlbum(songFilter.getAlbumName());
        Specification<Song> releasedInYear = SongSpecification.releasedInYear(songFilter.getReleasedYear());
        Specification<Song> isArtist = SongSpecification.isArist(songFilter.getArtistName());

        Specification<Song> finalSpec = isAlbum.and(releasedInYear).and(isArtist);

        Page<Song> songs = songRepository.findAll(finalSpec, pageable);

        return songs.map(this::convertToDetails);
    }

    public void generateReport(HttpServletResponse response, SongFilter songFilter) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"songs.csv\"");

        try(PrintWriter writer = response.getWriter()){
            writer.println("ID,Title,ArtistName,Album,ReleasedYear,Duration,Genres");

            List<SongInfoDto> songs = getFilteredSongsForReport(songFilter);
            for(SongInfoDto song : songs){
                String row = String.format(Locale.ROOT, "%s,%s,%s,%s,%d,%d,%s",
                song.getId(),
                escapeCsv(song.getTitle()),
                escapeCsv(song.getArtistInfo().getName()),
                escapeCsv(song.getAlbum() != null ? song.getAlbum().getName() : ""),
                song.getReleaseYear(),
                song.getDuration(),
                escapeCsv(convertGenresListToString(song.getGenres())));
                writer.println(row);
            }
        }
    }

    public FileUploadResultDto uploadFromFile(MultipartFile multipartFile) {
        
        if (!multipartFile.getOriginalFilename().endsWith(".json")) {
            throw new InvalidFileFormatException("Only JSON format is supported.");
        }

        int successCount = 0;
        int failureCount = 0;

        try (Reader reader = new InputStreamReader(multipartFile.getInputStream(), "UTF-8");
            JsonReader jsonReader = new JsonReader(reader)) {

            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                
                try {
                    SaveSongDto song = gson.fromJson(jsonReader, SaveSongDto.class);
                    
                    try {
                        saveSong(song); 
                        successCount++;
                    } catch (ResourceNotFoundException | DataIntegrityViolationException businessEx) {
                        failureCount++;
                        System.err.println("Failed to process song: " + businessEx.getMessage());

                    }
                    
                } catch (JsonParseException e) {
                    
                    failureCount++;
                    System.err.println("Parsing error: " + e.getMessage());
                    
                    try {
                        jsonReader.skipValue(); 
                    } catch (IOException ignored) {
                        System.err.println("Failed to skip corrupted element.");
                        break;
                    }
                } 
            }
            jsonReader.endArray();
            
            return new FileUploadResultDto(successCount, failureCount);

        } catch (IOException e) {
            throw new FileUploadProcessingException("File reading error: " + e.getMessage());
        }
    }

    private List<SongInfoDto> getFilteredSongsForReport(SongFilter songFilter){
        Specification<Song> isAlbum = SongSpecification.isAlbum(songFilter.getAlbumName());
        Specification<Song> releasedInYear = SongSpecification.releasedInYear(songFilter.getReleasedYear());
        Specification<Song> isArtist = SongSpecification.isArist(songFilter.getArtistName());

        Specification<Song> finalSpec = isAlbum.and(releasedInYear).and(isArtist);

        List<Song> songs = songRepository.findAll(finalSpec);

        return songs.stream().map(s -> convertToDetails(s)).collect(Collectors.toList());
    }

    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private String convertGenresListToString(List<GenreInfoDto> genres){
        StringBuilder sb =  new StringBuilder();
        String delimiter = ", ";

        for (int i = 0; i < genres.size(); i++) {
            
            if (i > 0) {
                sb.append(delimiter); 
            }
            
            sb.append(genres.get(i).getName());
        }
        
        return sb.toString();
    }

    private SongInfoDto convertToDetails(Song song){
        return SongInfoDto.builder()
        .id(song.getId())
        .title(song.getTitle())
        .album(albumService.convertToDetails(song.getAlbum()))
        .artistInfo(new ArtistInfoDto(song.getArtist().getId(), song.getArtist().getName()))
        .duration(song.getDuration())
        .releaseYear(song.getReleaseYear())
        .genres(song.getGenres().stream().map(genre -> genreService.convertToDetails(genre)).collect(Collectors.toList()))
        .build();
    }

    private Song updateFromDto(Song songToUpdate, SaveSongDto saveSongDto){
        songToUpdate.setAlbum(getAlbumOrNull(saveSongDto.getAlbumId()));
        songToUpdate.setTitle(saveSongDto.getTitle());
        songToUpdate.setDuration(saveSongDto.getDuration());
        songToUpdate.setReleaseYear(saveSongDto.getReleaseYear());
        songToUpdate.setArtist(artistService.findById(saveSongDto.getArtistId()));
        songToUpdate.setGenres(genreService.findAllById(saveSongDto.getGenresId()));

        return songToUpdate;
    }

    private Album getAlbumOrNull(UUID albumId){
        if(albumId == null){
            return null;
        }

        return albumService.findById(albumId);
    }
}
