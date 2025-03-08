package com.tecknobit.brownie.services.hostservices.services;

import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService.ServiceConfiguration;
import com.tecknobit.brownie.services.hostservices.repositories.ServicesConfigurationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

/**
 * The {@code ServicesConfigurationsService} class is useful to manage all the {@link ServiceConfiguration} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class ServicesConfigurationsService {

    /**
     * {@code servicesConfigurationsRepository} instance used to access to the {@link SERVICES_CONFIGURATIONS_KEY} table
     */
    @Autowired
    private ServicesConfigurationsRepository servicesConfigurationsRepository;

    /**
     * Method used to store a new configuration of a {@link BrownieHostService}
     *
     * @param serviceId                The identifier of the service owner
     * @param programArguments         The program arguments
     * @param purgeNohupOutAfterReboot Whether the {@code nohup.out} file related to the service must be deleted
     *                                 at each service start
     * @param autoRunAfterHostReboot   Whether the service must be automatically restarted after the host start or
     *                                 the host restart
     */
    public void storeConfiguration(String serviceId, String programArguments, boolean purgeNohupOutAfterReboot,
                                   boolean autoRunAfterHostReboot) {
        servicesConfigurationsRepository.storeConfiguration(generateIdentifier(), autoRunAfterHostReboot, programArguments,
                purgeNohupOutAfterReboot, serviceId);
    }

    /**
     * Method used to edit an existing configuration of a {@link BrownieHostService}
     *
     * @param serviceId The identifier of the service owner
     * @param programArguments The program arguments
     * @param purgeNohupOutAfterReboot Whether the {@code nohup.out} file related to the service must be deleted
     *                                 at each service start
     * @param autoRunAfterHostReboot Whether the service must be automatically restarted after the host start or
     *                               the host restart
     */
    public void editConfiguration(String serviceId, String programArguments, boolean purgeNohupOutAfterReboot,
                                  boolean autoRunAfterHostReboot) {
        servicesConfigurationsRepository.editConfiguration(autoRunAfterHostReboot, programArguments,
                purgeNohupOutAfterReboot, serviceId);
    }

}
