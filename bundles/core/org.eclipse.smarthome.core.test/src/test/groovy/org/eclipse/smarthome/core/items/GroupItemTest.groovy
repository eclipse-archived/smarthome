/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.items

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.SwitchItem
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.junit.Assert
import org.junit.Test


/**
 * The GroupItemTest tests functionality of the GroupItem.  
 * 
 * @author Dennis Nobel - Initial contribution
 */
class GroupItemTest {
    
    @Test(expected = UnsupportedOperationException.class)
    void 'assert accepted command types cannot be changed'() {
        new GroupItem("switch").acceptedCommandTypes.clear()
    }
    
    @Test()
    void 'assert acceptedCommandTypes on GroupItems returns subset of command types supported by all members'() {
        
        def switchItem = new SwitchItem("switch")
        def numberItem = new NumberItem("number")
        
        GroupItem groupItem = new GroupItem("group")
        groupItem.addMember(switchItem)
        groupItem.addMember(numberItem)
        
        assertThat groupItem.acceptedCommandTypes, hasItems(RefreshType)
    }
	

    @Test
    public void testGetAllMembers() {
        GroupItem rootGroupItem = new GroupItem("root");
        rootGroupItem.addMember(new TestItem("member1"));
        rootGroupItem.addMember(new TestItem("member2"));
        rootGroupItem.addMember(new TestItem("member2"));
        GroupItem subGroup = new GroupItem("subGroup1");
        subGroup.addMember(new TestItem("subGroup member 1"));
        subGroup.addMember(new TestItem("subGroup member 2"));
        subGroup.addMember(new TestItem("subGroup member 3"));
        subGroup.addMember(new TestItem("member1"));
        rootGroupItem.addMember(subGroup);
        int expectedAmountOfMembers = 5;
        Assert.assertEquals(expectedAmountOfMembers, rootGroupItem.getAllMembers().size());
        for (Item member : rootGroupItem.getAllMembers()) {
            if (member instanceof GroupItem) {
                fail("There are no GroupItems allowed in this Collection");
            }
        }
    }

    class TestItem extends GenericItem {

        public TestItem(String name) {
            super("Test", name);
        }

        @Override
        public List<Class<? extends State>> getAcceptedDataTypes() {
            return null;
        }

        @Override
        public List<Class<? extends Command>> getAcceptedCommandTypes() {
            return null;
        }
    }
}
