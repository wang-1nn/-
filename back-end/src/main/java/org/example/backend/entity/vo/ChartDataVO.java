package org.example.backend.entity.vo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataVO<L, V> {
    private List<L> labels;
    private List<V> values;
}