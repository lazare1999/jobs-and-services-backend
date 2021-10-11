package com.lazo.jc.app.main.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticationRequest implements Serializable {

    private String countryPhoneCode;
    private String username;
    private String password;
    private String tempPassword;

    public String getUsername() {
        if (StringUtils.isNotEmpty(this.countryPhoneCode) && StringUtils.isNotEmpty(this.username))
            return this.countryPhoneCode + this.username;
        return "";
    }

}
