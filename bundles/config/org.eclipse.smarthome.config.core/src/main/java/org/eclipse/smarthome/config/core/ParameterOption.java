package org.eclipse.smarthome.config.core;

/**
 * The {@link ParameterOption} specifies one option of a static selection list.
 * <p>
 * A {@link ConfigDescriptionParameter} instance can contain a list of
 * {@link ParameterOption}s to define a static selection list for the parameter
 * value.
 * 
 * @author Alex Tugarev - Initial Contribution
 *
 */
public class ParameterOption {

    private String label;
    private String value;

    public ParameterOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [value=\"" + value + "\", label=\"" + label
                + "\"]";
    }

}
