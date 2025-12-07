package com.example.task2.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.task2.config.AbstractIntegrationTest;
import com.example.task2.dto.SaveSongDto;
import com.example.task2.dto.SongFilter;
import com.example.task2.model.Album;
import com.example.task2.model.Artist;
import com.example.task2.model.Genre;
import com.example.task2.model.Song;
import com.example.task2.repository.AlbumRepository;
import com.example.task2.repository.ArtistRepository;
import com.example.task2.repository.GenreRepository;
import com.example.task2.repository.SongRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

public class SongControllerIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AlbumRepository albumRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    private UUID createdSongId;
    private UUID createdArtistId;
    private UUID createdGenreId;
    private UUID createdAlbumId;
    
    private final String API_PATH = "/api/song";


    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        songRepository.deleteAll(); 
        artistRepository.deleteAll();
        genreRepository.deleteAll();
    }

    @BeforeEach
    void setupTestData() {
        songRepository.deleteAll(); 
        artistRepository.deleteAll();
        genreRepository.deleteAll();

        Artist artist1 = Artist.builder().name("Test Song 1").build();
        Artist artist2 = Artist.builder().name("Test Song 2").build();

        Genre genre1 = Genre.builder().name("Genre 1").build();
        Genre genre2 = Genre.builder().name("Genre 2").build();

        Album album1 = Album.builder().name("Album 1").artist(artist1).build();
        Album album2 = Album.builder().name("Album 2").artist(artist2).build();

        Song song1 = Song.builder()
            .title("Song Title 1")
            .duration(210)
            .artist(artist1)
            .releaseYear(2020)
            .album(album1)
            .genres(List.of(genre1, genre2))
            .build();

        Song song2 = Song.builder()
            .title("Song Title 2")
            .duration(210)
            .artist(artist1)
            .releaseYear(2015)
            .album(album1)
            .genres(List.of(genre1))
            .build();

        Song song3 = Song.builder()
            .title("Song Title 3")
            .duration(210)
            .artist(artist2)
            .releaseYear(2020)
            .album(album2)
            .genres(List.of(genre1, genre2))
            .build();

        artistRepository.saveAll(List.of(artist1, artist2));
        genreRepository.saveAll(List.of(genre1, genre2));
        albumRepository.saveAll(List.of(album1, album2));
        songRepository.saveAll(List.of(song1, song2, song3));

        createdSongId = song1.getId();
        createdArtistId = artist1.getId();
        createdGenreId = genre1.getId();
        createdAlbumId = album1.getId();
    }


    @Test
    @DisplayName("POST /api/song - Successful song creation (201)")
    void createArtist_Success() throws Exception {
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title("New Song Title")
            .albumId(createdAlbumId)
            .artistId(createdArtistId)
            .duration(200)
            .releaseYear(2023)
            .genresId(List.of(createdGenreId))
            .build(); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(saveSongDto)));

        result.andExpect(status().isCreated())
              .andExpect(jsonPath("$.result").exists());
    }


    @Test
    @DisplayName("POST /api/song - Failure: Genre not found (404)")
    void createArtist_Failed_GenreNotExist() throws Exception {
        UUID nonexistentGenreId = UUID.randomUUID();
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title("New Song Title")
            .albumId(createdAlbumId)
            .artistId(createdArtistId)
            .duration(200)
            .releaseYear(2023)
            .genresId(List.of(nonexistentGenreId))
            .build(); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(saveSongDto)));

        result.andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value("Genre(s) with ID(s) not found: with id '" + nonexistentGenreId.toString() + "' not found."));
    }

    @Test
    @DisplayName("POST /api/song - Failure: Future Year (400)")
    void createArtist_Failed_FutureYear() throws Exception {
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title("New Song Title")
            .albumId(createdAlbumId)
            .artistId(createdArtistId)
            .duration(200)
            .releaseYear(2030)
            .genresId(List.of(createdGenreId))
            .build(); 
        
        ResultActions result = mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(saveSongDto)));

        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.message").value("releaseYear: The year cannot be later than the current year."));
    }


    @Test
    @DisplayName("GET /api/song/{id} - Successful retrieval of song details")
    void getSong_Success() throws Exception {
        
        ResultActions result = mockMvc.perform(get(API_PATH + "/{id}", createdSongId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.title").value("Song Title 1"));
    }

    @Test
    @DisplayName("GET /api/song/{id} - Failure: Song not found (404)")
    void getSong_Failed_NotExistingSongId() throws Exception {
        
        UUID nonExistingSongId = UUID.randomUUID();

        ResultActions result = mockMvc.perform(get(API_PATH + "/{id}", nonExistingSongId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value("Song with id '" + nonExistingSongId.toString() + "' not found."));
    }

    @Test
    @DisplayName("PUT /api/song/{id} - Successful song update")
    void updateSong_Success() throws Exception {
        
        String newTitle = "Updated Song Title";
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title(newTitle)
            .albumId(createdAlbumId)
            .artistId(createdArtistId)
            .duration(200)
            .releaseYear(2023)
            .genresId(List.of(createdGenreId))
            .build();

        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdSongId)
                .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(saveSongDto)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.result").value(createdSongId.toString()));
    }

    @Test
    @DisplayName("PUT /api/song/{id} - Failure: Title too long (400)")
    void updateSong_Failed_TitleTooLong() throws Exception {
        
        String newTitle = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
                + "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title(newTitle)
            .albumId(createdAlbumId)
            .artistId(createdArtistId)
            .duration(200)
            .releaseYear(2023)
            .genresId(List.of(createdGenreId))
            .build();

        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdSongId)
                .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(saveSongDto)));

        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.message").value("title: size must be between 1 and 100"));
    }

    @Test
    @DisplayName("PUT /api/song/{id} - Failure: Artist not found (404)")
    void updateSong_Failed_NotExistingArtist() throws Exception {
        
        UUID nonExistingArtistId = UUID.randomUUID();
        SaveSongDto saveSongDto = SaveSongDto.builder()
            .title("newTitle")
            .albumId(createdAlbumId)
            .artistId(nonExistingArtistId)
            .duration(200)
            .releaseYear(2023)
            .genresId(List.of(createdGenreId))
            .build();

        ResultActions result = mockMvc.perform(put(API_PATH + "/{id}", createdSongId)
                .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(saveSongDto)));

        result.andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value("Artist with id '" + nonExistingArtistId.toString() + "' not found."));
    }

    @Test
    @DisplayName("DELETE /api/song/{id} - Successful deletion (204)") 
    void deleteSong_Success() throws Exception {

        ResultActions result = mockMvc.perform(delete(API_PATH + "/{id}", createdSongId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/song/{id} - Failure: Song not found (404)")
    void deleteSong_Failed_NotExistingSong() throws Exception {

        UUID nonExistingSongId = UUID.randomUUID();
        ResultActions result = mockMvc.perform(delete(API_PATH + "/{id}", nonExistingSongId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message").value("Song with id '" + nonExistingSongId.toString() + "' not found."));
    }


    @Test
    @DisplayName("POST /api/song/_list - Successful retrieval of all records")
    void getSongs_Success() throws Exception {

        SongFilter songFilter = new SongFilter("", "", null);
        mockMvc.perform(post(API_PATH + "/_list") 
            
            .param("page", "0") 
            .param("size", "10") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(songFilter)))
            
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("POST /api/song/_list - Filter by release year (2015)") 
    void getSongs_Success_filterByReleasedYear() throws Exception {

        SongFilter songFilter = new SongFilter("", "", 2015);
        mockMvc.perform(post(API_PATH + "/_list")
            .param("page", "0") 
            .param("size", "10") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(songFilter)))
            
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/song/_list - Filter by album (Album 1)") 
    void getSongs_Success_filterByAlbum() throws Exception {

        SongFilter songFilter = new SongFilter("album 1", "", null);
        mockMvc.perform(post(API_PATH + "/_list") 
            .param("page", "0") 
            .param("size", "10") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(songFilter)))
            
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("POST /api/song/_report - Successful CSV report generation") 
    void generateReport_Success() throws Exception {
        
        SongFilter filter = new SongFilter(null, null, 2015);
        
        MvcResult result = mockMvc.perform(post(API_PATH + "/_report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(filter)))
                
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"songs.csv\"")))
                .andReturn();

        String csvContent = result.getResponse().getContentAsString();
        
        String[] lines = csvContent.trim().split("\n"); 
        
        Assertions.assertEquals(2, lines.length, "The report should contain the header and 2 records.");
        
        Assertions.assertTrue(lines[1].contains("Song Title 2"), "The first record should contain the expected title.");
    }

    @Test
    @DisplayName("POST /api/song/upload - Partial import success (1/1)")
    void uploadFromFile_PartialSuccess_ReturnsStats() throws Exception {

        final UUID nonExistentArtistId = UUID.randomUUID();

        String jsonContent = "[{"
                + "\"title\":\"Uploaded Song 1\","
                + "\"artistId\":\"" + createdArtistId + "\","
                + "\"releaseYear\":2023,"
                + "\"duration\":300,"
                + "\"albumId\":\"" + createdAlbumId + "\","
                + "\"genresId\":[\"" + createdGenreId + "\"]"
            + "}, {"
                + "\"title\":\"Uploaded Song 2\","
                + "\"artistId\":\"" + nonExistentArtistId + "\","
                + "\"releaseYear\":2024,"
                + "\"duration\":240,"
                + "\"albumId\":\"" + createdAlbumId + "\","
                + "\"genresId\":[\"" + createdGenreId + "\"]"
            + "}]";

        MockMultipartFile mockFile = new MockMultipartFile(
            "file", 
            "songs.json", 
            "application/json", 
            jsonContent.getBytes(StandardCharsets.UTF_8)
        );


        mockMvc.perform(multipart(API_PATH + "/upload").file(mockFile))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.successfulRecords").value(1))
            .andExpect(jsonPath("$.failedRecords").value(1));

    }

    @Test
    @DisplayName("POST /api/song/upload - Failure: Invalid file format (400)")
    void uploadFromFile_InvalidFormat_Returns400() throws Exception {

        MockMultipartFile mockFile = new MockMultipartFile(
        "file", "songs.xml", "text/xml", "content".getBytes()
        );

        mockMvc.perform(multipart(API_PATH + "/upload").file(mockFile))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Only JSON format is supported."));

    }

}
