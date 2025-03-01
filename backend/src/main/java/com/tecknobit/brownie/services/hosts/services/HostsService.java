package com.tecknobit.brownie.services.hosts.services;

import com.tecknobit.brownie.services.hosts.commands.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.repositories.HostsRepository;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxbackend.configuration.IndexesCreator;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
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
                             String sessionId) {
        hostsRepository.registerHost(hostId, hostName, hostAddress, sshUser, sshPassword, ONLINE.name(), sessionId);
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

    public void startHost(String hostId) throws IOException {
        // TODO: 01/03/2025 EXECUTE THE WoL
        /*WakeOnLanExecutor wakeOnLanExecutor = new WakeOnLanExecutor();
        wakeOnLanExecutor.execWoL("", "");*/
        // TODO: 01/03/2025 THEN
        setOnlineStatus(hostId);
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
                throw new IllegalStateException("Impossible reach the " + host + " address, you need to restart manually as needed");
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

}
