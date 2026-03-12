package com.bookrecord.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingProgressRequest {

    @NotNull(message = "Current page is required")
    @Min(value = 0, message = "Current page must be non-negative")
    private Integer currentPage;
}