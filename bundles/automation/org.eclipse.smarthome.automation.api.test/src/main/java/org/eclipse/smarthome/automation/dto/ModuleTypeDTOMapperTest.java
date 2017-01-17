package org.eclipse.smarthome.automation.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ModuleTypeDTOMapperTest {

    private static final String UID = "uid";
    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String TAG1 = "tag1";
    private static final String TAG2 = "tag2";
    private static final String TAG3 = "tag3";

    @Test
    public void shouldMapModuleType() {
        ModuleType from = new ActionType(UID, getConfigDescriptions(), LABEL, DESCRIPTION,
                Sets.newHashSet(TAG1, TAG2, TAG3), Visibility.EXPERT, null, null);

        ModuleTypeDTO to = new ActionTypeDTO();
        ModuleTypeDTOMapper.fillProperties(from, to);

        assertThat(to.uid, is(UID));
        assertThat(to.label, is(LABEL));
        assertThat(to.description, is(DESCRIPTION));
        assertThat(to.visibility, is(Visibility.EXPERT));
        assertThat(to.tags, hasItems(TAG1, TAG2, TAG3));
        assertThat(to.configDescriptions, hasSize(2));
    }

    private List<ConfigDescriptionParameter> getConfigDescriptions() {
        ConfigDescriptionParameter cdp1 = ConfigDescriptionParameterBuilder.create("cdp1", Type.TEXT).build();
        ConfigDescriptionParameter cdp2 = ConfigDescriptionParameterBuilder.create("cdp2", Type.DECIMAL).build();

        return Lists.newArrayList(cdp1, cdp2);
    }

}
