package org.eclipse.smarthome.core.internal.items;

import java.util.Locale;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
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
import org.eclipse.smarthome.core.types.UnitProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.quantity.Quantities;

@Component(service = ItemStateConverter.class, name = "itemStateConverter")
public class ItemStateConverterImpl implements ItemStateConverter {

    private final Logger logger = LoggerFactory.getLogger(ItemStateConverterImpl.class);

    private UnitProvider unitProvider;

    private LocaleProvider localeProvider;

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
            String unitFromPattern = parseItemUnit(numberItem);
            unitFromPattern = unitFromPattern == null ? "" : unitFromPattern.trim();
            if (StringUtils.isNotBlank(unitFromPattern) && !unitFromPattern.equals("%unit%")) {
                Quantity<?> quantity = null;
                try {
                    quantity = Quantities.getQuantity("1 " + unitFromPattern);
                } catch (IllegalArgumentException e) {
                    // we expect this exception in case the extracted string does not match any known unit
                    logger.warn("Unknown unit from state description pattern: {}", unitFromPattern);
                }
                if (quantity != null) {
                    Unit<?> itemUnit = quantity.getUnit();
                    if (!itemUnit.equals(quantityState.getUnit())) {
                        return quantityState.toUnit(itemUnit);
                    } else {
                        return quantityState;
                    }
                }
            }

            Locale locale = localeProvider.getLocale();
            MeasurementSystem ms = unitProvider.getMeasurementSystem(locale);
            if (quantityState.needsConversion(ms)) {
                Dimension dimension = numberItem.getDimension();

                Unit<?> conversionUnit = quantityState.getConversionUnit(ms);
                if (conversionUnit != null) {
                    // the quantity state knows for itself which unit to convert too.
                    return quantityState.toUnit(conversionUnit);
                } else if (dimension != Dimension.DIMENSIONLESS
                        && dimension.getDefaultUnit().isCompatible(quantityState.getUnit())) {
                    // we do default conversion to the system provided unit for the specific dimension & locale
                    return quantityState.toUnit(unitProvider.getUnit(dimension, locale));
                }

                return quantityState.as(DecimalType.class);
            }

            return quantityState;
        }

        return state;
    }

    /**
     * Extracts the unit from the {@link StateDescription} pattern. We assume the unit is always the last part of the
     * pattern separated by " ".
     *
     * @param numberItem the {@link NumberItem} from which the unit is to be extracted.
     * @return the unit or null.
     */
    private String parseItemUnit(NumberItem numberItem) {
        StateDescription stateDescription = numberItem.getStateDescription();
        if (stateDescription == null) {
            return null;
        }

        String pattern = stateDescription.getPattern();
        if (StringUtils.isBlank(pattern)) {
            return null;
        }

        return pattern.substring(pattern.lastIndexOf(" "));
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

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

}
