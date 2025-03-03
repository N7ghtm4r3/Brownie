package com.tecknobit.brownie.services.hostservices.services;

import com.tecknobit.brownie.services.hosts.services.HostEventsService;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.repositories.HostServicesRepository;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.tecknobit.browniecore.enums.ServiceStatus.STOPPED;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator.formatFullTextKeywords;
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

    public PaginatedResponse<BrownieHostService> getServices(String hostId, Set<String> keywords, List<String> statuses,
                                                             int page, int pageSize) {
        String fullTextKeywords = formatFullTextKeywords(keywords, "+", "*", true);
        long totalServices = servicesRepository.countServices(hostId, fullTextKeywords, statuses);
        List<BrownieHostService> services = servicesRepository.getServices(hostId, fullTextKeywords, statuses,
                PageRequest.of(page, pageSize));
        return new PaginatedResponse<>(services, page, pageSize, totalServices);
    }

}
