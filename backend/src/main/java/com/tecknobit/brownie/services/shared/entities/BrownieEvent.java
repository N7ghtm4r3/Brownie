package com.tecknobit.brownie.services.shared.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.equinoxcore.annotations.Structure;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import static com.tecknobit.browniecore.ConstantsKt.EVENT_DATE_KEY;

/**
 * The {@code BrownieEvent} class is useful to represent an event related to a Brownie's items
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Structure
@MappedSuperclass
public abstract class BrownieEvent extends EquinoxItem {

    /**
     * {@code eventDate} the date of the event when occurred
     */
    @Column(name = EVENT_DATE_KEY)
    protected final long eventDate;

    /**
     * {@code extra} extra information related to the event
     */
    @Column
    protected final String extra;

    /**
     * Constructor to instantiate the object
     *
     * @apiNote empty constructor required
     */
    public BrownieEvent() {
        this(null, 0, null);
    }

    /**
     * Constructor to instantiate the object
     *
     * @param id        The identifier of the event
     * @param eventDate The date of the event when occurred
     * @param extra     The extra information related to the event
     */
    public BrownieEvent(String id, long eventDate, Object extra) {
        super(id);
        this.eventDate = eventDate;
        String extraValue = null;
        if (extra != null)
            extraValue = extra.toString();
        this.extra = extraValue;
    }

    /**
     * Method to get the {@link #eventDate} instance
     *
     * @return the {@link #eventDate} instance as {@code long}
     */
    @JsonGetter(EVENT_DATE_KEY)
    public long getEventDate() {
        return eventDate;
    }

    /**
     * Method to get the {@link #extra} instance
     *
     * @return the {@link #extra} instance as {@link Object}
     */
    public Object getExtra() {
        return extra;
    }

}
