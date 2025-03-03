package com.tecknobit.brownie.services.hostservices.services;

import com.tecknobit.brownie.services.hostservices.repositories.HostServiceEventsRepository;
import com.tecknobit.browniecore.enums.ServiceEventType;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.ServiceEventType.RUNNING;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
public class HostServiceEventsService {

    @Autowired
    private HostServiceEventsRepository eventsRepository;

    @Wrapper
    public void registerServiceStarted(String serviceId, long pid) {
        registerEvent(RUNNING, serviceId, pid);
    }

    @Wrapper
    private void registerEvent(ServiceEventType type, String serviceId) {
        registerEvent(type, serviceId, null);
    }

    private void registerEvent(ServiceEventType type, String serviceId, Object extra) {
        String eventId = generateIdentifier();
        if (extra == null)
            eventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), serviceId);
        else
            eventsRepository.registerEvent(eventId, type.name(), System.currentTimeMillis(), extra.toString(), serviceId);
    }

}
