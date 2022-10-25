package com.team6.onandthefarmorderservice.vo.feignclient;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {

    private Long userId;

    private String userEmail;

    //private String userPassword;

    private String userZipcode;

    private String userAddress;

    private String userAddressDetail;

    private String userPhone;

    private String userBirthday;

    private Integer userSex;

    private String userName;

    private String userRegisterDate;

    private Boolean userIsActivated;

    private String role;

    private String provider;

    private Long userKakaoNumber;

    private String userNaverNumber;

    private String userAppleNumber;

    private String userGoogleNumber;

    private Integer userFollowingCount;

    private Integer userFollowerCount;

    private String userProfileImg;
}
