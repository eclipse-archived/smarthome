package org.eclipse.smarthome.automation.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.dto.ConfigDescriptionParameterDTO;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RuleDTOMapperTest {

    private static final String TEMPLATE_UID = "templateUID";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String TAG1 = "tag1";
    private static final String TAG2 = "tag2";
    private static final String TAG3 = "tag3";

    @Test
    public void shoudlMapRule() {
        RuleDTO dto = RuleDTOMapper.map(getRule());

        assertThat(dto.name, is(NAME));
        assertThat(dto.description, is(DESCRIPTION));
        assertThat(dto.templateUID, is(TEMPLATE_UID));
        assertThat(dto.visibility, is(Visibility.EXPERT));
        assertThat(dto.tags, hasItems(TAG1, TAG2, TAG3));
        assertThat(dto.actions, hasSize(2));
        assertThat(dto.conditions, hasSize(2));
        assertThat(dto.configDescriptions, hasSize(2));
        assertThat(dto.configuration.values(), hasSize(2));
    }

    @Test
    public void shouldMapRuleDTO() {
        Rule rule = RuleDTOMapper.map(getRuleDTO());

        assertThat(rule.getName(), is(NAME));
        assertThat(rule.getDescription(), is(DESCRIPTION));
        assertThat(rule.getTemplateUID(), is(TEMPLATE_UID));
        assertThat(rule.getVisibility(), is(Visibility.EXPERT));
        assertThat(rule.getTags(), hasItems(TAG1, TAG2, TAG3));
        assertThat(rule.getActions(), hasSize(2));
        assertThat(rule.getConditions(), hasSize(2));
        assertThat(rule.getConfigurationDescriptions(), hasSize(2));
        assertThat(rule.getConfiguration().values(), hasSize(2));
    }

    private RuleDTO getRuleDTO() {
        RuleDTO dto = new RuleDTO();
        dto.name = NAME;
        dto.description = DESCRIPTION;
        dto.templateUID = TEMPLATE_UID;
        dto.tags = Sets.newHashSet(TAG1, TAG2, TAG3);
        dto.visibility = Visibility.EXPERT;
        dto.actions = getActionDTOs();
        dto.conditions = getConditionDTOs();
        dto.configDescriptions = getConfigDescriptionDTOs();
        HashMap<String, Object> configMap = Maps.newHashMap();
        configMap.put("key1", "value1");
        configMap.put("key2", BigDecimal.ONE);
        dto.configuration = configMap;
        return dto;
    }

    private List<ConfigDescriptionParameterDTO> getConfigDescriptionDTOs() {
        ConfigDescriptionParameterDTO dto1 = new ConfigDescriptionParameterDTO();
        dto1.name = NAME;
        dto1.type = Type.TEXT;
        ConfigDescriptionParameterDTO dto2 = new ConfigDescriptionParameterDTO();
        dto2.name = NAME;
        dto2.type = Type.DECIMAL;
        return Lists.newArrayList(dto1, dto2);
    }

    private List<ConditionDTO> getConditionDTOs() {
        return Lists.newArrayList(new ConditionDTO(), new ConditionDTO());
    }

    private List<ActionDTO> getActionDTOs() {
        return Lists.newArrayList(new ActionDTO(), new ActionDTO());
    }

    private Rule getRule() {
        Rule rule = new Rule();
        rule.setName(NAME);
        rule.setTags(Sets.newHashSet(TAG1, TAG2, TAG3));
        rule.setVisibility(Visibility.EXPERT);
        rule.setTemplateUID(TEMPLATE_UID);
        rule.setDescription(DESCRIPTION);
        rule.setConfigurationDescriptions(getConfigDescriptions());
        rule.setActions(getActions());
        rule.setConditions(getConditions());
        rule.setConfiguration(getConfiguration());
        rule.setTriggers(getTriggers());

        return rule;
    }

    private List<Trigger> getTriggers() {
        return Lists.newArrayList(new Trigger(), new Trigger());
    }

    private Configuration getConfiguration() {
        Configuration config = new Configuration();
        config.put("key1", "value1");
        config.put("key2", "value2");
        return config;
    }

    private List<Condition> getConditions() {
        return Lists.newArrayList(new Condition(), new Condition());
    }

    private List<Action> getActions() {
        return Lists.newArrayList(new Action(), new Action());
    }

    private List<ConfigDescriptionParameter> getConfigDescriptions() {
        ConfigDescriptionParameter cdp1 = ConfigDescriptionParameterBuilder.create("cdp1", Type.TEXT).build();
        ConfigDescriptionParameter cdp2 = ConfigDescriptionParameterBuilder.create("cdp2", Type.DECIMAL).build();

        return Lists.newArrayList(cdp1, cdp2);
    }
}
