package com.tecknobit.brownie.services.hosts.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.*;

@Entity
@Table(name = HOSTS_KEY)
public class BrownieHost extends EquinoxItem {

    @Column(unique = true)
    private final String name;

    @Column(name = HOST_ADDRESS_KEY)
    private final String hostAddress;

    @Enumerated(EnumType.STRING)
    private final HostStatus status;

    @Column(name = SSH_USER_KEY)
    private final String sshUser;

    @Column(name = SSH_PASSWORD_KEY)
    private final String sshPassword;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private final BrownieSession session;

    public BrownieHost() {
        this(null, null, null, null, null, null, null);
    }

    public BrownieHost(String id, String name, String hostAddress, HostStatus status, String sshUser,
                       String sshPassword, BrownieSession session) {
        super(id);
        this.name = name;
        this.hostAddress = hostAddress;
        this.status = status;
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
        this.session = session;
    }

    public String getName() {
        return name;
    }

    @JsonGetter(HOST_ADDRESS_KEY)
    public String getHostAddress() {
        return hostAddress;
    }

    public HostStatus getStatus() {
        return status;
    }

    @JsonIgnore
    public String getSshUser() {
        return sshUser;
    }

    @JsonIgnore
    public String getSshPassword() {
        return sshPassword;
    }

    @JsonIgnore
    public BrownieSession getSession() {
        return session;
    }

    @JsonIgnore
    public boolean isRemoteHost() {
        return sshUser == null;
    }

}
