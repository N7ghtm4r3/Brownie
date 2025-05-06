package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.HOST_ADDRESS_KEY;

/**
 * The {@code BrownieHost} class is useful to represent a Brownie's host
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = HOSTS_KEY)
public class BrownieHost extends EquinoxItem {

    /**
     * {@code name} the name of the host
     */
    @Column(unique = true)
    private final String name;

    /**
     * {@code hostAddress} the address of the host
     */
    @Column(name = HOST_ADDRESS_KEY)
    private final String hostAddress;

    /**
     * {@code status} the current status of the host
     */
    @Enumerated(EnumType.STRING)
    private final HostStatus status;

    /**
     * {@code sshUser} the user to use for the SSH connection
     */
    @Column(name = SSH_USER_KEY)
    private final String sshUser;

    /**
     * {@code sshPassword} the password to use for the SSH connection
     */
    @Column(name = SSH_PASSWORD_KEY)
    private final String sshPassword;

    /**
     * {@code session} the owner session of the host
     */
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(HOSTS_KEY)
    private final BrownieSession session;

    /**
     * {@code macAddress} the physical mac address of the remote host network interface
     */
    @Column(name = MAC_ADDRESS_KEY)
    private final String macAddress;

    /**
     * {@code broadcastIp} the ip address of the remote host network interface
     */
    @Column(name = BROADCAST_IP_KEY)
    private final String broadcastIp;

    /**
     * {@code events} the events related to the lifecycle of the host
     */
    @OneToMany(
            mappedBy = HOST_KEY,
            cascade = CascadeType.ALL
    )
    @OrderBy(EVENT_DATE_KEY + " DESC")
    @JsonIgnoreProperties(HOST_KEY)
    private final List<HostHistoryEvent> events;

    /**
     * {@code services} the services attached to the host
     */
    @OneToMany(
            mappedBy = HOST_KEY,
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @OrderBy(INSERTION_DATE_KEY + " DESC")
    @JsonIgnoreProperties(HOST_KEY)
    private final List<BrownieHostService> services;

    /**
     * {@code insertionDate} the date when the host has been inserted in the system
     */
    @Column(name = INSERTION_DATE_KEY)
    private final long insertionDate;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public BrownieHost() {
        this(null, null, null, null, null, null, null, null, null, List.of(), List.of(), 0);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id            The identifier of the host
     * @param name          The name of the host
     * @param hostAddress   The address of the host
     * @param status        The current status of the host
     * @param sshUser       The user to use for the SSH connection
     * @param sshPassword   The password to use for the SSH connection
     * @param session       The session owner of the host
     * @param macAddress    The physical mac address of the remote host network interface
     * @param broadcastIp   The ip address of the remote host network interface
     * @param events        The events related to the lifecycle of the host
     * @param services      The services attached to the host
     * @param insertionDate The date when the host has been inserted in the system
     */
    public BrownieHost(String id, String name, String hostAddress, HostStatus status, String sshUser,
                       String sshPassword, BrownieSession session, String macAddress, String broadcastIp,
                       List<HostHistoryEvent> events, List<BrownieHostService> services, long insertionDate) {
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
        this.insertionDate = insertionDate;
    }

    /**
     * Method to get the {@link #name} instance
     *
     * @return the {@link #name} instance as {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Method to get the {@link #hostAddress} instance
     *
     * @return the {@link #hostAddress} instance as {@link String}
     */
    @JsonGetter(HOST_ADDRESS_KEY)
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Method to get the {@link #status} instance
     *
     * @return the {@link #status} instance as {@link HostStatus}
     */
    public HostStatus getStatus() {
        return status;
    }

    /**
     * Method to get the {@link #sshUser} instance
     *
     * @return the {@link #sshUser} instance as {@link String}
     */
    @JsonIgnore
    public String getSshUser() {
        return sshUser;
    }

    /**
     * Method to get the {@link #sshPassword} instance
     *
     * @return the {@link #sshPassword} instance as {@link String}
     */
    @JsonIgnore
    public String getSshPassword() {
        return sshPassword;
    }

    /**
     * Method to get the {@link #session} instance
     *
     * @return the {@link #session} instance as {@link BrownieSession}
     */
    @JsonIgnore
    public BrownieSession getSession() {
        return session;
    }

    /**
     * Method to get the {@link #macAddress} instance
     *
     * @return the {@link #macAddress} instance as {@link String}
     */
    @JsonIgnore
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Method to get the {@link #broadcastIp} instance
     *
     * @return the {@link #broadcastIp} instance as {@link String}
     */
    @JsonIgnore
    public String getBroadcastIp() {
        return broadcastIp;
    }

    /**
     * Method to get the {@link #events} instance
     *
     * @return the {@link #events} instance as {@link List} of {@link HostHistoryEvent}
     */
    @JsonGetter(HOST_EVENTS_KEY)
    public List<HostHistoryEvent> getEvents() {
        return events;
    }

    /**
     * Method to get the {@link #services} instance
     *
     * @return the {@link #services} instance as {@link List} of {@link BrownieHostService}
     */
    @JsonGetter(SERVICES_KEY)
    public List<BrownieHostService> getServices() {
        return services;
    }

    /**
     * Method to get the {@link #insertionDate} instance
     *
     * @return the {@link #insertionDate} instance as {@code long}
     */
    @JsonGetter(INSERTION_DATE_KEY)
    public long getInsertionDate() {
        return insertionDate;
    }

    /**
     * Method to check whether the specified service belongs to the host
     *
     * @param serviceId The identifier of the service
     * @return whether the service belongs to the host as {@code boolean}
     */
    public boolean hasService(String serviceId) {
        for (BrownieHostService service : services)
            if (service.getId().equals(serviceId))
                return true;
        return false;
    }

    /**
     * Method to retrieve the specified service from to the host
     *
     * @param serviceId The identifier of the service
     * @return the service if exists as {@link BrownieHostService}, null otherwise
     */
    public BrownieHostService getService(String serviceId) {
        for (BrownieHostService service : services)
            if (service.getId().equals(serviceId))
                return service;
        return null;
    }

    /**
     * Method to check whether the host is currently {@link HostStatus#ONLINE}
     *
     * @return whether the host is currently {@link HostStatus#ONLINE} as {@code boolean}
     */
    @JsonIgnore
    public boolean isOnline() {
        return status.isOnline();
    }

    /**
     * Method to check whether the host is currently {@link HostStatus#OFFLINE}
     *
     * @return whether the host is currently {@link HostStatus#OFFLINE} as {@code boolean}
     */
    @JsonIgnore
    public boolean isOffline() {
        return status.isOffline();
    }

    /**
     * Method to check whether the host is remotely connected
     *
     * @return whether the host is remotely connected as {@code boolean}
     */
    @JsonIgnore
    public boolean isRemoteHost() {
        return sshUser != null;
    }

    // TODO: 06/05/2025 TO DOCU
    @JsonIgnore
    public List<String> listRunningServiceNames() {
        List<String> serviceNames = new ArrayList<>();
        for (BrownieHostService service : services)
            if (service.isRunning())
                serviceNames.add(service.getName());
        return serviceNames;
    }

    // TODO: 06/05/2025 TO DOCU
    @JsonIgnore
    public HashSet<Long> listRunningServicePids() {
        HashSet<Long> servicePids = new HashSet<>();
        for (BrownieHostService service : services)
            if (service.isRunning())
                servicePids.add(service.getPid());
        return servicePids;
    }

}
