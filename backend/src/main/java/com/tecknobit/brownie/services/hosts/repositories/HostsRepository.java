package com.tecknobit.brownie.services.hosts.repositories;

import com.tecknobit.brownie.services.hosts.dtos.CurrentHostStatus;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.configuration.IndexesCreator._IN_BOOLEAN_MODE;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;

/**
 * The {@code HostsRepository} interface is useful to manage the queries of the {@link BrownieHost}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 */
@Repository
public interface HostsRepository extends JpaRepository<BrownieHost, String> {

    /**
     * Query used to count the hosts number
     *
     * @param keywords The keywords used as filters
     * @param statuses The statuses of the hosts to count
     * @return the number of the hosts as {@code long}
     */
    @Query(
            value = "SELECT COUNT(*) FROM " + HOSTS_KEY + _WHERE_ +
                    "( " +
                    "MATCH(" + NAME_KEY + "," + HOST_ADDRESS_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''" +
                    ") " +
                    "AND " + STATUS_KEY + " IN (:" + STATUSES_KEY + ")",
            nativeQuery = true
    )
    long countHosts(
            @Param(KEYWORDS_KEY) String keywords,
            @Param(STATUSES_KEY) List<String> statuses
    );

    /**
     * Query used to retrieve the hosts
     *
     * @param keywords The keywords used as filters
     * @param statuses The statuses of the hosts to count
     * @param pageable The parameters to paginate the query
     *
     * @return the list of the hosts as {@link List} of {@link BrownieHost}
     */
    @Query(
            value = "SELECT * FROM " + HOSTS_KEY + _WHERE_ +
                    "( " +
                    "MATCH(" + NAME_KEY + "," + HOST_ADDRESS_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''" +
                    ") " +
                    "AND " + STATUS_KEY + " IN (:" + STATUSES_KEY + ") " +
                    "ORDER BY " + INSERTION_DATE_KEY + " DESC",
            nativeQuery = true
    )
    List<BrownieHost> getHosts(
            @Param(KEYWORDS_KEY) String keywords,
            @Param(STATUSES_KEY) List<String> statuses,
            Pageable pageable
    );

    /**
     * Query used to retrieve the current status of the specified hosts
     *
     * @param currentHosts The identifiers of the hosts to fetch their current status
     *
     * @return the list of the current hosts status as {@link List} of {@link CurrentHostStatus}
     */
    @Query(
            value = "SELECT new com.tecknobit.brownie.services.hosts.dtos.CurrentHostStatus(" +
                    "h." + IDENTIFIER_KEY + ", " +
                    "h." + STATUS_KEY + ") FROM BrownieHost h" + _WHERE_ +
                    "h." + IDENTIFIER_KEY + " IN (:" + HOSTS_KEY + ")"
    )
    List<CurrentHostStatus> getHostsStatus(
            @Param(HOSTS_KEY) List<String> currentHosts
    );

    /**
     * Query used to register a new host
     *
     * @param hostId The identifier of the host
     * @param name The name of the host
     * @param hostAddress The address of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     * @param status The status of the host
     * @param sessionId The identifier of the session owner of the host
     * @param insertionDate The date when the host has been inserted
     * @param broadcastIp The ip address of the remote host network interface
     * @param macAddress The physical mac address of the remote host network interface
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + HOSTS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    NAME_KEY + "," +
                    HOST_ADDRESS_KEY + "," +
                    SSH_USER_KEY + "," +
                    SSH_PASSWORD_KEY + "," +
                    STATUS_KEY + "," +
                    SESSION_IDENTIFIER_KEY + "," +
                    INSERTION_DATE_KEY + "," +
                    BROADCAST_IP_KEY + "," +
                    MAC_ADDRESS_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + NAME_KEY + "," +
                    ":" + HOST_ADDRESS_KEY + "," +
                    ":" + SSH_USER_KEY + "," +
                    ":" + SSH_PASSWORD_KEY + "," +
                    ":" + STATUS_KEY + "," +
                    ":" + SESSION_IDENTIFIER_KEY + "," +
                    ":" + INSERTION_DATE_KEY + "," +
                    ":" + BROADCAST_IP_KEY + "," +
                    ":" + MAC_ADDRESS_KEY + ")",
            nativeQuery = true
    )
    void registerHost(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(NAME_KEY) String name,
            @Param(HOST_ADDRESS_KEY) String hostAddress,
            @Param(SSH_USER_KEY) String sshUser,
            @Param(SSH_PASSWORD_KEY) String sshPassword,
            @Param(STATUS_KEY) String status,
            @Param(SESSION_IDENTIFIER_KEY) String sessionId,
            @Param(INSERTION_DATE_KEY) long insertionDate,
            @Param(BROADCAST_IP_KEY) String broadcastIp,
            @Param(MAC_ADDRESS_KEY) String macAddress
    );

    /**
     * Query used to edit an existing host
     *
     * @param hostId The identifier of the host
     * @param name The name of the host
     * @param hostAddress The address of the host
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + HOSTS_KEY + " SET " +
                    HOST_ADDRESS_KEY + "=:" + HOST_ADDRESS_KEY + "," +
                    NAME_KEY + "=:" + NAME_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editHost(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(NAME_KEY) String name,
            @Param(HOST_ADDRESS_KEY) String hostAddress
    );

    /**
     * Query used to edit an existing host
     *
     * @param hostId The identifier of the host
     * @param name The name of the host
     * @param hostAddress The address of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     * @param broadcastIp The ip address of the remote host network interface
     * @param macAddress The physical mac address of the remote host network interface
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + HOSTS_KEY + " SET " +
                    HOST_ADDRESS_KEY + "=:" + HOST_ADDRESS_KEY + "," +
                    NAME_KEY + "=:" + NAME_KEY + "," +
                    SSH_USER_KEY + "=:" + SSH_USER_KEY + "," +
                    SSH_PASSWORD_KEY + "=:" + SSH_PASSWORD_KEY + "," +
                    BROADCAST_IP_KEY + "=:" + BROADCAST_IP_KEY + "," +
                    MAC_ADDRESS_KEY + "=:" + MAC_ADDRESS_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editHost(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(NAME_KEY) String name,
            @Param(HOST_ADDRESS_KEY) String hostAddress,
            @Param(SSH_USER_KEY) String sshUser,
            @Param(SSH_PASSWORD_KEY) String sshPassword,
            @Param(BROADCAST_IP_KEY) String broadcastIp,
            @Param(MAC_ADDRESS_KEY) String macAddress
    );

    /**
     * Query used to check whether a host belongs to the specified session
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session
     *
     * @return the host as {@link BrownieHost} if belongs, null otherwise
     */
    @Query(
            value = "SELECT * FROM " + HOSTS_KEY + _WHERE_ +
                    IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY +
                    " AND " + SESSION_IDENTIFIER_KEY + "=:" + SESSION_IDENTIFIER_KEY,
            nativeQuery = true
    )
    BrownieHost hostBelongsToSession(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(SESSION_IDENTIFIER_KEY) String sessionId
    );

    /**
     * Query used to handle the current status of the host
     *
     * @param hostId The identifier of the host
     * @param status The current status of the host
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + HOSTS_KEY + " SET " +
                    STATUS_KEY + "=:" + STATUS_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void handleHostStatus(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(STATUS_KEY) String status
    );

    /**
     * Query used to unregister a host from the session
     *
     * @param hostId The identifier of the host to unregister
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "DELETE FROM " + HOSTS_KEY + _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void unregisterHost(
            @Param(IDENTIFIER_KEY) String hostId
    );

}
