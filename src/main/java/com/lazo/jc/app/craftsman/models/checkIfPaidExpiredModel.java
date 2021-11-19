package com.lazo.jc.app.craftsman.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-11-19
 */

@Getter
@Setter
public class checkIfPaidExpiredModel {
    private Boolean isNotPaid;
    private Long expiresIn;
}
