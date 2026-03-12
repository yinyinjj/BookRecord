package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatistics {

    private Long totalBooks;
    private Long wantToRead;
    private Long reading;
    private Long completed;
    private Long abandoned;
}