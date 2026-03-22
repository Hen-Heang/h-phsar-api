package com.henheang.hphsar.model.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreRatingRequest {
    private Integer ratedStar;
//    private String comment;
}
