package com.example.task2.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GenreInfoDto {

    private final UUID id;
    private final String name;
}
