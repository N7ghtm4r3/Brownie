package com.tecknobit.brownie.services.hosts.service;

import com.tecknobit.brownie.services.hosts.entity.BrownieHost;
import com.tecknobit.brownie.services.hosts.repository.HostsRepository;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.equinoxbackend.configuration.IndexesCreator;
import com.tecknobit.equinoxcore.pagination.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.tecknobit.browniecore.enums.HostStatus.ONLINE;

@Service
public class HostsService {

    @Autowired
    private HostsRepository hostsRepository;

    public PaginatedResponse<BrownieHost> getHosts(Set<String> keywords, List<String> statuses, int page,
                                                   int pageSize) {
        String fullTextKeywords = IndexesCreator.formatFullTextKeywords(keywords, "+", "*", true);
        long totalHosts = hostsRepository.countHosts(fullTextKeywords, statuses);
        List<BrownieHost> hosts = hostsRepository.getHosts(fullTextKeywords, statuses, PageRequest.of(page, pageSize));
        return new PaginatedResponse<>(hosts, page, pageSize, totalHosts);
    }

    public void registerHost(String hostId, String hostName, String hostAddress, String sshUser, String sshPassword,
                             BrownieSession session) {
        hostsRepository.save(new BrownieHost(hostId, hostName, hostAddress, ONLINE, sshUser, sshPassword, session));
    }

}
