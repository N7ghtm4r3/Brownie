package com.tecknobit.brownie.services.hostservices.services;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.helpers.shell.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostEventsService;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.repositories.HostServicesRepository;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.tecknobit.browniecore.enums.ServiceStatus.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator.formatFullTextKeywords;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

@Service
public class HostServicesService {

    @Autowired
    private HostServicesRepository servicesRepository;

    @Autowired
    private HostEventsService hostEventsService;

    @Autowired
    private ServicesConfigurationsService configurationsService;

    @Autowired
    private HostServiceEventsService serviceEvents;

    public void storeService(String serviceName, String servicePath, String hostId, String programArguments,
                             boolean purgeNohupOutAfterReboot, boolean autoRunAfterHostReboot) {
        String serviceId = generateIdentifier();
        servicesRepository.storeService(serviceId, serviceName, STOPPED.name(), System.currentTimeMillis(), hostId,
                servicePath);
        configurationsService.storeConfiguration(serviceId, programArguments, purgeNohupOutAfterReboot, autoRunAfterHostReboot);
        hostEventsService.registerServiceAddedEvent(hostId, serviceName);
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

    public void startService(BrownieHost host, BrownieHostService service) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        long pid = commandsExecutor.startService(service);
        if (pid == -1)
            throw new JSchException();
        String serviceId = service.getId();
        servicesRepository.updateServiceStatus(serviceId, RUNNING.name(), pid);
        serviceEvents.registerServiceStarted(serviceId, pid);
    }

    public void rebootService(BrownieHost host, BrownieHostService service) throws Exception {
        String serviceId = service.getId();
        setServiceInRebooting(serviceId);
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        commandsExecutor.rebootService(service, extra -> {
            long pid = (long) extra[0];
            servicesRepository.updateServiceStatus(serviceId, RUNNING.name(), pid);
            serviceEvents.registerServiceRestarted(serviceId, pid);
        });
    }

    public void setServiceInRebooting(String serviceId) {
        servicesRepository.updateServiceStatus(serviceId, REBOOTING.name(), -1);
        serviceEvents.registerServiceRebooted(serviceId);
    }

    public void stopService(BrownieHost host, BrownieHostService service) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        commandsExecutor.stopService(service);
        String serviceId = service.getId();
        setServiceAsStopped(serviceId);
    }

    public void setServiceAsStopped(String serviceId) {
        servicesRepository.updateServiceStatus(serviceId, STOPPED.name(), -1);
        serviceEvents.registerServiceStopped(serviceId);
    }

    public void removeService(BrownieHost host, BrownieHostService service, boolean removeFromTheHost) throws Exception {
        String serviceName = service.getName();
        if (removeFromTheHost) {
            ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
            commandsExecutor.removeService(serviceName, true);
        }
        servicesRepository.removeService(service.getId());
        hostEventsService.registerServiceRemovedEvent(host.getId(), serviceName, removeFromTheHost);
    }

}
