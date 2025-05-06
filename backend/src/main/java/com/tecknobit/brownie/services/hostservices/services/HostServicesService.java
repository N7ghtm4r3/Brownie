package com.tecknobit.brownie.services.hostservices.services;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.helpers.shell.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostEventsService;
import com.tecknobit.brownie.services.hostservices.dtos.CurrentServiceStatus;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.repositories.HostServicesRepository;
import com.tecknobit.browniecore.enums.ServiceStatus;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.tecknobit.browniecore.enums.ServiceStatus.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator.formatFullTextKeywords;
import static com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController.generateIdentifier;

/**
 * The {@code HostServicesService} class is useful to manage all the {@link BrownieHostService} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class HostServicesService {

    /**
     * {@code eventsRepository} instance used to access to the {@link SERVICES_KEY} table
     */
    @Autowired
    private HostServicesRepository servicesRepository;

    /**
     * {@code hostEventsService} the support service used to manage the host events data
     */
    @Autowired
    private HostEventsService hostEventsService;

    /**
     * {@code configurationsService} the support service used to manage the service configurations data
     */
    @Autowired
    private ServicesConfigurationsService configurationsService;

    /**
     * {@code serviceEvents} the support service used to manage the service events data
     */
    @Autowired
    private HostServiceEventsService serviceEvents;

    /**
     * Method used to store a new service
     *
     * @param serviceName              The name of the service
     * @param servicePath              The path of the service inside the filesystem of the host
     * @param hostId                   The identifier of the host owner of the service
     * @param programArguments         The program arguments
     * @param purgeNohupOutAfterReboot Whether the {@code nohup.out} file related to the service must be deleted
     *                                 at each service start
     * @param autoRunAfterHostReboot   Whether the service must be automatically restarted after the host start or
     *                                 the host restart
     */
    public void storeService(String serviceName, String servicePath, String hostId, String programArguments,
                             boolean purgeNohupOutAfterReboot, boolean autoRunAfterHostReboot) {
        String serviceId = generateIdentifier();
        servicesRepository.storeService(serviceId, serviceName, STOPPED.name(), System.currentTimeMillis(), hostId,
                servicePath);
        configurationsService.storeConfiguration(serviceId, programArguments, purgeNohupOutAfterReboot, autoRunAfterHostReboot);
        hostEventsService.registerServiceAddedEvent(hostId, serviceName);
    }

    /**
     * Method used to edit an existing service
     *
     * @param serviceId The identifier of the service
     * @param serviceName The name of the service
     * @param servicePath The path of the service inside the filesystem of the host
     * @param programArguments The program arguments
     * @param purgeNohupOutAfterReboot Whether the {@code nohup.out} file related to the service must be deleted
     *                                 at each service start
     * @param autoRunAfterHostReboot Whether the service must be automatically restarted after the host start or
     *                               the host restart
     */
    public void editService(String serviceId, String serviceName, String servicePath, String programArguments,
                            boolean purgeNohupOutAfterReboot, boolean autoRunAfterHostReboot) {
        servicesRepository.editService(serviceId, serviceName, servicePath);
        configurationsService.editConfiguration(serviceId, programArguments, purgeNohupOutAfterReboot,
                autoRunAfterHostReboot);
    }

    /**
     * Method used to get the list of the services related to a host
     *
     * @param hostId The identifier of the host
     * @param keywords    The keywords used to filter the results
     * @param statuses The statuses used to filter the results
     * @param page        The page requested
     * @param pageSize    The size of the items to insert in the page
     * @return the list of the services as {@link PaginatedResponse} of {@link BrownieHostService}
     */
    public PaginatedResponse<BrownieHostService> getServices(String hostId, Set<String> keywords, List<String> statuses,
                                                             int page, int pageSize) {
        String fullTextKeywords = formatFullTextKeywords(keywords, "+", "*", true);
        long totalServices = servicesRepository.countServices(hostId, fullTextKeywords, statuses);
        List<BrownieHostService> services = servicesRepository.getServices(hostId, fullTextKeywords, statuses,
                PageRequest.of(page, pageSize));
        return new PaginatedResponse<>(services, page, pageSize, totalServices);
    }

    /**
     * Method used to get the current status of the specified services
     *
     * @param services The services used to retrieve the current statuses
     * @return the list of the current statuses as {@link List} of {@link CurrentServiceStatus}
     */
    public List<CurrentServiceStatus> getServicesStatus(List<String> services) {
        if (services.isEmpty())
            return Collections.EMPTY_LIST;
        return servicesRepository.getServicesStatus(services);
    }

    /**
     * Method used to start a service
     *
     * @param host The host owner of the service
     * @param service The service to start
     *
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public void startService(BrownieHost host, BrownieHostService service) throws Exception {
        startService(host, service, false);
    }

    /**
     * Method used to start a service
     *
     * @param host The host owner of the service
     * @param service The service to start
     * @param hostRebooted Whether this method has been invoked after the host rebooted
     *
     * @throws Exception when an exception occurred during the process
     */
    public void startService(BrownieHost host, BrownieHostService service, boolean hostRebooted) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        long pid = commandsExecutor.startService(service);
        if (pid == -1)
            throw new JSchException();
        String serviceId = service.getId();
        servicesRepository.updateServiceStatus(serviceId, RUNNING.name(), pid);
        if (hostRebooted)
            serviceEvents.registerServiceRestarted(serviceId, pid);
        else
            serviceEvents.registerServiceStarted(serviceId, pid);
    }

    /**
     * Method used to reboot a service
     *
     * @param host The host owner of the service
     * @param service The service to reboot
     *
     * @throws Exception when an exception occurred during the process
     */
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

    /**
     * Method used to set the {@link ServiceStatus#REBOOTING} status to the specified service
     *
     * @param serviceId The identifier of the service
     */
    public void setServiceInRebooting(String serviceId) {
        servicesRepository.updateServiceStatus(serviceId, REBOOTING.name(), -1);
        serviceEvents.registerServiceRebooted(serviceId);
    }

    /**
     * Method used to stop a service
     *
     * @param host The host owner of the service
     * @param service The service to stop
     *
     * @throws Exception when an exception occurred during the process
     */
    public void stopService(BrownieHost host, BrownieHostService service) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        commandsExecutor.stopService(service);
        String serviceId = service.getId();
        setServiceAsStopped(serviceId);
    }

    /**
     * Method used to set the {@link ServiceStatus#STOPPED} status to the specified service
     *
     * @param serviceId The identifier of the service
     */
    public void setServiceAsStopped(String serviceId) {
        servicesRepository.updateServiceStatus(serviceId, STOPPED.name(), -1);
        serviceEvents.registerServiceStopped(serviceId);
    }

    /**
     * Method used to remove a service
     *
     * @param host The host owner of the service
     * @param service The service to remove
     * @param removeFromTheHost Whether the removing include also the removing from the filesystem of the host
     *
     * @throws Exception when an exception occurred during the process
     */
    public void removeService(BrownieHost host, BrownieHostService service, boolean removeFromTheHost) throws Exception {
        if (removeFromTheHost) {
            ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
            commandsExecutor.removeService(service.getServicePath(), true);
        }
        servicesRepository.removeService(service.getId());
        hostEventsService.registerServiceRemovedEvent(host.getId(), service.getName(), removeFromTheHost);
    }

}
