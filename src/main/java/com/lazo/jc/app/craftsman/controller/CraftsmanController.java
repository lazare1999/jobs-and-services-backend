package com.lazo.jc.app.craftsman.controller;

import com.lazo.jc.app.craftsman.models.AllUserModel;
import com.lazo.jc.app.craftsman.models.ProfileModel;
import com.lazo.jc.app.craftsman.service.CraftsmanService;
import com.lazo.jc.app.user.domains.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2021-03-30
 */

@RestController
@RequestMapping("craftsman")
@RequiredArgsConstructor
public class CraftsmanController {

    private final CraftsmanService craftsmanService;

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
	@RequestMapping({ "/get_profile_data" })
	public ResponseEntity<AppUser> getProfileData(@RequestHeader("Authorization") String token) {
        return craftsmanService.getProfileData(token);
	}

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/get_rating" })
    public ResponseEntity<String> getRating(@RequestHeader("Authorization") String token) {
        return craftsmanService.getRating(token);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/update_profile" })
    public ResponseEntity<Boolean> updateProfile(@RequestHeader("Authorization") String token, ProfileModel model) {
        return craftsmanService.updateProfile(token, model);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/all_users" })
    public ResponseEntity<List<AllUserModel>> allUsers(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite) {
        return craftsmanService.allUsers(token, pageKey, pageSize, searchOnlyFavorite);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/add_user_in_favorites" })
    public ResponseEntity<Boolean> addUserInFavorites(@RequestHeader("Authorization") String token, Integer favoriteUserId) {
        return craftsmanService.addUserInFavorites(token, favoriteUserId);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/remove_favorite" })
    public ResponseEntity<Boolean> removeFavorite(@RequestHeader("Authorization") String token, Integer favoriteUserId) {
        return craftsmanService.removeFavorite(token, favoriteUserId);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/update_nickname" })
    public ResponseEntity<Boolean> updateNickname(@RequestHeader("Authorization") String token, Long favUserId, String nickname) {
        return craftsmanService.updateNickname(token, favUserId, nickname);
    }

    @PreAuthorize("hasRole('ROLE_JC_CRAFTSMAN')")
    @RequestMapping({ "/get_user_by_user_id" })
    public ResponseEntity<AllUserModel> getUserByUserId(@RequestHeader("Authorization") String token, Long otherUserId) {
        return craftsmanService.getUserByUserId(token, otherUserId);
    }

}
