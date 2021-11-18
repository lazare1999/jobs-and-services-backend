package com.lazo.jc.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by Lazo on 2021-11-18
 * Created for <strong>Ministry of Internal Affairs</strong>
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "users_paid_users")
public class UsersPaidUsersDomain {

    @Id
    @Column(name = "users_paid_users_id")
    @SequenceGenerator(name = "users_paid_users_users_paid_users_id_seq", sequenceName = "users.users_paid_users_users_paid_users_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_paid_users_users_paid_users_id_seq")
    private Long usersPaidUsersId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "paid_user_id")
    private Long paidUserId;

    @Column(name = "paid_until", updatable = false)
    private LocalDateTime paidUntil;

}
