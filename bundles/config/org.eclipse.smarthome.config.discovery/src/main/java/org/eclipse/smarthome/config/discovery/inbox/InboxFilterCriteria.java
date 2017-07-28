/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery.inbox;

import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link InboxFilterCriteria} specifies the filter for {@link Inbox} <i>GET</i> requests.
 * <p>
 * The according property is filtered in the {@link Inbox} if it's <i>NEITHER</i> {@code null} <i>NOR</i> empty. All
 * specified properties are filtered with an <i>AND</i> operator.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Andre Fuechsel - changed constructors, added the representation property
 *
 * @see Inbox
 */
public final class InboxFilterCriteria {

    private final String bindingId;
    private final ThingTypeUID thingTypeUID;
    private final ThingUID thingUID;
    private final String representationValue;
    private final DiscoveryResultFlag flag;

    private InboxFilterCriteria(String bindingId, ThingTypeUID thingTypeUID, ThingUID thingUID,
            String representationValue, DiscoveryResultFlag flag) {
        super();
        this.bindingId = bindingId;
        this.thingTypeUID = thingTypeUID;
        this.thingUID = thingUID;
        this.representationValue = representationValue;
        this.flag = flag;
    }

    /**
     * Creates a new {@link InboxFilterCriteria}, that filters for binding IDs.
     *
     * @param bindingId the binding ID to be filtered (could be null or empty)
     * @param flag the {@link DiscoveryResultFlag} to be filtered (could be null)
     * @return a new {@link InboxFilterCriteria}
     */
    public static InboxFilterCriteria bindingFilter(String bindingId, DiscoveryResultFlag flag) {
        return new InboxFilterCriteria(bindingId, null, null, null, flag);
    }

    /**
     * Creates a new {@link InboxFilterCriteria}, that filters for thing types.
     *
     * @param thingTypeUID the thing type UID to be filtered (could be null or empty)
     * @param flag the {@link DiscoveryResultFlag} to be filtered (could be null)
     * @return a new {@link InboxFilterCriteria}
     */
    public static InboxFilterCriteria thingTypeFilter(ThingTypeUID thingTypeUID, DiscoveryResultFlag flag) {
        return new InboxFilterCriteria(null, thingTypeUID, null, null, flag);
    }

    /**
     * Creates a new {@link InboxFilterCriteria}, that filters for things.
     *
     * @param thingUID the thing UID to be filtered (could be null or empty)
     * @param flag the {@link DiscoveryResultFlag} to be filtered (could be null)
     * @return a new {@link InboxFilterCriteria}
     */
    public static InboxFilterCriteria thingFilter(ThingUID thingUID, DiscoveryResultFlag flag) {
        return new InboxFilterCriteria(null, null, thingUID, null, flag);
    }

    /**
     * Creates a new {@link InboxFilterCriteria}, that filters for specific representation property values.
     *
     * @param value the of the representation property to be filtered (could be null or empty)
     * @param flag the {@link DiscoveryResultFlag} to be filtered (could be null)
     * @return a new {@link InboxFilterCriteria}
     */
    public static InboxFilterCriteria representationFilter(String value, DiscoveryResultFlag flag) {
        return new InboxFilterCriteria(null, null, null, value, flag);
    }

    /**
     * Creates a new {@link InboxFilterCriteria}, that filters only for the givel {@link DiscoveryResultFlag}.
     *
     * @param flag the {@link DiscoveryResultFlag} to be filtered (could be null)
     * @return a new {@link InboxFilterCriteria}
     */
    public static InboxFilterCriteria resultFlagFilter(DiscoveryResultFlag flag) {
        return new InboxFilterCriteria(null, null, null, null, flag);
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param bindingId the binding ID to be filtered (could be null or empty)
     * @param flag the discovery result flag to be filtered (could be null)
     *
     * @deprecated will be removed with release 1.0
     */
    @Deprecated
    private InboxFilterCriteria(String bindingId, DiscoveryResultFlag flag) {
        this.bindingId = bindingId;
        this.thingTypeUID = null;
        this.thingUID = null;
        this.representationValue = null;
        this.flag = flag;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param thingTypeUID the Thing type UID to be filtered (could be null or empty)
     * @param flag the discovery result flag to be filtered (could be null)
     *
     * @deprecated will be removed with release 1.0
     */
    @Deprecated
    private InboxFilterCriteria(ThingTypeUID thingTypeUID, DiscoveryResultFlag flag) {
        this.bindingId = null;
        this.thingTypeUID = thingTypeUID;
        this.thingUID = null;
        this.representationValue = null;
        this.flag = flag;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param thingUID the Thing UID to be filtered (could be null or empty)
     * @param flag the discovery result flag to be filtered (could be null)
     *
     * @deprecated will be removed with release 1.0
     */
    @Deprecated
    private InboxFilterCriteria(ThingUID thingUID, DiscoveryResultFlag flag) {
        this.bindingId = null;
        this.thingTypeUID = null;
        this.thingUID = thingUID;
        this.representationValue = null;
        this.flag = flag;
    }

    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param flag the discovery result flag to be filtered (could be null)
     *
     * @deprecated will be removed with release 1.0
     */
    @Deprecated
    private InboxFilterCriteria(DiscoveryResultFlag flag) {
        this.bindingId = null;
        this.thingTypeUID = null;
        this.thingUID = null;
        this.representationValue = null;
        this.flag = flag;
    }

    /**
     * Returns the binding ID to be filtered.
     *
     * @return the binding ID to be filtered (could be null or empty)
     */
    public String getBindingId() {
        return this.bindingId;
    }

    /**
     * Returns the {@code Thing} type UID to be filtered.
     *
     * @return the Thing type UID to be filtered (could be null or empty)
     */
    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    /**
     * Returns the {@code Thing} UID to be filtered.
     *
     * @return the Thing UID to be filtered (could be null or empty)
     */
    public ThingUID getThingUID() {
        return this.thingUID;
    }

    /**
     * Return the {@link DiscoveryResultFlag} to be filtered.
     *
     * @return the discovery result flag to be filtered (could be null)
     */
    public DiscoveryResultFlag getFlag() {
        return this.flag;
    }

    /**
     * Return the value of the representation property to be filtered.
     *
     * @return the value of the representation property to be filtered (could be null)
     */
    public String getRepresentationValue() {
        return representationValue;
    }
}
