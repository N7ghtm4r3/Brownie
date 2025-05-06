package com.tecknobit.brownie.services.session.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import jakarta.persistence.*;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;

/**
 * The {@code BrownieSession} class is useful to represent a Brownie's session
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = SESSIONS_KEY)
public class BrownieSession extends EquinoxItem {

    /**
     * {@code joinCode} the join code to connect to the session
     */
    @Column(
            name = JOIN_CODE_KEY,
            unique = true
    )
    private final String joinCode;

    /**
     * {@code password} the password used to protect the session accesses
     */
    @Column(name = PASSWORD_KEY)
    private final String password;

    /**
     * {@code hosts} the hosts attached to the session
     */
    @OneToMany(
            mappedBy = SESSION_KEY,
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JsonIgnoreProperties(SESSION_KEY)
    private final List<BrownieHost> hosts;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public BrownieSession() {
        this(null, null, null);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id       The identifier of the session
     * @param joinCode The join code to connect to the session
     * @param password The password used to protect the session accesses
     */
    @Wrapper
    public BrownieSession(String id, String joinCode, String password) {
        this(id, joinCode, password, List.of());
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id The identifier of the session
     * @param joinCode The join code to connect to the session
     * @param password The password used to protect the session accesses
     * @param hosts The hosts attached to the session
     */
    public BrownieSession(String id, String joinCode, String password, List<BrownieHost> hosts) {
        super(id);
        this.joinCode = joinCode;
        this.password = password;
        this.hosts = hosts;
    }

    /**
     * Method to get the {@link #joinCode} instance
     *
     * @return the {@link #joinCode} instance as {@link String}
     */
    @JsonGetter(JOIN_CODE_KEY)
    public String getJoinCode() {
        return joinCode;
    }

    /**
     * Method to get the {@link #password} instance
     *
     * @return the {@link #password} instance as {@link String}
     */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /**
     * Method to get the {@link #hosts} instance
     *
     * @return the {@link #hosts} instance as {@link BrownieHost} of {@link BrownieHost}
     */
    @JsonIgnore
    public List<BrownieHost> getHosts() {
        return hosts;
    }

}
