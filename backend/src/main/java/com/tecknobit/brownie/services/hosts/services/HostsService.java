package com.tecknobit.brownie.services.hosts.services;

import com.jcraft.jsch.JSchException;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.brownie.helpers.shell.RemoteShellCommandsExecutors;
import com.tecknobit.brownie.helpers.shell.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.commands.WakeOnLanExecutor;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostOverview;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.brownie.services.hosts.dtos.usages.CPUUsage;
import com.tecknobit.brownie.services.hosts.dtos.usages.StorageUsage;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.repositories.HostsRepository;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.configuration.IndexesCreator;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import kotlin.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tecknobit.brownie.helpers.RemoteHostWaiter.waitForHostRestart;
import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.enums.HostStatus.*;

@Service
public class HostsService {

    @Autowired
    private HostsRepository hostsRepository;

    @Autowired
    private HostEventsService eventsService;

    @Autowired
    private HostServicesService servicesService;

    public PaginatedResponse<BrownieHost> getHosts(Set<String> keywords, List<String> statuses, int page,
                                                   int pageSize) {
        String fullTextKeywords = IndexesCreator.formatFullTextKeywords(keywords, "+", "*", true);
        long totalHosts = hostsRepository.countHosts(fullTextKeywords, statuses);
        List<BrownieHost> hosts = hostsRepository.getHosts(fullTextKeywords, statuses, PageRequest.of(page, pageSize));
        return new PaginatedResponse<>(hosts, page, pageSize, totalHosts);
    }

    public void registerHost(String hostId, String hostName, String hostAddress, String sshUser, String sshPassword,
                             String sessionId) throws Exception {
        String macAddress = null;
        String broadcastIp = null;
        if (sshUser != null) {
            RemoteShellCommandsExecutors commandsExecutor = new RemoteShellCommandsExecutors(sshUser, hostAddress,
                    sshPassword);
            Pair<String, String> details = commandsExecutor.getNetworkInterfaceDetails();
            macAddress = details.getFirst();
            broadcastIp = details.getSecond();
        }
        hostsRepository.registerHost(hostId, hostName, hostAddress, sshUser, sshPassword, ONLINE.name(), sessionId,
                broadcastIp, macAddress);
    }

    public void editHost(String hostId, String hostAddress, String hostName, String sshUser, String sshPassword) {
        if (sshUser == null)
            hostsRepository.editHost(hostId, hostAddress, hostName);
        else
            hostsRepository.editHost(hostId, hostAddress, hostName, sshUser, sshPassword);
    }

    public boolean hostBelongsToSession(String sessionId, String hostId) {
        return getBrownieHost(sessionId, hostId) != null;
    }

    public BrownieHost getBrownieHost(String sessionId, String hostId) {
        return hostsRepository.hostBelongsToSession(hostId, sessionId);
    }

    public void startHost(BrownieHost host) throws Exception {
        WakeOnLanExecutor wakeOnLanExecutor = new WakeOnLanExecutor();
        wakeOnLanExecutor.execWoL(host);
        waitForHostRestart(host, new AtomicInteger(0), () -> {
            setOnlineStatus(host);
            handleServicesOnStart(host);
        });
    }

    public void rebootHost(BrownieHost host) throws Exception {
        ShellCommandsExecutor shellCommandsExecutor = ShellCommandsExecutor.getInstance(host);
        shellCommandsExecutor.rebootHost(this, host);
    }

    public void stopHost(BrownieHost host) throws Exception {
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        commandsExecutor.stopHost(this, host);
    }

    public void restartHost(BrownieHost host) throws Exception {
        String hostId = host.getId();
        hostsRepository.handleHostStatus(hostId, ONLINE.name());
        eventsService.registerHostRestartedEvent(hostId);
        handleServicesOnStart(host);
    }

    private void handleServicesOnStart(BrownieHost host) throws Exception {
        for (BrownieHostService service : host.getServices()) {
            if (service.getConfiguration().autoRunAfterHostReboot())
                servicesService.startService(host, service, true);
            else
                servicesService.setServiceAsStopped(service.getId());
        }
    }

    @Wrapper
    public void setOnlineStatus(BrownieHost host) {
        handleHostStatus(host.getId(), ONLINE);
    }

    @Wrapper
    public void setRebootingStatus(BrownieHost host) {
        handleHostStatus(host.getId(), REBOOTING);
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceInRebooting(service.getId());
    }

    @Wrapper
    public void setOfflineStatus(BrownieHost host) {
        handleHostStatus(host.getId(), OFFLINE);
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceAsStopped(service.getId());
    }

    private void handleHostStatus(String hostId, HostStatus status) {
        hostsRepository.handleHostStatus(hostId, status.name());
        eventsService.registerHostStatusChangedEvent(hostId, status);
    }

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

    public void addService(BrownieHost host, String serviceName, JsonHelper hPayload) throws Exception {
        String servicePath = findServicePath(host, serviceName);
        servicesService.storeService(serviceName, servicePath, host.getId(), hPayload.getString(PROGRAM_ARGUMENTS_KEY, ""),
                hPayload.getBoolean(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY),
                hPayload.getBoolean(AUTO_RUN_AFTER_HOST_REBOOT_KEY));
    }

    public void editService(BrownieHost host, String serviceId, String serviceName, JsonHelper hPayload) throws Exception {
        BrownieHostService currentService = host.getService(serviceId);
        String servicePath = currentService.getServicePath();
        if (!currentService.getName().equals(serviceName))
            servicePath = findServicePath(host, serviceName);
        servicesService.editService(serviceId, serviceName, servicePath, hPayload.getString(PROGRAM_ARGUMENTS_KEY, ""),
                hPayload.getBoolean(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY),
                hPayload.getBoolean(AUTO_RUN_AFTER_HOST_REBOOT_KEY));
    }

    private String findServicePath(BrownieHost host, String serviceName) throws Exception {
        String servicePath;
        ShellCommandsExecutor commandsExecutor = ShellCommandsExecutor.getInstance(host);
        servicePath = commandsExecutor.findServicePath(serviceName);
        if (servicePath.isEmpty())
            throw new JSchException("Could not locate the " + serviceName);
        return servicePath;
    }

    public void unregisterHost(String hostId) {
        hostsRepository.unregisterHost(hostId);
    }

}
