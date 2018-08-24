package org.eclipse.smarthome.binding.homematic.internal.communicator.client;

import static org.eclipse.smarthome.binding.homematic.test.util.DimmerHelper.createDimmerDummyChannel;
import static org.eclipse.smarthome.binding.homematic.test.util.DimmerHelper.createDimmerHmChannel;
import static org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl.GET_PARAMSET_DESCRIPTION_NAME;
import static org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl.GET_PARAMSET_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmParamsetType;
import org.eclipse.smarthome.binding.homematic.test.util.RpcClientMockImpl;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;

public class RpcClientTest extends JavaTest {

    private RpcClientMockImpl rpcClient;

    @Before
    public void setup() throws IOException {
        this.rpcClient = new RpcClientMockImpl();
    }

    @Test
    public void valuesParamsetDescriptionIsLoadedForChannel() throws IOException {
        HmChannel channel = createDimmerHmChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(1));
    }

    @Test
    public void masterParamsetDescriptionIsLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.MASTER);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(1));
    }

    @Test
    public void valuesParamsetDescriptionIsNotLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.addChannelDatapoints(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_DESCRIPTION_NAME), is(0));
    }

    @Test
    public void valuesParamsetIsLoadedForChannel() throws IOException {
        HmChannel channel = createDimmerHmChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(1));
    }

    @Test
    public void masterParamsetIsLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.MASTER);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(1));
    }

    @Test
    public void valuesParamsetIsNotLoadedForDummyChannel() throws IOException {
        HmChannel channel = createDimmerDummyChannel();

        rpcClient.setChannelDatapointValues(channel, HmParamsetType.VALUES);

        assertThat(rpcClient.numberOfCalls.get(GET_PARAMSET_NAME), is(0));
    }

}
