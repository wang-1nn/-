package org.example.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementVO {
    private Long id;
    private String title;
    private LocalDate date;
    private String tag;
    private boolean isNew;
} 