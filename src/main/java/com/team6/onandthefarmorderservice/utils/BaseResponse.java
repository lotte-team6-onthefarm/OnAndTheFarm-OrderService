package com.team6.onandthefarmorderservice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
@Builder
public class BaseResponse<T> {

    @ApiModelProperty(value = "HttpStatus Code", example = "OK")
    private HttpStatus httpStatus;  // 상태 코드 메세지

    @ApiModelProperty(value = "응답 메시지", example = "요청 성공하였습니다.")
    private String message; // 에러 설명

    @ApiModelProperty(value = "응답 result")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

//    public BaseResponse()

}

