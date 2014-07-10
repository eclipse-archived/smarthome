/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ${package};

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ${bindingIdCamelCase}Binding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author ${author} - Initial contribution
 */
public class ${bindingIdCamelCase}BindingConstants {

    public static final String BINDING_ID = "${bindingId}";
    
    // List all Thing Type UIDs, related to the ${bindingIdCamelCase} Binding
    public final static ThingTypeUID SAMPLE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "sample");

}
