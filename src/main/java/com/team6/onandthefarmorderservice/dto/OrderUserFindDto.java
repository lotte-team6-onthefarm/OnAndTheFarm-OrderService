package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserFindDto {
    private String userId;

    private String startDate;

    private String endDate;

    private Integer pageNumber;
}
