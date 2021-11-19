package com.lazo.jc.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Lazo on 2021-11-19
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "users")
public class AppUserMinimalDomain {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;
}
