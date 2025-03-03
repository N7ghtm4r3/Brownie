package com.tecknobit.brownie.services.hosts.services.brownieservices;

import com.tecknobit.brownie.services.hosts.repositories.services.HostServicesRepository;
import com.tecknobit.brownie.services.hosts.services.HostEventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.browniecore.enums.ServiceStatus.STOPPED;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
public class HostServicesService {

    @Autowired
    private HostServicesRepository servicesRepository;

    @Autowired
    private HostEventsService eventsService;

    @Autowired
    private ServicesConfigurationsService configurationsService;

    public void storeService(String serviceName, String servicePath, String hostId, String programArguments,
                             boolean purgeNohupOutAfterReboot, boolean autoRunAfterHostReboot) {
        String serviceId = generateIdentifier();
        servicesRepository.storeService(serviceId, serviceName, STOPPED.name(), System.currentTimeMillis(), hostId,
                servicePath);
        configurationsService.storeConfiguration(serviceId, programArguments, purgeNohupOutAfterReboot, autoRunAfterHostReboot);
        eventsService.registerServiceAddedEvent(hostId, serviceName);
    }

    public void editService(String serviceId, String serviceName, String servicePath, String programArguments,
                            boolean purgeNohupOutAfterReboot, boolean autoRunAfterHostReboot) {
        servicesRepository.editService(serviceId, serviceName, servicePath);
        configurationsService.editConfiguration(serviceId, programArguments, purgeNohupOutAfterReboot,
                autoRunAfterHostReboot);
    }

}
