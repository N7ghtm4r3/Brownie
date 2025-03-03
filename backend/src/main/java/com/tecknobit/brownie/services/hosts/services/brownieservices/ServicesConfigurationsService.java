package com.tecknobit.brownie.services.hosts.services.brownieservices;

import com.tecknobit.brownie.services.hosts.repositories.services.ServicesConfigurationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
public class ServicesConfigurationsService {

    @Autowired
    private ServicesConfigurationsRepository servicesConfigurationsRepository;

    public void storeConfiguration(String serviceId, String programArguments, boolean purgeNohupOutAfterReboot,
                                   boolean autoRunAfterHostReboot) {
        servicesConfigurationsRepository.storeConfiguration(generateIdentifier(), autoRunAfterHostReboot, programArguments,
                purgeNohupOutAfterReboot, serviceId);
    }

    public void editConfiguration(String serviceId, String programArguments, boolean purgeNohupOutAfterReboot,
                                  boolean autoRunAfterHostReboot) {
        servicesConfigurationsRepository.editConfiguration(autoRunAfterHostReboot, programArguments,
                purgeNohupOutAfterReboot, serviceId);
    }

}
