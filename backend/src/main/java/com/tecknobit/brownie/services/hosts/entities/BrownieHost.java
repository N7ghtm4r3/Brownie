package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties(HOSTS_KEY)
    private final BrownieSession session;

    @Column(name = MAC_ADDRESS_KEY)
    private final String macAddress;

    @Column(name = BROADCAST_IP_KEY)
    private final String broadcastIp;

    @OneToMany(
            mappedBy = HOST_KEY,
            cascade = CascadeType.ALL
    )
    @OrderBy(EVENT_DATE_KEY + " DESC")
    @JsonIgnoreProperties(HOST_KEY)
    private final List<HostHistoryEvent> events;

    @OneToMany(
            mappedBy = HOST_KEY,
            cascade = CascadeType.ALL
    )
    @OrderBy(INSERTION_DATE_KEY + " DESC")
    @JsonIgnoreProperties(HOST_KEY)
    private final List<BrownieHostService> services;

    public BrownieHost() {
        this(null, null, null, null, null, null, null, null, null, List.of(), List.of());
    }

    public BrownieHost(String id, String name, String hostAddress, HostStatus status, String sshUser,
                       String sshPassword, BrownieSession session, String macAddress, String broadcastIp,
                       List<HostHistoryEvent> events, List<BrownieHostService> services) {
        super(id);
        this.name = name;
        this.hostAddress = hostAddress;
        this.status = status;
        this.sshUser = sshUser;
        this.sshPassword = sshPassword;
        this.session = session;
        this.macAddress = macAddress;
        this.broadcastIp = broadcastIp;
        this.events = events;
        this.services = services;
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
    public String getMacAddress() {
        return macAddress;
    }

    @JsonIgnore
    public String getBroadcastIp() {
        return broadcastIp;
    }

    @JsonGetter(HOST_EVENTS_KEY)
    public List<HostHistoryEvent> getEvents() {
        return events;
    }

    @JsonGetter(SERVICES_KEY)
    public List<BrownieHostService> getServices() {
        return services;
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
