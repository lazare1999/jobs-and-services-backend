package com.lazo.jc.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Lazo on 2021-02-11
 */


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "active_users")
public class AppUserDomain {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String username;

    @Column(name = "user_passwd")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "personal_number")
    private String personalNumber;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "visible_for_search", insertable = false)
    private Boolean visibleForSearch;

}
