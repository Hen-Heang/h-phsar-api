package com.henheang.hphsar.model.appUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateActiveStatusRequest {
    private Boolean isActive;
}
