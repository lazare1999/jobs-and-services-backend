package com.lazo.jc.app.main.service;

import com.lazo.jc.app.main.models.AuthenticationRequest;
import com.lazo.jc.app.main.models.RegisterModel;
import org.springframework.http.ResponseEntity;

/**
 * Created by Lazo on 2021-05-20
 */

public interface MainService {

    ResponseEntity<Boolean> addRole(String token, Integer roleId);

    ResponseEntity<String> getUserName(String token);

    ResponseEntity<Boolean> logout(String token);

    void generateTemporaryCodeForLogin(String username, String countryCode);

    void generateTemporaryCodeForRegister(String username, String countryCode);

    ResponseEntity<?> createAuthenticationToken(AuthenticationRequest autRequest) throws Exception;

    ResponseEntity<?> jwtViaRefreshToken(String refreshToken);

    ResponseEntity<String> register(RegisterModel model);
}
