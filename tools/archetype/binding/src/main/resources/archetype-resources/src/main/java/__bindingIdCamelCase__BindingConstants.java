#set( $dt = $package.getClass().forName("java.util.Date").newInstance() )
#set( $year = $dt.getYear() + 1900 )
#if( $vendorName == "Eclipse.org/SmartHome" )
    #set( $copyright = "Contributors to the Eclipse Foundation" )
#else
    #set( $copyright = "by the respective copyright holders." )
#end
/**
 * Copyright (c) 2014,${year} ${copyright}
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package ${package};

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ${bindingIdCamelCase}BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author ${author} - Initial contribution
 */
 @NonNullByDefault
public class ${bindingIdCamelCase}BindingConstants {

    private static final String BINDING_ID = "${bindingId}";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

}
