package com.tecknobit.brownie.services.session.service;

import com.tecknobit.apimanager.apis.APIRequest;
import com.tecknobit.brownie.events.BrownieApplicationEvent;
import com.tecknobit.brownie.events.BrownieEventsEmitter;
import com.tecknobit.brownie.helpers.shell.ShellCommandsExecutor;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.repository.BrownieSessionsRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM;
import static com.tecknobit.brownie.events.BrownieApplicationEventType.SYNC_SERVICES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The {@code BrownieSessionsService} class is useful to manage all the {@link BrownieSession} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @see BrownieEventsEmitter
 */
@Service
public class BrownieSessionsService extends BrownieEventsEmitter {

    /**
     * {@code LOGGER} is the instance used to log about the events of the monitor-and-sync routine
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BrownieSessionsService.class);

    /**
     * {@code MONITOR_AND_SYNC_DELAY} constant value for the delay between each monitor-and-sync check
     */
    private static final int MONITOR_AND_SYNC_DELAY = 5;

    /**
     * {@code sessionsRepository} instance used to access to the {@link SESSIONS_KEY} table
     */
    private final BrownieSessionsRepository sessionsRepository;

    /**
     * {@code servicesMonitor} pool used to perform the monitor-and-sync routine
     */
    private final ScheduledExecutorService servicesMonitor;

    /**
     * Constructor to instantiate the service
     *
     * @param sessionsRepository the dedicated repository to manage the {@link BrownieSession} entity
     */
    @Autowired
    public BrownieSessionsService(BrownieSessionsRepository sessionsRepository) {
        this.sessionsRepository = sessionsRepository;
        servicesMonitor = Executors.newScheduledThreadPool((int) sessionsRepository.count());
    }

    /**
     * Method automatically invoked after dependency injection by Spring used to perform the monitor-and-sync routine.
     * Each {@link #MONITOR_AND_SYNC_DELAY} for all the session stored will be checked for each host the real status
     * of its services and synced consequentially
     */
    @PostConstruct
    private void monitorAndSyncServiceStatuses() {
        servicesMonitor.scheduleWithFixedDelay(() -> {
            LOGGER.info("Executing the monitor-and-sync routine for all the services");
            List<BrownieSession> sessions = sessionsRepository.findAll();
            for (BrownieSession session : sessions)
                for (BrownieHost host : session.getHosts())
                    monitorServices(host);
        }, 0, MONITOR_AND_SYNC_DELAY, SECONDS);
    }

    /**
     * Method used to perform the monitoring of the services of a host
     *
     * @param host The host to check its services
     */
    private void monitorServices(BrownieHost host) {
        try {
            ShellCommandsExecutor executor = ShellCommandsExecutor.getInstance(host);
            Collection<Long> stoppedServices = executor.detectStoppedServices(host);
            syncServiceStatuses(host, stoppedServices);
        } catch (Exception e) {
            LOGGER.error("Executing the monitor-and-sync routine for the {} host occurred an error",
                    host.getName(), e);
        }
    }

    /**
     * Method used to sync the services of a host
     *
     * @param host            The host owner of the services
     * @param stoppedServices The stopped services to sync
     */
    private void syncServiceStatuses(BrownieHost host, Collection<Long> stoppedServices) {
        if (stoppedServices.isEmpty())
            return;
        BrownieApplicationEvent event = new BrownieApplicationEvent(this, SYNC_SERVICES, host, stoppedServices);
        emitEvent(event);
    }

    /**
     * Method automatically invoked by Spring before termination used to shut down the {@link #servicesMonitor} pool
     */
    @PreDestroy
    private void shutdownServicesMonitor() {
        servicesMonitor.shutdownNow();
    }

    /**
     * Method used to create a new session
     *
     * @param sessionId The identifier of the new created session
     * @param joinCode  The join code to connect to the session
     * @param password  The password used to protect the session accesses
     * @return the instance of the new created session as {@link BrownieSession}
     */
    public BrownieSession createSession(String sessionId, String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        BrownieSession session = new BrownieSession(sessionId, joinCode, password);
        sessionsRepository.save(session);
        return session;
    }

    /**
     * Method used to connect to an existing session
     *
     * @param joinCode The join code to connect to the session
     * @param password The password used to protect the session accesses
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if not authorized
     */
    public BrownieSession connectToSession(String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        return sessionsRepository.validateSessionConnectionAttempt(joinCode, password);
    }

    /**
     * Method to get an existing Brownie's session
     *
     * @param sessionId The identifier of the session to retrieve
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if not exists
     */
    public BrownieSession getBrownieSession(String sessionId) {
        return sessionsRepository.findById(sessionId).orElse(null);
    }

    /**
     * Method to get an existing Brownie's session to validate its deletion
     *
     * @param sessionId The identifier of the session to retrieve
     * @param password The password used to protect the session accesses
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if authorized
     */
    public BrownieSession getBrownieSession(String sessionId, String password) throws NoSuchAlgorithmException {
        BrownieSession session = sessionsRepository.findById(sessionId).orElse(null);
        if (session == null)
            return null;
        password = hash(password);
        if (!session.getPassword().equals(password))
            return null;
        return session;
    }

    /**
     * Method used to delete an existing session
     *
     * @param sessionId The identifier of the session to delete
     */
    public void deleteSession(String sessionId) {
        sessionsRepository.deleteById(sessionId);
    }

    /**
     * Method to hash a sensitive user data
     *
     * @param secret The user value to hash
     * @throws NoSuchAlgorithmException when the hash of the user value fails
     */
    protected String hash(String secret) throws NoSuchAlgorithmException {
        return APIRequest.base64Digest(secret, SHA256_ALGORITHM);
    }

}
