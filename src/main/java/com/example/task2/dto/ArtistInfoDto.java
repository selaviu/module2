package com.example.task2.dto;


import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class ArtistInfoDto {

    private final UUID id;
    private final String name;
}
