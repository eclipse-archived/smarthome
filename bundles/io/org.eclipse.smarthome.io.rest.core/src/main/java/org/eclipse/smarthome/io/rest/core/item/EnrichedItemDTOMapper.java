/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.item;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.items.dto.ItemDTOMapper;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.io.rest.core.internal.RESTCoreActivator;
import org.eclipse.smarthome.io.rest.core.internal.item.ItemResource;

/**
 * The {@link EnrichedItemDTOMapper} is a utility class to map items into enriched item data transform objects (DTOs).
 *
 * @author Dennis Nobel - Initial contribution
 * @author Jochen Hiller - Fix #473630 - handle optional dependency to TransformationHelper
 */
public class EnrichedItemDTOMapper {

    /**
     * Maps item into enriched item DTO object.
     *
     * @param item the item
     * @param drillDown the drill down
     * @param uri the uri
     * @return item DTO object
     */
    public static EnrichedItemDTO map(Item item, boolean drillDown, URI uri, Locale locale) {
        ItemDTO itemDTO = ItemDTOMapper.map(item);
        return map(item, itemDTO, uri, drillDown, locale);
    }

    private static EnrichedItemDTO map(Item item, ItemDTO itemDTO, URI uri, boolean drillDown, Locale locale) {

        String state = item.getState().toFullString();
        String transformedState = considerTransformation(state, item.getStateDescription(locale));
        if (transformedState != null && transformedState.equals(state)) {
            transformedState = null;
        }
        StateDescription stateDescription = considerTransformation(item.getStateDescription(locale));
        String link = null != uri ? uri.toASCIIString() + ItemResource.PATH_ITEMS + "/" + itemDTO.name : null;

        EnrichedItemDTO enrichedItemDTO = null;

        if (item instanceof GroupItem) {
            GroupItem groupItem = (GroupItem) item;
            EnrichedItemDTO[] memberDTOs;
            if (drillDown) {
                Collection<EnrichedItemDTO> members = new LinkedHashSet<>();
                for (Item member : groupItem.getMembers()) {
                    members.add(map(member, drillDown, uri, locale));
                }
                memberDTOs = members.toArray(new EnrichedItemDTO[members.size()]);
            } else {
                memberDTOs = new EnrichedItemDTO[0];
            }
            enrichedItemDTO = new EnrichedGroupItemDTO(itemDTO, memberDTOs, link, state, transformedState,
                    stateDescription);
        } else {
            enrichedItemDTO = new EnrichedItemDTO(itemDTO, link, state, transformedState, stateDescription);
        }

        return enrichedItemDTO;
    }

    private static StateDescription considerTransformation(StateDescription desc) {
        if (desc == null || desc.getPattern() == null) {
            return desc;
        } else {
            try {
                return TransformationHelper.isTransform(desc.getPattern()) ? new StateDescription(desc.getMinimum(),
                        desc.getMaximum(), desc.getStep(), "", desc.isReadOnly(), desc.getOptions()) : desc;
            } catch (NoClassDefFoundError ex) {
                // TransformationHelper is optional dependency, so ignore if class not found
                // return state description as it is without transformation
                return desc;
            }
        }
    }

    private static String considerTransformation(String state, StateDescription stateDescription) {
        if (stateDescription != null && stateDescription.getPattern() != null && state != null) {
            try {
                return TransformationHelper.transform(RESTCoreActivator.getBundleContext(),
                        stateDescription.getPattern(), state.toString());
            } catch (NoClassDefFoundError ex) {
                // TransformationHelper is optional dependency, so ignore if class not found
                // return state as it is without transformation
                return state;
            }
        } else {
            return state;
        }
    }
}
