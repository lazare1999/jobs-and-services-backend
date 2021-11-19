package com.lazo.jc.app.user.repository;

import com.lazo.jc.app.user.domains.AppUserMinimalDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Lazo on 2021-11-19
 */

public interface AppUserMinimalRepository extends JpaRepository<AppUserMinimalDomain, Long>, JpaSpecificationExecutor<AppUserMinimalDomain> {
}
