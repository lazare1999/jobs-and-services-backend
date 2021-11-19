package com.lazo.jc.app.craftsman.models;

import com.lazo.jc.app.user.domains.AppUserDomain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by Lazo on 2021-06-03
 */

@Getter
@Setter
@RequiredArgsConstructor
public class AllUserModel {

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Double rating;
    private String mainNickname;
    private String email;
    private String nickname;
    private Boolean isFav;
    private Boolean isPaid;

    public AllUserModel(AppUserDomain u, String nickname, Boolean isFav , Boolean isPaid) {
        this.userId = u.getUserId();
        this.username = u.getUsername();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
        this.rating = u.getRating();
        this.mainNickname = u.getNickname();
        this.email = u.getEmail();
        this.nickname = nickname;
        this.isFav = isFav;
        this.isPaid = isPaid;
    }

}
