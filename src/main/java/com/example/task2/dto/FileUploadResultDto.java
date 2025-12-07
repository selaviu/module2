package com.example.task2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileUploadResultDto {

    private final int successfulRecords;
    private final int failedRecords;
}
