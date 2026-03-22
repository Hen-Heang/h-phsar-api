package com.henheang.hphsar.model.retailer.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalRejectedAndAccepted {

    private Integer totalRejected;
    private Integer totalAccepted;
}
