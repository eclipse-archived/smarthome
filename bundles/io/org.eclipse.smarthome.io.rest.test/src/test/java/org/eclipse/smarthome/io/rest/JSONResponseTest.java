package org.eclipse.smarthome.io.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.google.gson.JsonObject;

public class JSONResponseTest {

    private static final String ENTITY_VALUE = "entityValue";
    private static final String ENTITY_JSON_VALUE = "\"" + ENTITY_VALUE + "\"";

    @Test
    public void creatErrorShouldCreateErrorResponse() {
        Response errorResponse = JSONResponse.createErrorResponse(Status.INTERNAL_SERVER_ERROR, "error");

        assertThat(errorResponse.getMediaType(), is(equalTo(MediaType.APPLICATION_JSON_TYPE)));
        assertThat(errorResponse.getStatus(), is(500));

        JsonObject entity = ((JsonObject) errorResponse.getEntity()).get(JSONResponse.JSON_KEY_ERROR).getAsJsonObject();
        assertThat(entity.get(JSONResponse.JSON_KEY_ERROR_MESSAGE).getAsString(), is("error"));
        assertThat(entity.get(JSONResponse.JSON_KEY_HTTPCODE).getAsInt(), is(500));
    }

    @Test
    public void createMessageWithErrorStatusShouldCreateErrorResponse() {
        Response errorResponse = JSONResponse.createResponse(Status.INTERNAL_SERVER_ERROR, null, "error");

        assertThat(errorResponse.getMediaType(), is(equalTo(MediaType.APPLICATION_JSON_TYPE)));
        assertThat(errorResponse.getStatus(), is(500));

        JsonObject entity = ((JsonObject) errorResponse.getEntity()).get(JSONResponse.JSON_KEY_ERROR).getAsJsonObject();
        assertThat(entity.get(JSONResponse.JSON_KEY_ERROR_MESSAGE).getAsString(), is("error"));
        assertThat(entity.get(JSONResponse.JSON_KEY_HTTPCODE).getAsInt(), is(500));
        assertThat(entity.get(JSONResponse.JSON_KEY_ENTITY), is(nullValue()));
    }

    @Test
    public void createMessageWithErrorStatusShouldCreateErrorResponseWithEntity() {
        Response errorResponse = JSONResponse.createResponse(Status.INTERNAL_SERVER_ERROR, ENTITY_VALUE, "error");

        assertThat(errorResponse.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));
        assertThat(errorResponse.getStatus(), is(500));

        JsonObject resultJson = (JsonObject) errorResponse.getEntity();
        assertThat(resultJson.get(JSONResponse.JSON_KEY_ENTITY).getAsString(), is(ENTITY_VALUE));

        JsonObject errorJson = resultJson.get(JSONResponse.JSON_KEY_ERROR).getAsJsonObject();
        assertThat(errorJson.get(JSONResponse.JSON_KEY_ERROR_MESSAGE).getAsString(), is("error"));
        assertThat(errorJson.get(JSONResponse.JSON_KEY_HTTPCODE).getAsInt(), is(500));
    }

    @Test
    public void shouldCreateSuccessResponseWithStreamEntity() throws IOException {
        Response response = JSONResponse.createResponse(Status.OK, ENTITY_VALUE, null);

        assertThat(response.getStatus(), is(200));
        assertThat(response.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));

        Object entity = response.getEntity();
        assertThat(entity.getClass(), is(typeCompatibleWith(InputStream.class)));

        try (InputStream entityInStream = (InputStream) entity) {
            byte[] entityValue = new byte[ENTITY_JSON_VALUE.length()];
            entityInStream.read(entityValue);
            assertThat(new String(entityValue), is(ENTITY_JSON_VALUE));
        }
    }
}
