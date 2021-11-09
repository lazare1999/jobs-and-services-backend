package com.lazo.jc.app.main.service;

import com.lazo.jc.app.email.MailService;
import com.lazo.jc.app.main.models.AuthenticationRequest;
import com.lazo.jc.app.main.models.AuthenticationResponse;
import com.lazo.jc.app.main.models.RegisterModel;
import com.lazo.jc.app.main.models.SmsOfficeResponseClass;
import com.lazo.jc.app.user.domains.TemporaryCodesDomain;
import com.lazo.jc.app.user.repository.TemporaryCodesRepository;
import com.lazo.jc.app.user.repository.UserRepository;
import com.lazo.jc.app.user.services.MyUserDetailsService;
import com.lazo.jc.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.lazo.jc.utils.EncryptUtils.encrypt;
import static com.lazo.jc.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-05-20
 */

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    HttpHeaders headers = new HttpHeaders();

    public static Map<String, String> forRegisterUserInfo = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build();

    public static Map<Long, String> resetPasswordsTempCodes = ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).build();

    private final TemporaryCodesRepository temporaryCodesRepository;

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtTokenUtils;

    private final MyUserDetailsService userDetailsService;

    private final MailService mailService;


    @Value("${js.module.smsOffice.api_key}")
    private String SMS_OFFICE_API_KEY;

    @Value("${js.module.smsOffice.sender}")
    private String SMS_OFFICE_SENDER;

    @Value("${js.module.salt}")
    private String SALT;

    @Override
    public ResponseEntity<Boolean> addRole(String token, Integer roleId) {
        if (roleId==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (!userDetailsService.checkIfRoleExists(roleId))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();

        if (userId ==null || Objects.equals(userId, 0))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        if (userDetailsService.roleIsAlreadyDefined(userId, roleId))
            return new ResponseEntity<>(true, headers, HttpStatus.OK);

        if (!userDetailsService.addRole(userId, roleId))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getUserName(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(userName, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> logout(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(userName);
        td.ifPresent(temporaryCodesDomain -> temporaryCodesRepository.deleteById(temporaryCodesDomain.getTemporaryCodeId()));

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    //TODO: smsoffice
    private boolean smsOffice(String destination, String content) {
        String url = "https://smsoffice.ge/api/v2/send/";

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", SMS_OFFICE_API_KEY);
        params.add("destination", destination);
        params.add("sender", SMS_OFFICE_SENDER);
        params.add("content", content);
//			params.add("urgent", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        var restTemplate = new RestTemplate();

        try {
            var response = restTemplate.postForEntity(url, request, SmsOfficeResponseClass.class).getBody();

            return response != null && response.getSuccess() != null && response.getSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void generateTemporaryCodeForLogin(String username, String countryCode) {

        if (StringUtils.isEmpty(countryCode) || StringUtils.isEmpty(username))
            return;

        var fullUsrName = countryCode + username;

//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

//        if (smsOffice(username, code)) {
            var userId = userRepository.findUserIdByUsername(fullUsrName);
            if (userId ==null || Objects.equals(userId, 0L)) {
                return;
            }
            var td = temporaryCodesRepository.findByUserName(fullUsrName);
            td.ifPresent(temporaryCodesDomain -> temporaryCodesRepository.deleteById(temporaryCodesDomain.getTemporaryCodeId()));
            temporaryCodesRepository.save(new TemporaryCodesDomain(fullUsrName, encrypt(SALT, code)));
//        }
    }

    @Override
    public void generateTemporaryCodeForRegister(String username, String countryCode) {

        if (StringUtils.isEmpty(countryCode) || StringUtils.isEmpty(username))
            return;

        var fullUsrName = countryCode + username;

//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

//        if (smsOffice(username, code)) {
            forRegisterUserInfo.remove(fullUsrName);
            forRegisterUserInfo.put(fullUsrName, encrypt(SALT, code));
//        }
    }

    @Override
    public ResponseEntity<?> createAuthenticationToken(AuthenticationRequest autRequest) throws Exception  {
        if (StringUtils.isEmpty(autRequest.getUsername()) ||
                StringUtils.isEmpty(autRequest.getPassword()) ||
                StringUtils.isEmpty(autRequest.getTempPassword())
        )
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(autRequest.getUsername());
        if (td.isEmpty() || td.get().getCode() == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);


        var newUser = userDetailsService.authenticateJwt(autRequest.getUsername(), encrypt(SALT, autRequest.getPassword()), encrypt(SALT, autRequest.getTempPassword()), td.get().getCode());
        if (newUser ==null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        try {
            authenticationManager.authenticate(newUser);
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or" +
                    " password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(autRequest.getUsername());

        final AuthenticationResponse jwt = jwtTokenUtils.generateToken(userDetails);

        return new ResponseEntity<>(jwt, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> jwtViaRefreshToken(String refreshToken) {
        String userName = null;
        if (StringUtils.isNotEmpty(refreshToken)) {
            try {
                if (jwtTokenUtils.extractAccessTokenStatus(refreshToken))
                    return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);
                userName = jwtTokenUtils.extractUsername(refreshToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (userName == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        var td = temporaryCodesRepository.findByUserName(userName);
        if (td.isEmpty() || td.get().getCode() == null)
            return new ResponseEntity<>("".toCharArray(), headers, HttpStatus.BAD_REQUEST);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

        final AuthenticationResponse jwt = jwtTokenUtils.generateToken(userDetails);

        return new ResponseEntity<>(jwt, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> register(RegisterModel model) {

        if (StringUtils.isEmpty(model.getCode()))
            return new ResponseEntity<>("temporary_code_empty", headers, HttpStatus.BAD_REQUEST);

        var username = model.getCountryPhoneCode() + model.getPhoneNumber();

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>("phone_number_empty", headers, HttpStatus.BAD_REQUEST);

        if (forRegisterUserInfo.get(username) == null)
            return new ResponseEntity<>("temporary_code_not_exists", headers, HttpStatus.BAD_REQUEST);

        var userId = userRepository.findUserIdByUsername(username);
        if (userId !=null && userId !=0)
            return new ResponseEntity<>("user_already_defined", headers, HttpStatus.OK);

        if (userRepository.findUserIdByEmail(model.getEmail()) !=null)
            return new ResponseEntity<>("email_already_in_use", headers, HttpStatus.OK);

        if (!Objects.equals(encrypt(SALT, model.getCode()), forRegisterUserInfo.get(username)))
            return new ResponseEntity<>("temporary_code_incorrect", headers, HttpStatus.BAD_REQUEST);

        userDetailsService.register(model, SALT);

        forRegisterUserInfo.remove(username);
        return new ResponseEntity<>("success", headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getTempCodeForResetPasswordByPhone(String countryCode, String phoneNumber) {


        if (StringUtils.isEmpty(countryCode) || StringUtils.isEmpty(phoneNumber))
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        var fullUsrName = countryCode + phoneNumber;

        var userId = userRepository.findUserIdByUsername(fullUsrName);

        if (userId == null)
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

//        if (smsOffice(fullUsrName, code)) {
            resetPasswordsTempCodes.remove(userId);
            resetPasswordsTempCodes.put(userId, encrypt(SALT, code));
//        }


        return new ResponseEntity<>(true, headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> getTempCodeForResetPasswordByEmail(String email) {


        if (StringUtils.isEmpty(email))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);


//        String code = RandomStringUtils.random(6, false, true);
        String code = "123";

        //TODO : გასაკეთებელია
        try {
            mailService.sendMail(email, "lazarekvirtia@gmail.com", "Reset Password", "temp code: " + code);
        } catch (Exception e) {
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);
        }

        var userId = userRepository.findUserIdByEmail(email);
        if (userId == null)
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        resetPasswordsTempCodes.remove(userId);
        resetPasswordsTempCodes.put(userId, encrypt(SALT, code));

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }


    private boolean resetPassword(Long userId, String tempPassword, String newPassword) {

        if (resetPasswordsTempCodes.get(userId) == null)
            return true;


        if (!Objects.equals(encrypt(SALT, tempPassword), resetPasswordsTempCodes.get(userId)))
            return true;

        userDetailsService.resetPassword(userId, newPassword, SALT);

        resetPasswordsTempCodes.remove(userId);

        return false;
    }

    @Override
    public ResponseEntity<?> resetPasswordByPhone(String countryPhoneCode, String phoneNumber, String newPassword, String tempPassword) {

        if (StringUtils.isEmpty(tempPassword))
            return new ResponseEntity<>("temporary_code_empty", headers, HttpStatus.BAD_REQUEST);

        var username = countryPhoneCode + phoneNumber;
        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>("phone_number_empty", headers, HttpStatus.BAD_REQUEST);

        var userId = userRepository.findUserIdByUsername(username);
        if (userId == null)
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        if (resetPassword(userId, tempPassword, newPassword))
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>("success", headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> resetPasswordByEmail(String email, String newPassword, String tempPassword) {

        if (StringUtils.isEmpty(tempPassword))
            return new ResponseEntity<>("temporary_code_empty", headers, HttpStatus.BAD_REQUEST);

        if (StringUtils.isEmpty(email))
            return new ResponseEntity<>("email_empty", headers, HttpStatus.BAD_REQUEST);

        var userId = userRepository.findUserIdByEmail(email);
        if (userId == null)
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        if (resetPassword(userId, tempPassword, newPassword))
            return new ResponseEntity<>(true, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>("success", headers, HttpStatus.OK);

    }


}
