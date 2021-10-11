package com.lazo.jc.app.main.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-02-15
 */

@Getter
@Setter
public class RegisterModel {

    private String phoneNumber;
    private String countryPhoneCode;
    private String firstName;
    private String lastName;
    private String email;
    private String nickname;
    private String password;
    private String code;
    private String address;
    private String personalNumber;
    private String passportNumber;

    public String getFullPhone() {
        return this.countryPhoneCode + this.phoneNumber;
    }

}
