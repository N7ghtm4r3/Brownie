package com.tecknobit.brownie.services.hosts.services;

import com.tecknobit.brownie.services.hosts.repositories.HostEventsRepository;
import com.tecknobit.browniecore.enums.HostEventType;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.HostEventType.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
public class HostEventsService {

    @Autowired
    private HostEventsRepository hostEventsRepository;

    @Wrapper
    public void registerHostStatusChangedEvent(String hostId, HostStatus status) {
        HostEventType type = null;
        switch (status) {
            case ONLINE -> type = ONLINE;
            case OFFLINE -> type = OFFLINE;
            case REBOOTING -> type = REBOOTING;
        }
        registerEvent(type, hostId);
    }

    @Wrapper
    public void registerHostRestartedEvent(String hostId) {
        registerEvent(RESTARTED, hostId);
    }

    @Wrapper
    public void registerServiceAddedEvent(String hostId, String serviceName) {
        registerEvent(SERVICE_ADDED, hostId, serviceName);
    }

    @Wrapper
    public void registerServiceRemovedEvent(String hostId, String serviceName, boolean removeFromTheHost) {
        HostEventType type = SERVICE_REMOVED;
        if (removeFromTheHost)
            type = SERVICE_DELETED;
        registerEvent(type, hostId, serviceName);
    }

    @Wrapper
    private void registerEvent(HostEventType type, String hostId) {
        registerEvent(type, hostId, null);
    }

    private void registerEvent(HostEventType type, String hostId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            hostEventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), hostId);
        else
            hostEventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), hostId);
    }

}
