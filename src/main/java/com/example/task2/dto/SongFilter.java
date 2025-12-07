package com.example.task2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongFilter {

    private final String albumName;
    private final String artistName;
    private final Integer releasedYear;
}
