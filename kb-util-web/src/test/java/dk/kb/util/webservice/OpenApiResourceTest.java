package dk.kb.util.webservice;

import dk.kb.util.webservice.exception.NotFoundServiceException;
import dk.kb.util.yaml.YAML;
import org.junit.jupiter.api.Test;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenApiResourceTest {

    @Test
    public void testGetYamlSpec() throws IOException {
        YAML config = YAML.resolveLayeredConfigs("conf/test.yaml");
        OpenApiResource apiResource = new OpenApiResource();
        OpenApiResource.setConfig(config);
        String yamlSpec = apiResource.getYamlSpec("util-openapi_v1")
                            .getEntity().toString();
        assertFalse(yamlSpec.contains("${config:"));
    }

    @Test
    public void testEnumConstruction() throws IOException {
        YAML config = YAML.resolveLayeredConfigs("conf/test.yaml");
        OpenApiResource apiResource = new OpenApiResource();
        OpenApiResource.setConfig(config);
        String yamlSpec = apiResource.getYamlSpec("util-openapi_v1")
                            .getEntity().toString();
        assertTrue(yamlSpec.contains("enum: [\"a\", \"b\", \"c\"]"));
    }


    @Test
    public void testPathHacking() throws IOException {
        YAML config = YAML.resolveLayeredConfigs("conf/test.yaml");
        OpenApiResource apiResource = new OpenApiResource();
        OpenApiResource.setConfig(config);


        assertThrows(NotFoundServiceException.class, () ->
                apiResource.getYamlSpec("secret/very").getEntity().toString());
    }

    @Test
    public void testGettingConfigPath() throws IOException {
        YAML config = YAML.resolveLayeredConfigs("conf/test.yaml");
        OpenApiResource apiResource = new OpenApiResource();
        OpenApiResource.setConfig(config);

        assertThrows(NotFoundServiceException.class, () ->
                apiResource.getYamlSpec("test")
                        .getEntity().toString());
    }

    @SuppressWarnings("resource")
    @Test
    public void testGetJsonSpec(){
        String jsonSpec = OpenApiResource.createJson("util-openapi_v1")
                .getEntity().toString();

        assertFalse(jsonSpec.contains("${config:"));

    }

    @SuppressWarnings("resource")
    @Test
    public void testGettingConfigJson(){
        assertThrows(NotFoundServiceException.class, () ->
                OpenApiResource.createJson("test")
                        .getEntity().toString());
    }

}
