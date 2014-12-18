package org.eclipse.smarthome.config.core;

/**
 * The {@link FilterCriteria} specifies a filter for dynamic selection list
 * providers of a {@link ConfigDescriptionParameter}.
 * <p>
 * The {@link FilterCriteria} and its name is related to the context of the
 * containing {@link ConfigDescriptionParameter}.
 * 
 * @author Alex Tugarev - Initial Contribution
 *
 */
public class FilterCriteria {

    private String value;
    private String name;

    public FilterCriteria(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [name=\"" + name + "\", value=\"" + value
                + "\"]";
    }

}
