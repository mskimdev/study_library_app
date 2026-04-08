package com.tenco.library.dto;


import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class BookBorrowJoin {
    private int id;
    private String title;
    private String author;
    private LocalDate borrowDate;
}
