package com.lazo.jc.app.user.repository;

import com.lazo.jc.app.user.domains.AppUserDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by Lazo on 2021-02-11
 */

public interface UserRepository extends JpaRepository<AppUserDomain, Long>, JpaSpecificationExecutor<AppUserDomain> {

    AppUserDomain findByUsername(String username);

    @Query("select u.userId from AppUserDomain u where u.username = :username")
    Long findUserIdByUsername(@Param("username") String username);

    @Query("select u.userId from AppUserDomain u where u.email = :email")
    Long findUserIdByEmail(@Param("email") String email);

    @Query("select u.visibleForSearch from AppUserDomain u where u.userId = :userId")
    Boolean getUserVisibility(@Param("userId") Long userId);

}
