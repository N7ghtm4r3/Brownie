package com.tecknobit.brownie.services.session.entity;

import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static com.tecknobit.browniecore.ConstantsKt.JOIN_CODE_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSIONS_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;

@Entity
@Table(name = SESSIONS_KEY)
public class BrownieSession extends EquinoxItem {

    @Column(
            name = JOIN_CODE_KEY,
            unique = true
    )
    private final String joinCode;

    @Column(name = PASSWORD_KEY)
    private final String password;

    public BrownieSession() {
        this(null, null, null);
    }

    public BrownieSession(String id, String joinCode, String password) {
        super(id);
        this.joinCode = joinCode;
        this.password = password;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public String getPassword() {
        return password;
    }

}
