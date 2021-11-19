package com.lazo.jc.app.craftsman.service;

import com.lazo.jc.app.craftsman.models.AllUserModel;
import com.lazo.jc.app.craftsman.models.ProfileModel;
import com.lazo.jc.app.craftsman.models.checkIfPaidExpiredModel;
import com.lazo.jc.app.user.domains.AppUserDomain;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2021-03-30
 */

public interface CraftsmanService {

    ResponseEntity<AppUserDomain> getProfileData(String token);

    ResponseEntity<String> getRating(String token);

    ResponseEntity<Boolean> updateProfile(String token, ProfileModel model);

    ResponseEntity<List<AllUserModel>> allUsers(String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite);

    ResponseEntity<Boolean> addUserInFavorites(String token, Integer favoriteUserId);

    ResponseEntity<Boolean> removeFavorite(String token, Integer favoriteUserId);

    ResponseEntity<Boolean> updateNickname(String token, Long favUserId, String nickname);

    ResponseEntity<AllUserModel> getUserByUserId(String token, Long otherUserId);

    ResponseEntity<Boolean> makeVisible();

    ResponseEntity<Boolean> unMakeVisible();

    ResponseEntity<Boolean> getVisibilityStatus();

    ResponseEntity<Double> getPaidUsersTariff(String checkedUsers);

    ResponseEntity<Boolean> payForUsersContactInfo(String checkedUsers);

    ResponseEntity<Boolean> removeFromPaidUsers(Long paidUserId);

    ResponseEntity<checkIfPaidExpiredModel> checkIfPaidExpired(Long paidUserId);

    ResponseEntity<String> getCraftsmanPhoneByUserId(Long paidUserId);

    ResponseEntity<String> getCraftsmanEmailByUserId(Long paidUserId);
}
