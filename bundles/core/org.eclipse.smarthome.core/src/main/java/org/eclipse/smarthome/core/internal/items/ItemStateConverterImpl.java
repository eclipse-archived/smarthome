package org.eclipse.smarthome.core.internal.items;

import javax.measure.Unit;

import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemStateConverter;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.Dimension;
import org.eclipse.smarthome.core.types.MeasurementSystem;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ItemStateConverter.class, name = "itemStateConverter")
public class ItemStateConverterImpl implements ItemStateConverter {

    private final Logger logger = LoggerFactory.getLogger(ItemStateConverterImpl.class);

    private UnitProvider unitProvider;

    @Override
    public State convertToAcceptedState(State state, Item item) {
        if (state == null) {
            logger.error("A conversion of null was requested", new NullPointerException("state should not be null"));
            return UnDefType.NULL;
        }

        if (item != null && !isAccepted(item, state)) {
            for (Class<? extends State> acceptedType : item.getAcceptedDataTypes()) {
                State convertedState = state.as(acceptedType);
                if (convertedState != null) {
                    logger.debug("Converting {} '{}' to {} '{}' for item '{}'", state.getClass().getSimpleName(), state,
                            convertedState.getClass().getSimpleName(), convertedState, item.getName());
                    return convertedState;
                }
            }
        }

        if (item instanceof NumberItem && state instanceof QuantityType) {
            QuantityType quantityState = (QuantityType) state;
            NumberItem numberItem = (NumberItem) item;

            // in case the item does define a unit it takes predescense over all other conversions:
            Unit<?> itemUnit = parseItemUnit(numberItem);
            if (itemUnit != null) {
                if (!itemUnit.equals(quantityState.getUnit())) {
                    return quantityState.toUnit(itemUnit);
                }

                return quantityState;
            }

            MeasurementSystem ms = unitProvider.getMeasurementSystem();
            if (quantityState.needsConversion(ms)) {
                Dimension dimension = numberItem.getDimension();

                Unit<?> conversionUnit = quantityState.getConversionUnit(ms);
                if (conversionUnit != null) {
                    // the quantity state knows for itself which unit to convert too.
                    return quantityState.toUnit(conversionUnit);
                } else if (dimension != Dimension.DIMENSIONLESS
                        && dimension.getDefaultUnit().isCompatible(quantityState.getUnit())) {
                    // we do default conversion to the system provided unit for the specific dimension & locale
                    return quantityState.toUnit(unitProvider.getUnit(dimension));
                }

                return quantityState.as(DecimalType.class);
            }

            return quantityState;
        }

        return state;
    }

    private Unit<?> parseItemUnit(NumberItem numberItem) {
        StateDescription stateDescription = numberItem.getStateDescription();
        if (stateDescription == null) {
            return null;
        }

        String pattern = stateDescription.getPattern();
        return unitProvider.parseUnit(pattern);
    }

    private boolean isAccepted(Item item, State state) {
        return item.getAcceptedDataTypes().contains(state.getClass());
    }

    @Reference
    protected void setUnitProvider(UnitProvider unitProvider) {
        this.unitProvider = unitProvider;
    }

    protected void unsetUnitProvider(UnitProvider unitProvider) {
        this.unitProvider = null;
    }

}
