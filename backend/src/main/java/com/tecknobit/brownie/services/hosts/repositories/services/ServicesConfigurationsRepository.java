package com.tecknobit.brownie.services.hosts.repositories.services;

import com.tecknobit.brownie.services.hosts.entities.BrownieHostService.ServiceConfiguration;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;

@Repository
public interface ServicesConfigurationsRepository extends JpaRepository<ServiceConfiguration, String> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "INSERT INTO " + SERVICES_CONFIGURATIONS_KEY + " (" +
                    IDENTIFIER_KEY + "," +
                    AUTO_RUN_AFTER_HOST_REBOOT_KEY + "," +
                    PROGRAM_ARGUMENTS_KEY + "," +
                    PURGE_NOHUP_OUT_AFTER_REBOOT_KEY + "," +
                    SERVICE_IDENTIFIER_KEY + ") VALUES (" +
                    ":" + IDENTIFIER_KEY + "," +
                    ":" + AUTO_RUN_AFTER_HOST_REBOOT_KEY + "," +
                    ":" + PROGRAM_ARGUMENTS_KEY + "," +
                    ":" + PURGE_NOHUP_OUT_AFTER_REBOOT_KEY + "," +
                    ":" + SERVICE_IDENTIFIER_KEY + ")",
            nativeQuery = true
    )
    void storeConfiguration(
            @Param(IDENTIFIER_KEY) String configurationId,
            @Param(AUTO_RUN_AFTER_HOST_REBOOT_KEY) boolean autoRunAfterHostReboot,
            @Param(PROGRAM_ARGUMENTS_KEY) String programArguments,
            @Param(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY) boolean purgeNohupOutAfterReboot,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE " + SERVICES_CONFIGURATIONS_KEY + " SET " +
                    AUTO_RUN_AFTER_HOST_REBOOT_KEY + "=:" + AUTO_RUN_AFTER_HOST_REBOOT_KEY + "," +
                    PROGRAM_ARGUMENTS_KEY + "=:" + PROGRAM_ARGUMENTS_KEY + "," +
                    PURGE_NOHUP_OUT_AFTER_REBOOT_KEY + "=:" + PURGE_NOHUP_OUT_AFTER_REBOOT_KEY +
                    _WHERE_ + SERVICE_IDENTIFIER_KEY + "=:" + SERVICE_IDENTIFIER_KEY,
            nativeQuery = true
    )
    void editConfiguration(
            @Param(AUTO_RUN_AFTER_HOST_REBOOT_KEY) boolean autoRunAfterHostReboot,
            @Param(PROGRAM_ARGUMENTS_KEY) String programArguments,
            @Param(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY) boolean purgeNohupOutAfterReboot,
            @Param(SERVICE_IDENTIFIER_KEY) String serviceId
    );

}
