package com.lazo.jc.app.craftsman.service;

import com.lazo.jc.app.craftsman.models.AllUserModel;
import com.lazo.jc.app.craftsman.models.ProfileModel;
import com.lazo.jc.app.user.domains.AppUser;
import com.lazo.jc.app.user.domains.UsersFavoriteUsersDomain;
import com.lazo.jc.app.user.repository.UserRepository;
import com.lazo.jc.app.user.repository.UsersFavoriteUsersRepository;
import com.lazo.jc.utils.JwtUtils;
import com.lazo.jc.utils.LazoUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.lazo.jc.utils.LazoUtils.getCurrentApplicationUserId;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lazo on 2021-03-30
 */

@Service
@RequiredArgsConstructor
public class CraftsmanServiceImpl implements CraftsmanService {

    HttpHeaders headers = new HttpHeaders();

    private final UserRepository userRepository;

    private final UsersFavoriteUsersRepository usersFavoriteUsersRepository;

    private final JwtUtils jwtTokenUtils;

    @Override
    public ResponseEntity<AppUser> getProfileData(String token) {
        var userName = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(new AppUser(), headers, HttpStatus.BAD_REQUEST);

        var username = jwtTokenUtils.extractUsername(token.substring(7));

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>(new AppUser(), headers, HttpStatus.BAD_REQUEST);


        AppUser user = userRepository.findByUsername(username);

        return new ResponseEntity<>(user, headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<String> getRating(String token) {
        var username = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>("", headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findByUsername(username);
        var rating = user.getRating();

        if (rating ==null)
            return new ResponseEntity<>("0.0", headers, HttpStatus.OK);

        return new ResponseEntity<>(String.valueOf(rating), headers, HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Boolean> updateProfile(String token, ProfileModel model) {
        var username = jwtTokenUtils.getUserNameViaToken(token);

        if (StringUtils.isEmpty(username))
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var user = userRepository.findByUsername(username);
        user.setFirstName(model.getFirstName());
        user.setLastName(model.getLastName());
        user.setNickname(model.getNickname());
        user.setEmail(model.getEmail());
        userRepository.save(user);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<AllUserModel>> allUsers(String token, Integer pageKey, Integer pageSize, Boolean searchOnlyFavorite) {
        List<AllUserModel> ans = new ArrayList<>();
        if (StringUtils.isEmpty(token) || pageKey ==null || pageSize ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var favUsers = usersFavoriteUsersRepository.findAllByUserId(userId.longValue());
        var favUsersIds = new ArrayList<>();
        favUsersIds.add(-1L);
        for (var f : favUsers) {
            favUsersIds.add(f.getFavoriteUserId());
        }

        var users =  userRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();

            if (searchOnlyFavorite) {
                predicate = builder.and(predicate, builder.in(root.get("userId")).value(favUsersIds));
            } else {
                predicate = builder.and(predicate, builder.not(builder.in(root.get("userId")).value(favUsersIds)));
            }

            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("rating")));

        for (var u : users) {
            String nName = "";
            var isF = false;
            if (favUsersIds.contains(u.getUserId())) {
                isF = true;
                nName = usersFavoriteUsersRepository.nickname(userId.longValue(), u.getUserId());
            }

            var m = new AllUserModel(u, nName, isF);
            ans.add(m);
        }

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> addUserInFavorites(String token, Integer favoriteUserId) {
        if (StringUtils.isEmpty(token) || favoriteUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var newFav = new UsersFavoriteUsersDomain();
        newFav.setUserId(userId.longValue());
        newFav.setFavoriteUserId(favoriteUserId.longValue());
        usersFavoriteUsersRepository.save(newFav);

        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Boolean> removeFavorite(String token, Integer favoriteUserId) {
        if (StringUtils.isEmpty(token) || favoriteUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        usersFavoriteUsersRepository.deleteByUserIdAndFavoriteUserId(userId.longValue(), favoriteUserId.longValue());
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> updateNickname(String token, Long favUserId, String nickname) {
        if (StringUtils.isEmpty(token) || favUserId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var favUser0 = usersFavoriteUsersRepository.findByUserIdAndFavoriteUserId(userId.longValue(), favUserId);

        if (favUser0.isEmpty())
            return new ResponseEntity<>(false, headers, HttpStatus.BAD_REQUEST);

        var favUser = favUser0.get();
        favUser.setFavoriteNickname(nickname);
        usersFavoriteUsersRepository.save(favUser);
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AllUserModel> getUserByUserId(String token, Long otherUserId) {
        var ans = new AllUserModel();
        if (otherUserId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return new ResponseEntity<>(ans, headers, HttpStatus.BAD_REQUEST);

        var user0 = userRepository.findById(otherUserId);
        if (user0.isEmpty())
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var user = user0.get();

        var favUsers = usersFavoriteUsersRepository.findAllByUserId(Long.valueOf(userId));
        var favUsersIds = new ArrayList<>();
        for (var f : favUsers) {
            favUsersIds.add(f.getFavoriteUserId());
        }

        if (favUsersIds.contains(otherUserId)) {
            ans.setNickname(usersFavoriteUsersRepository.nickname(userId.longValue(), user.getUserId()));
        }

        if (StringUtils.isEmpty(ans.getNickname())) {
            if (StringUtils.isNotEmpty(user.getUsername()))
                ans.setUsername(user.getUsername());

            if (StringUtils.isNotEmpty(user.getFirstName()))
                ans.setFirstName(user.getFirstName());

            if (StringUtils.isNotEmpty(user.getLastName()))
                ans.setLastName(user.getLastName());

            if (StringUtils.isNotEmpty(user.getNickname()))
                ans.setMainNickname(user.getNickname());
        }


        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

}
