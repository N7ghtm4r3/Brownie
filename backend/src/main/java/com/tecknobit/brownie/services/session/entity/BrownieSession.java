package com.tecknobit.brownie.services.session.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
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

    @OneToMany(
            mappedBy = SESSION_KEY,
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    private final List<BrownieHost> hosts;

    public BrownieSession() {
        this(null, null, null);
    }

    public BrownieSession(String id, String joinCode, String password) {
        this(id, joinCode, password, List.of());
    }

    public BrownieSession(String id, String joinCode, String password, List<BrownieHost> hosts) {
        super(id);
        this.joinCode = joinCode;
        this.password = password;
        this.hosts = hosts;
    }

    @JsonGetter(JOIN_CODE_KEY)
    public String getJoinCode() {
        return joinCode;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public List<BrownieHost> getHosts() {
        return hosts;
    }

}
