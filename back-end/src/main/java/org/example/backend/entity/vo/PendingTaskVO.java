package org.example.backend.entity.vo;



import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingTaskVO {
    private String id;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime deadline;
    private String priority;
    private boolean completed;
}