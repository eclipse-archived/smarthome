/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.library.items.SwitchItem
import org.junit.Test

/**
 * The {@link ItemRegistryOSGiTest} runs inside an OSGi container and tests the {@link ItemRegistry}.  
 * 
 * @author Andre Fuechsel - Initial contribution
 */
class ItemTagTest {
    
    def TAG1 = "tag1"
    def TAG2 = "tag2"
    def TAG3 = "tag3"
    
    def ITEM1 = "item1"
    def ITEM2 = "item2"
    def ITEM3 = "item3"
    
    @Test 
    void 'assert tags are handled correctly'() {
        def item = new SwitchItem(ITEM1)

        assertThat item.getTags().size(), is(0)
        
        item.addTag(TAG1) 
        item.addTag(TAG2)

        assertThat item.getTags().size(), is(2)
        assertThat item.hasTag(TAG1), is(true)
        assertThat item.hasTag(TAG2), is(true)
        assertThat item.hasTag(TAG3), is(false)
        
        item.removeTag(TAG2)        
        assertThat item.getTags().size(), is(1)
        assertThat item.hasTag(TAG1), is(true)
        assertThat item.hasTag(TAG2), is(false)
        assertThat item.hasTag(TAG3), is(false)

        item.removeAllTags()        
        assertThat item.getTags().size(), is(0)
        assertThat item.hasTag(TAG1), is(false)
        assertThat item.hasTag(TAG2), is(false)
        assertThat item.hasTag(TAG3), is(false)
    }
	
}
