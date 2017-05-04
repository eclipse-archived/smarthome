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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

import ${package}.handler.${bindingIdCamelCase}Handler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
* Tests cases for {@link  ${bindingIdCamelCase}Handler}. The tests provide mocks for supporting entities using Mockito.
*
* @author  ${author} - Initial contribution
*/
public class ${bindingIdCamelCase}HandlerTest {

    private ThingHandler handler;

    private ThingHandlerCallback callback;

    @Before
    public void setUp() {
        handler = new ${bindingIdCamelCase}Handler(mock(Thing.class));

        callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
    }

    @Test
    public void initializeShouldCallTheCallback() {
        handler.initialize();

        Mockito.verify(callback).statusUpdated(eq(handler.getThing()), any(ThingStatusInfo.class));
    }

}
