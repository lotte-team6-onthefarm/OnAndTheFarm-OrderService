package com.team6.onandthefarmorderservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
@Builder
public class BaseResponse<T> {

    private HttpStatus httpStatus;  // 상태 코드 메세지

    private String message; // 에러 설명

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

//    public BaseResponse()

}

