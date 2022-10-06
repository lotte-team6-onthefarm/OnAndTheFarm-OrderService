package com.team6.onandthefarmorderservice.vo.user;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
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
}
