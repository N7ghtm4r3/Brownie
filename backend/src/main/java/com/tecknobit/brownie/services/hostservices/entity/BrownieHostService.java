package com.tecknobit.brownie.services.hostservices.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.entities.ServiceEvent;
import com.tecknobit.browniecore.enums.ServiceStatus;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.enums.ServiceStatus.STOPPED;

@Entity
@Table(name = SERVICES_KEY)
public class BrownieHostService extends EquinoxItem {

    @Column
    @Enumerated(value = EnumType.STRING)
    private final ServiceStatus status;

    @Column
    private final String name;

    @Column(
            name = SERVICE_PATH_KEY,
            unique = true
    )
    private final String servicePath;

    @Column(
            columnDefinition = "VARCHAR(30) DEFAULT '-1'",
            insertable = false
    )
    private final long pid;

    @Column(name = INSERTION_DATE_KEY)
    private final long insertionDate;

    @OneToOne(
            mappedBy = SERVICE_KEY,
            cascade = CascadeType.ALL
    )
    private final ServiceConfiguration configuration;

    @OneToMany(
            mappedBy = SERVICE_KEY,
            cascade = CascadeType.ALL
    )
    @JsonIgnoreProperties(SERVICE_KEY)
    @OrderBy(EVENT_DATE_KEY + " DESC")
    private final List<ServiceEvent> events;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(SERVICES_KEY)
    private BrownieHost host;

    public BrownieHostService() {
        this(null, null, null, null, -1, 0, null, List.of());
    }

    public BrownieHostService(String id, ServiceStatus status, String name, String servicePath, long pid,
                              long insertionDate, ServiceConfiguration configuration, List<ServiceEvent> events) {
        super(id);
        this.status = status;
        this.name = name;
        this.servicePath = servicePath;
        this.pid = pid;
        this.insertionDate = insertionDate;
        this.configuration = configuration;
        this.events = events;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getServicePath() {
        return servicePath;
    }

    public long getPid() {
        return pid;
    }

    @JsonGetter(INSERTION_DATE_KEY)
    public long getInsertionDate() {
        return insertionDate;
    }

    public ServiceConfiguration getConfiguration() {
        return configuration;
    }

    public List<ServiceEvent> getEvents() {
        return events;
    }

    @JsonIgnore
    public boolean isStopped() {
        return status == STOPPED;
    }

    @Entity
    @Table(name = SERVICES_CONFIGURATIONS_KEY)
    public static class ServiceConfiguration extends EquinoxItem {

        @Column(
                name = PROGRAM_ARGUMENTS_KEY,
                columnDefinition = "TEXT DEFAULT ''",
                insertable = false
        )
        private final String programArguments;

        @Column(name = PURGE_NOHUP_OUT_AFTER_REBOOT_KEY)
        private final boolean purgeNohupOutAfterReboot;

        @Column(name = AUTO_RUN_AFTER_HOST_REBOOT_KEY)
        private final boolean autoRunAfterHostReboot;

        @OneToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JsonIgnoreProperties(CONFIGURATION_KEY)
        private BrownieHostService service;

        public ServiceConfiguration() {
            this(null, null, false, false);
        }

        public ServiceConfiguration(String id, String programArguments, boolean purgeNohupOutAfterReboot,
                                    boolean autoRunAfterHostReboot) {
            super(id);
            this.programArguments = programArguments;
            this.purgeNohupOutAfterReboot = purgeNohupOutAfterReboot;
            this.autoRunAfterHostReboot = autoRunAfterHostReboot;
        }

        @JsonGetter(PROGRAM_ARGUMENTS_KEY)
        public String getProgramArguments() {
            return programArguments;
        }

        @JsonGetter(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY)
        public boolean isPurgeNohupOutAfterReboot() {
            return purgeNohupOutAfterReboot;
        }

        @JsonGetter(AUTO_RUN_AFTER_HOST_REBOOT_KEY)
        public boolean isAutoRunAfterHostReboot() {
            return autoRunAfterHostReboot;
        }

    }

}