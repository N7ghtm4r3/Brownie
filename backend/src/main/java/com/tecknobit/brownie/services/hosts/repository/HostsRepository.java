package com.tecknobit.brownie.services.hosts.repository;

import com.tecknobit.brownie.services.hosts.entity.BrownieHost;
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

@Repository
public interface HostsRepository extends JpaRepository<BrownieHost, String> {

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

    @Query(
            value = "SELECT * FROM " + HOSTS_KEY + _WHERE_ +
                    "( " +
                    "MATCH(" + NAME_KEY + "," + HOST_ADDRESS_KEY + ") AGAINST (:" + KEYWORDS_KEY + _IN_BOOLEAN_MODE + ") " +
                    "OR :" + KEYWORDS_KEY + " = ''" +
                    ") " +
                    "AND " + STATUS_KEY + " IN (:" + STATUSES_KEY + ")",
            nativeQuery = true
    )
    List<BrownieHost> getHosts(
            @Param(KEYWORDS_KEY) String keywords,
            @Param(STATUSES_KEY) List<String> statuses,
            Pageable pageable
    );

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
                    SESSION_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + NAME_KEY + "," +
                    ":" + HOST_ADDRESS_KEY + "," +
                    ":" + SSH_USER_KEY + "," +
                    ":" + SSH_PASSWORD_KEY + "," +
                    ":" + STATUS_KEY + "," +
                    ":" + SESSION_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    void registerHost(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(NAME_KEY) String name,
            @Param(HOST_ADDRESS_KEY) String hostAddress,
            @Param(SSH_USER_KEY) String sshUser,
            @Param(SSH_PASSWORD_KEY) String sshPassword,
            @Param(STATUS_KEY) String status,
            @Param(SESSION_IDENTIFIER_KEY) String sessionId
    );

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

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + HOSTS_KEY + " SET " +
                    HOST_ADDRESS_KEY + "=:" + HOST_ADDRESS_KEY + "," +
                    NAME_KEY + "=:" + NAME_KEY + "," +
                    SSH_USER_KEY + "=:" + SSH_USER_KEY + "," +
                    SSH_PASSWORD_KEY + "=:" + SSH_PASSWORD_KEY +
                    _WHERE_ + IDENTIFIER_KEY + "=:" + IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editHost(
            @Param(IDENTIFIER_KEY) String hostId,
            @Param(NAME_KEY) String name,
            @Param(HOST_ADDRESS_KEY) String hostAddress,
            @Param(SSH_USER_KEY) String sshUser,
            @Param(SSH_PASSWORD_KEY) String sshPassword
    );

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

}
