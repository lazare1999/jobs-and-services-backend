package com.lazo.jc.app.main.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticationRequest implements Serializable {

    private String username;
    private String password;
    private String tempPassword;

}
