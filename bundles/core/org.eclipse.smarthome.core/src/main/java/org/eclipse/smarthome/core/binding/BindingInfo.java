package org.eclipse.smarthome.core.binding;


/**
 * The {@link BindingInfo} is a service interface each <i>Binding</i> has to implement
 * to provide general information. Each <i>Binding</i> has to register its implementation
 * as service at the <i>OSGi</i> service registry.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public interface BindingInfo {

    /**
     * Returns an identifier for the binding (e.g. "hue").
     * 
     * @return an identifier for the binding (must neither be null nor empty)
     */
    String getId();

    /**
     * Returns a human readable name for the binding (e.g. "HUE Binding").
     * 
     * @return a human readable name for the binding (must neither be null nor empty)
     */
    String getName();

    /**
     * Returns a human readable description for the binding
     * (e.g. "Discovers and controls HUE bulbs").
     * 
     * @return a human readable description for the binding (must neither be null nor empty)
     */
    String getDescription();

    /**
     * Returns the author of the binding (e.g. "Max Mustermann").
     * 
     * @return the author of the binding (must neither be null, nor empty)
     */
    String getAuthor();

    /**
     * Returns {@code true} if a link to a concrete {@link ConfigDescription} exists,
     * otherwise {@code false}.
     * 
     * @return true if a link to a concrete ConfigDescription exists, otherwise false
     */
    boolean hasConfigDescriptionURI();

    /**
     * Returns the link to a concrete {@link ConfigDescription}.
     * 
     * @return the link to a concrete ConfigDescription (could be null)
     */
    String getConfigDescriptionURI();

}
