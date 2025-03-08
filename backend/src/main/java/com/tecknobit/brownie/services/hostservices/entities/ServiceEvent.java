package com.tecknobit.brownie.services.hostservices.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.browniecore.enums.ServiceEventType;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.SERVICE_EVENTS_KEY;

/**
 * The {@code ServiceEvent} class is useful to represent an event related to a {@link HostsService}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 * @see BrownieEvent
 */
@Entity
@Table(name = SERVICE_EVENTS_KEY)
public class ServiceEvent extends BrownieEvent {

    /**
     * {@code type} the type of the event occurred
     */
    @Column
    @Enumerated(value = EnumType.STRING)
    private final ServiceEventType type;

    /**
     * {@code service} the service owner of the event
     */
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(SERVICE_EVENTS_KEY)
    private BrownieHostService service;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    public ServiceEvent() {
        this(null, 0, null, null);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id        The identifier of the event
     * @param eventDate The date of the event when occurred
     * @param extra     The extra information related to the event
     * @param type      The type of the event occurred
     */
    public ServiceEvent(String id, long eventDate, Object extra, ServiceEventType type) {
        super(id, eventDate, extra);
        this.type = type;
    }

    /**
     * Method to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link ServiceEventType}
     */
    public ServiceEventType getType() {
        return type;
    }

}
