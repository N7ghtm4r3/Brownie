package com.tecknobit.brownie.services.hosts.services;

import com.jcraft.jsch.JSchException;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.brownie.services.hosts.commands.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.commands.WakeOnLanExecutor;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostOverview;
import com.tecknobit.brownie.services.hosts.dtos.BrownieHostStat;
import com.tecknobit.brownie.services.hosts.dtos.usages.CPUUsage;
import com.tecknobit.brownie.services.hosts.dtos.usages.StorageUsage;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.repositories.HostsRepository;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.configuration.IndexesCreator;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import kotlin.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import static com.tecknobit.browniecore.enums.HostStatus.*;
import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class HostsService {

    @Autowired
    private HostsRepository hostsRepository;

    @Autowired
    private HostEventsService eventsService;

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
            ShellCommandsExecutor commandsExecutor = new ShellCommandsExecutor(sshUser, hostAddress, sshPassword);
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

    public void startHost(BrownieHost host) throws IOException {
        // TODO: 01/03/2025 EXECUTE THE WoL
        WakeOnLanExecutor wakeOnLanExecutor = new WakeOnLanExecutor();
        wakeOnLanExecutor.execWoL(host);
        // TODO: 01/03/2025 THEN
        // setOnlineStatus(hostId);
        // TODO: 01/03/2025 RESTART ALL THE SERVICES WHERE autoRun = true
    }

    public void stopHost(BrownieHost host) throws Exception {
        String hostId = host.getId();
        if (host.isRemoteHost()) {
            ShellCommandsExecutor commandsExecutor = new ShellCommandsExecutor(host);
            commandsExecutor.stopHost(() -> {
                setOfflineStatus(hostId);
            });
        }
    }

    public void rebootHost(BrownieHost host) throws Exception {
        String hostId = host.getId();
        if (host.isRemoteHost()) {
            ShellCommandsExecutor commandsExecutor = new ShellCommandsExecutor(host);
            commandsExecutor.rebootHost(() -> {
                setRebootingStatus(hostId);
                waitHostRestarted(host);
            });
        }
    }

    private void waitHostRestarted(BrownieHost host) {
        Executors.newCachedThreadPool().execute(() -> {
            try (Socket socket = new Socket()) {
                int timeout = Math.toIntExact(MINUTES.toMillis(2));
                socket.connect(new InetSocketAddress(host.getHostAddress(), 22), timeout);
                String hostId = host.getId();
                hostsRepository.handleHostStatus(hostId, ONLINE.name());
                eventsService.registerHostRestartedEvent(hostId);
                // TODO: 01/03/2025 RESTART ALL THE SERVICES WHERE autoRun = true
            } catch (IOException e) {
                throw new IllegalStateException("Impossible reach the " + host.getHostAddress() + " address, you need to restart manually as needed");
            }
        });
    }

    @Wrapper
    private void setOnlineStatus(String hostId) {
        handleHostStatus(hostId, ONLINE);
    }

    @Wrapper
    private void setOfflineStatus(String hostId) {
        handleHostStatus(hostId, OFFLINE);
    }

    @Wrapper
    private void setRebootingStatus(String hostId) {
        handleHostStatus(hostId, REBOOTING);
    }

    private void handleHostStatus(String hostId, HostStatus status) {
        hostsRepository.handleHostStatus(hostId, status.name());
        eventsService.registerHostStatusChangedEvent(hostId, status);
    }

    public BrownieHostOverview getHostOverview(BrownieHost brownieHost) throws Exception {
        try {
            ShellCommandsExecutor commandsExecutor = new ShellCommandsExecutor(brownieHost);
            String[] currentHostStats = commandsExecutor.getCurrentHostStats();
            CPUUsage cpuUsage = new CPUUsage(currentHostStats[0], currentHostStats[1]);
            BrownieHostStat memoryUsage = new BrownieHostStat(currentHostStats[2]);
            StorageUsage storageUsage = new StorageUsage(currentHostStats[3], currentHostStats[4]);
            return new BrownieHostOverview(brownieHost, cpuUsage, memoryUsage, storageUsage);
        } catch (JSchException e) {
            if (e.getLocalizedMessage().equals("timeout: socket is not established"))
                return new BrownieHostOverview(brownieHost);
            throw e;
        }
    }

    public void addService(BrownieHost host, String serviceName, JsonHelper hPayload) throws Exception {
        ShellCommandsExecutor commandsExecutor = new ShellCommandsExecutor(host);
        String servicePath = commandsExecutor.findServicePath(serviceName);
        if (servicePath.isEmpty())
            throw new JSchException("Could not locate the " + serviceName);
        System.out.println(servicePath);
    }

}
