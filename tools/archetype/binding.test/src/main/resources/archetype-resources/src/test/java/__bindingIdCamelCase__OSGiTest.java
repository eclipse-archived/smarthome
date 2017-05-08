#set( $dt = $package.getClass().forName("java.util.Date").newInstance() )
#set( $year = $dt.getYear() + 1900 )
/**
 * Copyright (c) 2010-${year} by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ${package};

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import ${package}.handler.${bindingIdCamelCase}Handler;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.Before;
import org.junit.Test;

/**
* Tests cases for {@link  ${bindingIdCamelCase}Handler}.
*
* @author  ${author} - Initial contribution
*/
public class ${bindingIdCamelCase}OSGiTest extends JavaOSGiTest {

    private final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("${bindingId}", "bridge");

    private ManagedThingProvider managedThingProvider;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    @Before
    public void setUp() {
        registerService(volatileStorageService);
        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));
    }

    @Test
    public void creationOf${bindingIdCamelCase}Handler() {
        ${bindingIdCamelCase}Handler handler = getService(ThingHandler.class,${bindingIdCamelCase}Handler.class);

        assertThat(handler, is(nullValue()));
    }

}
