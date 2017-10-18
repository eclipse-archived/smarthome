/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item;

import javax.measure.Unit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.types.Dimension;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This {@link BindingConfigReader} is responsible for the "dimension" binding on {@link NumberItem}s. It will set the
 * configured {@link Dimension} and the optional {@link Unit} configuration.
 *
 * @author Henning Treu - initial contribution
 *
 */
@Component(service = BindingConfigReader.class)
public class DimensionBindingConfigProvider implements BindingConfigReader {

    private static final String UNIT = "unit";
    private static final String DIMENSION = "dimension";

    private ItemRegistry itemRegistry;

    @Override
    public String getBindingType() {
        return DIMENSION;
    }

    @Override
    public void validateItemType(String itemType, String bindingConfig) throws BindingConfigParseException {
        if (!itemType.equals("Number")) {
            throw new BindingConfigParseException("The 'dimension' binding is only valid for 'Number' items.");
        }
    }

    @Override
    public void processBindingConfiguration(String context, String itemType, String itemName, String bindingConfig,
            Configuration configuration) throws BindingConfigParseException {
        Item item = itemRegistry.get(itemName);
        if (!(item instanceof NumberItem)) {
            throw new BindingConfigParseException("The 'dimension' binding is only valid for 'Number' items.");
        }

        NumberItem numberItem = (NumberItem) item;
        numberItem.setDimension(Dimension.parse(bindingConfig));
        if (configuration.get(UNIT) != null) {
            String unit = configuration.get(UNIT).toString();
            numberItem.setUnit(unit);
        }
    }

    @Override
    public void startConfigurationUpdate(String context) {
        // nop
    }

    @Override
    public void stopConfigurationUpdate(String context) {
        // nop
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

}
