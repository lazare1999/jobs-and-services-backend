package com.lazo.jc.app.main.controller;

import com.lazo.jc.app.main.models.AuthenticationRequest;
import com.lazo.jc.app.main.models.RegisterModel;
import com.lazo.jc.app.main.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MainController {

	private final MainService mainService;

	@PreAuthorize("hasRole('ROLE_JC_ADMIN')")
	@RequestMapping({ "/add_role" })
	public ResponseEntity<Boolean> addRole(@RequestHeader("Authorization") String token, Integer roleId) {
		return mainService.addRole(token, roleId);
	}

	@PreAuthorize("hasRole('ROLE_JC_APP')")
	@RequestMapping({ "/get_user_name" })
	public ResponseEntity<String> getUserName(@RequestHeader("Authorization") String token) {
		return mainService.getUserName(token);
	}

	@PreAuthorize("hasRole('ROLE_JC_APP')")
	@RequestMapping({ "/logout_from_system" })
	public ResponseEntity<Boolean> logout(@RequestHeader("Authorization") String token) {
		return mainService.logout(token);
	}

	@RequestMapping(value = "/generate_temp_code_for_login")
	public void generateTemporaryCodeForLogin(String username, String countryCode) {
		mainService.generateTemporaryCodeForLogin(username, countryCode);
	}

	@RequestMapping(value = "/generate_temp_code_for_register")
	public void generateTemporaryCodeForRegister(String username, String countryCode) {
		mainService.generateTemporaryCodeForRegister(username, countryCode);
	}

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(AuthenticationRequest autRequest) throws Exception {
		return mainService.createAuthenticationToken(autRequest);
	}

	@RequestMapping(value = "/jwt_via_refresh_token", method = RequestMethod.POST)
	public ResponseEntity<?> jwtViaRefreshToken(String refreshToken) {
		return mainService.jwtViaRefreshToken(refreshToken);
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> register(RegisterModel model) {
		return mainService.register(model);
	}

	@RequestMapping(value = "/get_temp_code_for_reset_password_by_phone", method = RequestMethod.POST)
	public ResponseEntity<?> getTempCodeForResetPasswordByPhone(String countryCode, String phoneNumber) {
		return mainService.getTempCodeForResetPasswordByPhone(countryCode, phoneNumber);
	}

	@RequestMapping(value = "/get_temp_code_for_reset_password_by_email", method = RequestMethod.POST)
	public ResponseEntity<?> getTempCodeForResetPasswordByEmail(String email) {
		return mainService.getTempCodeForResetPasswordByEmail(email);
	}

	@RequestMapping(value = "/reset_password_by_phone", method = RequestMethod.POST)
	public ResponseEntity<?> resetPasswordByPhone(String countryPhoneCode, String phoneNumber, String newPassword, String tempPassword) {
		return mainService.resetPasswordByPhone(countryPhoneCode, phoneNumber, newPassword, tempPassword);
	}

	@RequestMapping(value = "/reset_password_by_email", method = RequestMethod.POST)
	public ResponseEntity<?> resetPasswordByEmail(String email, String newPassword, String tempPassword) {
		return mainService.resetPasswordByEmail(email, newPassword, tempPassword);
	}

}
