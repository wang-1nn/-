package org.example.backend.entity.vo;

import lombok.Data;
import org.example.backend.entity.pojo.QuestionBatch;
import org.springframework.data.relational.core.sql.In;

import java.util.List;

@Data
public class QuestionBatchVo {
   List<QuestionBatch> questionBatch;
    private List<Integer> count;
}
