package com.tecknobit.brownie.services.hosts.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.brownie.services.hosts.dtos.usages.CPUUsage;
import com.tecknobit.brownie.services.hosts.dtos.usages.StorageUsage;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.entities.HostHistoryEvent;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.DTO;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.HOST_ADDRESS_KEY;

/**
 * The {@code BrownieHostOverview} class is used as {@link DTO} to share the information about the current
 * stats of a {@link BrownieHost}
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
public class BrownieHostOverview {

    /**
     * {@code id} the identifier of the host
     */
    private final String id;

    /**
     * {@code name} the name of the host
     */
    private final String name;

    /**
     * {@code hostAddress} the address of the host
     */
    private final String hostAddress;

    /**
     * {@code status} the current status of the host
     */
    private final HostStatus status;

    /**
     * {@code events} the events related to the lifecycle of the host
     */
    private final List<HostHistoryEvent> events;

    /**
     * {@code cpuUsage} the current usage of the {@code CPU}
     */
    private final CPUUsage cpuUsage;

    /**
     * {@code memoryUsage} the current usage of the {@code RAM memory}
     */
    private final BrownieHostStat memoryUsage;

    /**
     * {@code storageUsage} the current usage of the {@code storage}
     */
    private final StorageUsage storageUsage;

    /**
     * Constructor to instantiate the object with default values when the host is not reachable
     *
     * @param host The host monitored
     */
    public BrownieHostOverview(BrownieHost host) {
        this(host, new CPUUsage(), new BrownieHostStat(), new StorageUsage());
    }

    /**
     * Constructor to instantiate the object
     *
     * @param host         The host monitored
     * @param cpuUsage     The current usage of the {@code CPU}
     * @param memoryUsage  The current usage of the {@code RAM memory}
     * @param storageUsage The current usage of the {@code storage}
     */
    public BrownieHostOverview(BrownieHost host, CPUUsage cpuUsage, BrownieHostStat memoryUsage, StorageUsage storageUsage) {
        id = host.getId();
        name = host.getName();
        hostAddress = host.getHostAddress();
        status = host.getStatus();
        events = host.getEvents();
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.storageUsage = storageUsage;
    }

    /**
     * Method to get the {@link #id} instance
     *
     * @return the {@link #id} instance as {@link String}
     */
    public String getId() {
        return id;
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
     * Method to get the {@link #cpuUsage} instance
     *
     * @return the {@link #cpuUsage} instance as {@link CPUUsage}
     */
    @JsonGetter(CPU_USAGE_KEY)
    public CPUUsage getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Method to get the {@link #memoryUsage} instance
     *
     * @return the {@link #memoryUsage} instance as {@link BrownieHostStat}
     */
    @JsonGetter(MEMORY_USAGE_KEY)
    public BrownieHostStat getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * Method to get the {@link #storageUsage} instance
     *
     * @return the {@link #storageUsage} instance as {@link StorageUsage}
     */
    @JsonGetter(STORAGE_USAGE_KEY)
    public StorageUsage getStorageUsage() {
        return storageUsage;
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

}
