package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

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

    @OneToMany(
            mappedBy = HOST_KEY,
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @OrderBy(EVENT_DATE_KEY + " DESC")
    private final List<HostHistoryEvent> events;

    public BrownieHost() {
        this(null, null, null, null, null, null, null, List.of());
    }

    public BrownieHost(String id, String name, String hostAddress, HostStatus status, String sshUser,
                       String sshPassword, BrownieSession session, List<HostHistoryEvent> events) {
        super(id);
        this.name = name;
        this.hostAddress = hostAddress;
        this.status = status;
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
        this.session = session;
        this.events = events;
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

    @JsonGetter(HOST_EVENTS_KEY)
    public List<HostHistoryEvent> getEvents() {
        return events;
    }

    @JsonIgnore
    public boolean isOnline() {
        return status.isOnline();
    }

    @JsonIgnore
    public boolean isOffline() {
        return status.isOffline();
    }

    @JsonIgnore
    public boolean isRemoteHost() {
        return sshUser != null;
    }

}
