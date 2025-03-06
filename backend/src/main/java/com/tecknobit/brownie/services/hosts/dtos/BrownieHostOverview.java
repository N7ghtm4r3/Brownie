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

@DTO
public class BrownieHostOverview {

    private final String id;

    private final String name;

    private final String hostAddress;

    private final HostStatus status;

    private final List<HostHistoryEvent> events;

    private final CPUUsage cpuUsage;

    private final BrownieHostStat memoryUsage;

    private final StorageUsage storageUsage;

    public BrownieHostOverview(BrownieHost host) {
        this(host, new CPUUsage(), new BrownieHostStat(), new StorageUsage());
    }

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


    public String getId() {
        return id;
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

    @JsonGetter(CPU_USAGE_KEY)
    public CPUUsage getCpuUsage() {
        return cpuUsage;
    }

    @JsonGetter(MEMORY_USAGE_KEY)
    public BrownieHostStat getMemoryUsage() {
        return memoryUsage;
    }

    @JsonGetter(STORAGE_USAGE_KEY)
    public StorageUsage getStorageUsage() {
        return storageUsage;
    }

    @JsonGetter(HOST_EVENTS_KEY)
    public List<HostHistoryEvent> getEvents() {
        return events;
    }

}
