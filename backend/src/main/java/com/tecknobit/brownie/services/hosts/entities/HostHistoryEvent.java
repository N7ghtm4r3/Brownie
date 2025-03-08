package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.browniecore.enums.HostEventType;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.HOST_EVENTS_KEY;

/**
 * The {@code HostHistoryEvent} class is useful to represent an event related to a {@link BrownieHost}'s lifecycle
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 * @see BrownieEvent
 */
@Entity
@Table(name = HOST_EVENTS_KEY)
public class HostHistoryEvent extends BrownieEvent {

    /**
     * {@code type} the type of the event occurred
     */
    @Enumerated(EnumType.STRING)
    private final HostEventType type;

    /**
     * {@code host} the host owner of the event
     */
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(HOST_EVENTS_KEY)
    private BrownieHost host;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    public HostHistoryEvent() {
        this(null, null, 0, null);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id        The identifier of the event
     * @param type      The type of the event occurred
     * @param eventDate The date of the event when occurred
     * @param extra     The extra information related to the event
     */
    public HostHistoryEvent(String id, HostEventType type, long eventDate, Object extra) {
        super(id, eventDate, extra);
        this.type = type;
    }

    /**
     * Method to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link HostEventType}
     */
    public HostEventType getType() {
        return type;
    }

}
