package com.tecknobit.brownie.services.hosts.services;

import com.jcraft.jsch.JSchException;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.brownie.helpers.shell.RemoteShellCommandsExecutors;
import com.tecknobit.brownie.helpers.shell.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.commands.WakeOnLanExecutor;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostOverview;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.brownie.services.hosts.dtos.CurrentHostStatus;
import com.tecknobit.brownie.services.hosts.dtos.usages.CPUUsage;
import com.tecknobit.brownie.services.hosts.dtos.usages.StorageUsage;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.repositories.HostsRepository;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import kotlin.Pair;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tecknobit.brownie.helpers.RemoteHostWaiter.waitForHostRestart;
import static com.tecknobit.brownie.helpers.RequestParamsConverter.convertToFiltersList;
import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.enums.HostStatus.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator.formatFullTextKeywords;

/**
 * The {@code HostsService} class is useful to manage all the {@link BrownieHost} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class HostsService {

    /**
     * {@code hostsRepository} instance used to access to the {@link HOSTS_KEY} table
     */
    @Autowired
    private HostsRepository hostsRepository;

    /**
     * {@code eventsService} the support service used to manage the host events data
     */
    @Autowired
    private HostEventsService eventsService;

    /**
     * {@code servicesService} the support service used to manage the services data
     */
    @Autowired
    private HostServicesService servicesService;

    /**
     * Method used to get the list of the hosts
     *
     * @param keywords    The keywords used to filter the results
     * @param rawStatuses The statuses used to filter the results
     * @param page        The page requested
     * @param pageSize    The size of the items to insert in the page
     * @return the list of the hosts as {@link PaginatedResponse} of {@link BrownieHost}
     */
    public PaginatedResponse<BrownieHost> getHosts(Set<String> keywords, JSONArray rawStatuses, int page,
                                                   int pageSize) {
        String fullTextKeywords = formatFullTextKeywords(keywords, "+", "*", true);
        List<String> statuses = convertToFiltersList(rawStatuses);
        long totalHosts = hostsRepository.countHosts(fullTextKeywords, statuses);
        List<BrownieHost> hosts = hostsRepository.getHosts(fullTextKeywords, statuses, PageRequest.of(page, pageSize));
        return new PaginatedResponse<>(hosts, page, pageSize, totalHosts);
    }

    /**
     * Method used to get the current status of the specified hosts
     *
     * @param rawHosts The hosts used to retrieve the current statuses
     * @return the list of the current statues as {@link List} of {@link CurrentHostStatus}
     */
    public List<CurrentHostStatus> getHostsStatus(JSONArray rawHosts) {
        List<String> currentHosts = convertToFiltersList(rawHosts);
        if (currentHosts.isEmpty())
            return Collections.EMPTY_LIST;
        return hostsRepository.getHostsStatus(currentHosts);
    }

    /**
     * Method used to register a new host
     *
     * @param hostId The identifier of the host
     * @param hostName The name of the host
     * @param hostAddress The address of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     * @param sessionId The identifier of the session owner of the host
     */
    public void registerHost(String hostId, String hostName, String hostAddress, String sshUser, String sshPassword,
                             String sessionId) throws Exception {
        String macAddress = null;
        String broadcastIp = null;
        if (sshUser != null) {
            Pair<String, String> details = getNetworkInterfaceDetails(sshUser, sshPassword, hostAddress);
            macAddress = details.getFirst();
            broadcastIp = details.getSecond();
        }
        hostsRepository.registerHost(hostId, hostName, hostAddress, sshUser, sshPassword, ONLINE.name(), sessionId,
                System.currentTimeMillis(), broadcastIp, macAddress);
    }

    /**
     * Method used to edit an existing host
     *
     * @param hostId The identifier of the host
     * @param hostAddress The address of the host
     * @param hostName The name of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     */
    public void editHost(String hostId, String hostAddress, String hostName, String sshUser, String sshPassword) throws Exception {
        if (sshUser == null)
            hostsRepository.editHost(hostId, hostName, hostAddress);
        else {
            Pair<String, String> details = getNetworkInterfaceDetails(sshUser, sshPassword, hostAddress);
            String macAddress = details.getFirst();
            String broadcastIp = details.getSecond();
            hostsRepository.editHost(hostId, hostName, hostAddress, sshUser, sshPassword, broadcastIp, macAddress);
        }
    }

    /**
     * Method used to retrieve the network interface details of the remote host
     *
     * @param sshUser The user to use for the SSH connection
     * @param hostAddress The address of the host to reach
     * @param sshPassword The password of the SSH user
     *
     * @return the network interface details as {@link Pair} of {@link String}
     */
    private Pair<String, String> getNetworkInterfaceDetails(String sshUser, String sshPassword, String hostAddress) throws Exception {
        RemoteShellCommandsExecutors commandsExecutor = new RemoteShellCommandsExecutors(sshUser, hostAddress,
                sshPassword);
        return commandsExecutor.getNetworkInterfaceDetails();
    }

    /**
     * Method used to check whether a host belongs to the specified session
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     *
     * @return whether a host belongs to the specified session as {@code boolean}
     */
    public boolean hostBelongsToSession(String sessionId, String hostId) {
        return getBrownieHost(sessionId, hostId) != null;
    }

    /**
     * Method used to check whether a host belongs to the specified session
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     *
     * @return the host as {@link BrownieHost} if belongs, null otherwise
     */
    public BrownieHost getBrownieHost(String sessionId, String hostId) {
        return hostsRepository.hostBelongsToSession(hostId, sessionId);
    }

    /**
     * Method used to start the remote host
     *
     * @param host The remote host to start
     * @throws Exception when an error occurred during the execution
     */
    public void startHost(BrownieHost host) throws Exception {
        WakeOnLanExecutor wakeOnLanExecutor = new WakeOnLanExecutor();
        wakeOnLanExecutor.execWoL(host);
        waitForHostRestart(host, new AtomicInteger(0), () -> {
            setOnlineStatus(host);
            handleServicesOnStart(host);
        });
    }

    /**
     * Method used to reboot a host
     *
     * @param host The host to reboot
     * @throws Exception when an error occurred during the execution
     */
    public void rebootHost(BrownieHost host) throws Exception {
        ShellCommandsExecutor shellCommandsExecutor = ShellCommandsExecutor.getInstance(host);
        shellCommandsExecutor.rebootHost(this, host);
    }

    /**
     * Method used to stop a host
     *
     * @param host The host to stop
     * @throws Exception when an error occurred during the execution
     */
    public void stopHost(BrownieHost host) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        commandsExecutor.stopHost(this, host);
    }

    /**
     * Method used to restart a host after it has been rebooted or stopped
     *
     * @param host The host to restart
     * @throws Exception when an error occurred during the execution
     */
    public void restartHost(BrownieHost host) throws Exception {
        String hostId = host.getId();
        hostsRepository.handleHostStatus(hostId, ONLINE.name());
        eventsService.registerHostRestartedEvent(hostId);
        handleServicesOnStart(host);
    }

    /**
     * Method used to handle the services of the host has been rebooted or stopped
     *
     * @param host The rebooted/stopped host
     * @throws Exception when an error occurred during the execution
     */
    private void handleServicesOnStart(BrownieHost host) throws Exception {
        for (BrownieHostService service : host.getServices()) {
            if (service.getConfiguration().autoRunAfterHostReboot())
                servicesService.startService(host, service, true);
            else
                servicesService.setServiceAsStopped(service.getId());
        }
    }

    /**
     * Method used to set the {@link HostStatus#ONLINE} status of a host
     *
     * @param host The host to set the status
     */
    @Wrapper
    public void setOnlineStatus(BrownieHost host) {
        handleHostStatus(host.getId(), ONLINE);
    }

    /**
     * Method used to set the {@link HostStatus#REBOOTING} status of a host and all its attached services
     *
     * @param host The host to set the status
     */
    @Wrapper
    public void setRebootingStatus(BrownieHost host) {
        handleHostStatus(host.getId(), REBOOTING);
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceInRebooting(service.getId());
    }

    /**
     * Method used to set the {@link HostStatus#OFFLINE} status of a host and all its attached services
     *
     * @param host The host to set the status
     */
    @Wrapper
    public void setOfflineStatus(BrownieHost host) {
        handleHostStatus(host.getId(), OFFLINE);
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceAsStopped(service.getId());
    }

    /**
     * Method used to set the status of a host
     *
     * @param hostId The identifier of the host
     * @param status The status to set
     */
    private void handleHostStatus(String hostId, HostStatus status) {
        hostsRepository.handleHostStatus(hostId, status.name());
        eventsService.registerHostStatusChangedEvent(hostId, status);
    }

    /**
     * Method used to retrieve the current overview of a host
     *
     * @param host The host to retrieve its information
     * @return the overview of the host as {@link BrownieHostOverview}
     *
     * @throws Exception when an error occurred during the execution
     */
    public BrownieHostOverview getHostOverview(BrownieHost host) throws Exception {
        try {
            ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
            String[] currentHostStats = commandsExecutor.getCurrentHostStats();
            CPUUsage cpuUsage = new CPUUsage(currentHostStats[0], currentHostStats[1]);
            BrownieHostStat memoryUsage = new BrownieHostStat(currentHostStats[2]);
            StorageUsage storageUsage = new StorageUsage(currentHostStats[3], currentHostStats[4]);
            return new BrownieHostOverview(host, cpuUsage, memoryUsage, storageUsage);
        } catch (JSchException e) {
            if (e.getLocalizedMessage().equals("timeout: socket is not established"))
                return new BrownieHostOverview(host);
            throw e;
        }
    }

    /**
     * Method used to add a service to a host
     *
     * @param host The host owner of the service
     * @param serviceName The name of the service to add
     * @param hPayload The {@code JSON} payload with the service information
     * @throws Exception when an error occurred during the execution
     */
    public void addService(BrownieHost host, String serviceName, JsonHelper hPayload) throws Exception {
        String servicePath = findServicePath(host, serviceName);
        servicesService.storeService(serviceName, servicePath, host.getId(), hPayload.getString(PROGRAM_ARGUMENTS_KEY, ""),
                hPayload.getBoolean(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY),
                hPayload.getBoolean(AUTO_RUN_AFTER_HOST_REBOOT_KEY));
    }

    /**
     * Method used to edit an existing service of a host
     *
     * @param host The host owner of the service
     * @param serviceId The identifier of the service to edit
     * @param serviceName The name of the service to edit
     * @param hPayload The {@code JSON} payload with the service information
     * @throws Exception when an error occurred during the execution
     */
    public void editService(BrownieHost host, String serviceId, String serviceName, JsonHelper hPayload) throws Exception {
        BrownieHostService currentService = host.getService(serviceId);
        String servicePath = currentService.getServicePath();
        if (!currentService.getName().equals(serviceName))
            servicePath = findServicePath(host, serviceName);
        servicesService.editService(serviceId, serviceName, servicePath, hPayload.getString(PROGRAM_ARGUMENTS_KEY, ""),
                hPayload.getBoolean(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY),
                hPayload.getBoolean(AUTO_RUN_AFTER_HOST_REBOOT_KEY));
    }

    /**
     * Method used to find the path of the service inside the filesystem of the machine
     *
     * @param host The host where find the path of the service
     * @param serviceName The name of the service
     * @return the path of the service inside the filesystem as {@link String}
     * @throws Exception when an error occurred during the execution
     */
    private String findServicePath(BrownieHost host, String serviceName) throws Exception {
        String servicePath;
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        servicePath = commandsExecutor.findServicePath(serviceName);
        if (servicePath.isEmpty())
            throw new JSchException("Could not locate the " + serviceName);
        return servicePath;
    }

    /**
     * Method used to unregister a host from the system
     *
     * @param hostId The identifier of the host to unregister
     */
    public void unregisterHost(String hostId) {
        hostsRepository.unregisterHost(hostId);
    }

}
