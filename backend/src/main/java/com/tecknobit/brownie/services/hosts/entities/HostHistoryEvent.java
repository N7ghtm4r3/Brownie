package com.tecknobit.brownie.services.hosts.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.browniecore.enums.HostEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static com.tecknobit.browniecore.ConstantsKt.HOST_EVENTS_KEY;

@Entity
@Table(name = HOST_EVENTS_KEY)
public class HostHistoryEvent extends BrownieEvent {

    @Enumerated(EnumType.STRING)
    private final HostEventType type;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties(HOST_EVENTS_KEY)
    private BrownieHost host;

    public HostHistoryEvent() {
        this(null, null, 0, null);
    }

    public HostHistoryEvent(String id, HostEventType type, long eventDate, Object extra) {
        super(id, eventDate, extra);
        this.type = type;
    }

    public HostEventType getType() {
        return type;
    }

}
