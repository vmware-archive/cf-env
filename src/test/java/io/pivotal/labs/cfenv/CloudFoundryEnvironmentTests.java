package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class CloudFoundryEnvironmentTests {

    @Test
    public void shouldRequireOnlyAnEnvironmentToConstruct() throws Exception {
        Environment environment = environment("VCAP_SERVICES", "{}");

        new CloudFoundryEnvironment(environment);
    }

    @Test(expected = JsonProcessingException.class)
    public void shouldThrowAnExceptionOnAMissingVariable() throws Exception {
        new CloudFoundryEnvironment(environment("NOT_VCAP_SERVICES", "{}"));
    }

    @Test(expected = JsonProcessingException.class)
    public void shouldThrowAnExceptionOnInvalidJson() throws Exception {
        new CloudFoundryEnvironment(environment("VCAP_SERVICES", "<json>ceci n'est pas de JSON</json>"));
    }

    @Test
    public void shouldGetServiceName() throws Exception {
        String json = ResourceUtils.loadResource("system_service.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getName(), equalTo("myapp-db"));
    }

    @Test
    public void shouldGetServiceUri() throws Exception {
        String json = ResourceUtils.loadResource("system_service.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getUri(), equalTo("postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm"));
    }

    private Environment environment(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

}
