package com.example.task2.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class SongInfoDto {

    private final UUID id;
    private final String title;
    private final ArtistInfoDto artistInfo;
    private final int releaseYear;
    private final int duration;
    private final AlbumInfoDto album;
    private final List<GenreInfoDto> genres;
}
