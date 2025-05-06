package com.tecknobit.brownie.services.hostservices.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.browniecore.enums.ServiceStatus;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.enums.ServiceStatus.RUNNING;
import static com.tecknobit.browniecore.enums.ServiceStatus.STOPPED;

/**
 * The {@code BrownieHostService} class is useful to represent a Brownie's host service
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = SERVICES_KEY)
public class BrownieHostService extends EquinoxItem {

    /**
     * {@code status} the status of the service
     */
    @Column
    @Enumerated(value = EnumType.STRING)
    private final ServiceStatus status;

    /**
     * {@code name} the name of the service
     */
    @Column
    private final String name;

    /**
     * {@code servicePath} the path of the service inside the filesystem of the host
     */
    @Column(
            name = SERVICE_PATH_KEY,
            unique = true
    )
    private final String servicePath;

    /**
     * {@code pid} the pid of the service
     */
    @Column(
            columnDefinition = "VARCHAR(30) DEFAULT '-1'",
            insertable = false
    )
    private final long pid;

    /**
     * {@code insertionDate} the date when the service has been inserted
     */
    @Column(name = INSERTION_DATE_KEY)
    private final long insertionDate;

    /**
     * {@code configuration} the configuration of the service
     */
    @OneToOne(
            mappedBy = SERVICE_KEY,
            cascade = CascadeType.ALL
    )
    private final ServiceConfiguration configuration;

    /**
     * {@code events} the events related to service lifecycle
     */
    @OneToMany(
            mappedBy = SERVICE_KEY,
            cascade = CascadeType.ALL
    )
    @JsonIgnoreProperties(SERVICE_KEY)
    @OrderBy(EVENT_DATE_KEY + " DESC")
    private final List<ServiceEvent> events;

    /**
     * {@code host} the host owner of the service
     */
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(SERVICES_KEY)
    private BrownieHost host;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public BrownieHostService() {
        this(null, null, null, null, -1, 0, null, List.of());
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id            The identifier of the service
     * @param status        The status of the service
     * @param name          The name of the service
     * @param servicePath   The path of the service inside the filesystem of the host
     * @param pid           The pid of the service
     * @param insertionDate The date when the service has been inserted
     * @param configuration The configuration of the service
     * @param events        The events related to service lifecycle
     */
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

    /**
     * Method to get the {@link #status} instance
     *
     * @return the {@link #status} instance as {@link ServiceStatus}
     */
    public ServiceStatus getStatus() {
        return status;
    }

    /**
     * Method to get the {@link #name} instance
     *
     * @return the {@link #name} instance as {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Method to get the {@link #servicePath} instance
     *
     * @return the {@link #servicePath} instance as {@link String}
     */
    @JsonIgnore
    public String getServicePath() {
        return servicePath;
    }

    /**
     * Method to get the {@link #pid} instance
     *
     * @return the {@link #pid} instance as {@code long}
     */
    public long getPid() {
        return pid;
    }

    /**
     * Method to get the {@link #insertionDate} instance
     *
     * @return the {@link #insertionDate} instance as {@code long}
     */
    @JsonGetter(INSERTION_DATE_KEY)
    public long getInsertionDate() {
        return insertionDate;
    }

    /**
     * Method to get the {@link #configuration} instance
     *
     * @return the {@link #configuration} instance as {@link ServiceConfiguration}
     */
    public ServiceConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Method to get the {@link #events} instance
     *
     * @return the {@link #events} instance as {@link List} of {@link ServiceEvent}
     */
    @JsonGetter(SERVICE_EVENTS_KEY)
    public List<ServiceEvent> getEvents() {
        return events;
    }

    /**
     * Method to check whether the host is currently {@link ServiceStatus#STOPPED}
     *
     * @return whether the host is currently {@link ServiceStatus#STOPPED} as {@code boolean}
     */
    @JsonIgnore
    public boolean isStopped() {
        return status == STOPPED;
    }

    /**
     * Method to check whether the host is currently {@link ServiceStatus#RUNNING}
     *
     * @return whether the host is currently {@link ServiceStatus#RUNNING} as {@code boolean}
     */
    @JsonIgnore
    public boolean isRunning() {
        return status == RUNNING;
    }

    /**
     * The {@code ServiceConfiguration} class is useful to represent the configuration related to a {@link BrownieHostService}
     *
     * @author N7ghtm4r3 - Tecknobit
     * @see EquinoxItem
     */
    @Entity
    @Table(name = SERVICES_CONFIGURATIONS_KEY)
    public static class ServiceConfiguration extends EquinoxItem {

        /**
         * {@code programArguments} the program arguments of the service
         */
        @Column(
                name = PROGRAM_ARGUMENTS_KEY,
                columnDefinition = "TEXT DEFAULT ''",
                insertable = false
        )
        private final String programArguments;

        /**
         * {@code purgeNohupOutAfterReboot} whether the {@code nohup.out} file related to the service must be deleted
         * at each service start
         */
        @Column(name = PURGE_NOHUP_OUT_AFTER_REBOOT_KEY)
        private final boolean purgeNohupOutAfterReboot;

        /**
         * {@code autoRunAfterHostReboot} whether the service must be automatically restarted after the host start or
         * the host restart
         */
        @Column(name = AUTO_RUN_AFTER_HOST_REBOOT_KEY)
        private final boolean autoRunAfterHostReboot;

        /**
         * {@code service} the host owner of the configuration
         */
        @OneToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JsonIgnoreProperties(CONFIGURATION_KEY)
        private BrownieHostService service;

        /**
         * Constructor to instantiate the object
         *
         * @apiNote empty constructor required
         */
        @EmptyConstructor
        public ServiceConfiguration() {
            this(null, null, false, false);
        }

        /**
         * Constructor to instantiate the object
         *
         * @param id The identifier of the configuration
         * @param programArguments The program arguments of the service
         * @param purgeNohupOutAfterReboot Whether the {@code nohup.out} file related to the service must be deleted
         *                                 at each service start
         * @param autoRunAfterHostReboot Whether the service must be automatically restarted after the host start or
         *                               the host restart
         */
        public ServiceConfiguration(String id, String programArguments, boolean purgeNohupOutAfterReboot,
                                    boolean autoRunAfterHostReboot) {
            super(id);
            this.programArguments = programArguments;
            this.purgeNohupOutAfterReboot = purgeNohupOutAfterReboot;
            this.autoRunAfterHostReboot = autoRunAfterHostReboot;
        }

        /**
         * Method to get the {@link #programArguments} instance
         *
         * @return the {@link #programArguments} instance as {@link String}
         */
        @JsonGetter(PROGRAM_ARGUMENTS_KEY)
        public String getProgramArguments() {
            return programArguments;
        }

        /**
         * Method to get the {@link #purgeNohupOutAfterReboot} instance
         *
         * @return the {@link #purgeNohupOutAfterReboot} instance as {@code boolean}
         */
        @JsonGetter(PURGE_NOHUP_OUT_AFTER_REBOOT_KEY)
        public boolean purgeNohupOutAfterReboot() {
            return purgeNohupOutAfterReboot;
        }

        /**
         * Method to get the {@link #autoRunAfterHostReboot} instance
         *
         * @return the {@link #autoRunAfterHostReboot} instance as {@code boolean}
         */
        @JsonGetter(AUTO_RUN_AFTER_HOST_REBOOT_KEY)
        public boolean autoRunAfterHostReboot() {
            return autoRunAfterHostReboot;
        }

    }

}