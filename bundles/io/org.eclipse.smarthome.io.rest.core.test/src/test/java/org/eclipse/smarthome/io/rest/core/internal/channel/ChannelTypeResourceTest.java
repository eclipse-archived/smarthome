package org.eclipse.smarthome.io.rest.core.internal.channel;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Locale;

import javax.ws.rs.core.Response;

import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeRegistry;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

public class ChannelTypeResourceTest {

    private ChannelTypeResource channelTypeResource;

    @Mock
    private ChannelTypeRegistry channelTypeRegistry;

    @Mock
    private ProfileTypeRegistry profileTypeRegistry;

    @Mock
    private ProfileAdvisor profileAdvisor;

    @Before
    public void setup() {
        initMocks(this);
        channelTypeResource = new ChannelTypeResource();

        channelTypeResource.setChannelTypeRegistry(channelTypeRegistry);
        channelTypeResource.setProfileTypeRegistry(profileTypeRegistry);
        channelTypeResource.addProfileAdvisor(profileAdvisor);
    }

    @Test
    public void getAll_shouldRetrieveAllChannelTypes() throws Exception {
        channelTypeResource.getAll(null);
        verify(channelTypeRegistry).getChannelTypes(any(Locale.class));
    }

    @Test
    public void getProfileType_shouldGetProfileForChannelType() {
        ChannelType channelType = mockChannelType("ct");
        ChannelTypeUID uid = channelType.getUID();
        ProfileTypeUID profileTypeUID = new ProfileTypeUID("system:profileType");

        when(channelTypeRegistry.getChannelType(uid)).thenReturn(channelType);
        when(profileAdvisor.getSuggestedProfileTypeUID(channelType, null)).thenReturn(profileTypeUID);

        ProfileType profileType = mock(ProfileType.class);
        when(profileType.getUID()).thenReturn(profileTypeUID);

        when(profileTypeRegistry.getProfileTypes()).thenReturn(Lists.newArrayList(profileType));

        Response response = channelTypeResource.getAdvicedProfile(uid.getAsString());

        verify(channelTypeRegistry).getChannelType(uid);
        verify(profileAdvisor).getSuggestedProfileTypeUID(channelType, null);
        verify(profileTypeRegistry).getProfileTypes();
        assertThat(response.getStatus(), is(200));
    }

    private ChannelType mockChannelType(String channelId) {
        return new ChannelType(new ChannelTypeUID("binding", channelId), false, "Number", "Label", null, null, null,
                null, null);
    }

}
