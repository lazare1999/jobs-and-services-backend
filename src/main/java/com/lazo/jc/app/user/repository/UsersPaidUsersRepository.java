package com.lazo.jc.app.user.repository;

import com.lazo.jc.app.user.domains.UsersPaidUsersDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Created by Lazo on 2021-11-18
 * Created for <strong>Ministry of Internal Affairs</strong>
 */

public interface UsersPaidUsersRepository extends JpaRepository<UsersPaidUsersDomain, Long>, JpaSpecificationExecutor<UsersPaidUsersDomain> {

    List<UsersPaidUsersDomain> findAllByUserId(Long userId);

    @Modifying
    @Query("delete from UsersPaidUsersDomain p where p.userId = :userId and p.paidUserId = :paidUserId")
    void deleteByUserIdAndPaidUserId(@Param("userId") Long userId, @Param("paidUserId") Long paidUserId);

    Optional<UsersPaidUsersDomain> findByUserIdAndPaidUserId(Long userId, Long paidUserId);

}
