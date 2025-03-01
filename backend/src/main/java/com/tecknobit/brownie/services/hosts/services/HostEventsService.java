package com.tecknobit.brownie.services.hosts.services;

import com.tecknobit.brownie.services.hosts.repositories.HostEventsRepository;
import com.tecknobit.browniecore.enums.HostEventType;
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
    public void registerHostStartedEvent(String hostId) {
        registerEvent(ONLINE, hostId);
    }

    @Wrapper
    public void registerHostStoppedEvent(String hostId) {
        registerEvent(OFFLINE, hostId);
    }

    @Wrapper
    public void registerHostRebootedEvent(String hostId) {
        registerEvent(REBOOTING, hostId);
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
