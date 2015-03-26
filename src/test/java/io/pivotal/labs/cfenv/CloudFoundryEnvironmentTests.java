package io.pivotal.labs.cfenv;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CloudFoundryEnvironmentTests {

    @Test
    public void constructionShouldRequireOnlyAnEnvironment() throws Exception {
        Environment environment = environment("VCAP_SERVICES", "{}");

        new CloudFoundryEnvironment(environment);
    }

    @Test(expected = JsonProcessingException.class)
    public void constructionShouldThrowAnExceptionOnAMissingVariable() throws Exception {
        new CloudFoundryEnvironment(environment("NOT_VCAP_SERVICES", "{}"));
    }

    @Test(expected = JsonProcessingException.class)
    public void constructionShouldThrowAnExceptionOnInvalidJson() throws Exception {
        new CloudFoundryEnvironment(environment("VCAP_SERVICES", "<json>ceci n'est pas de JSON</json>"));
    }

    @Test
    public void shouldGetServiceNameForSingleService() throws Exception {
        String json = ResourceUtils.loadResource("system_service.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getName(), equalTo("myapp-db"));
    }

    @Test
    public void shouldGetServiceUriForSingleService() throws Exception {
        String json = ResourceUtils.loadResource("system_service.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getUri(), equalTo("postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm"));
    }

    @Test
    public void shouldGetServiceNameForMultipleServices() throws Exception {
        String json = ResourceUtils.loadResource("multiple_services.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getNameFor("cloudamqp"), equalTo("myapp-mq"));
        assertThat(environment.getNameFor("elephantsql"), equalTo("myapp-db"));
        assertThat(environment.getNameFor("user-provided"), equalTo("search-engine"));
    }

    @Test
    public void shouldGetServiceUriForMultipleServices() throws Exception {
        String json = ResourceUtils.loadResource("multiple_services.json");
        CloudFoundryEnvironment environment = new CloudFoundryEnvironment(environment("VCAP_SERVICES", json));

        assertThat(environment.getUriFor("cloudamqp"), equalTo("amqp://bxgflcee:xxxxxxxx@turtle.rmq.cloudamqp.com/bxgflcee"));
        assertThat(environment.getUriFor("elephantsql"), equalTo("postgres://dxktcwjm:xxxxxxxx@babar.elephantsql.com:5432/dxktcwjm"));
        assertThat(environment.getUriFor("user-provided"), equalTo("https://duckduckgo.com/"));
    }

    private Environment environment(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

}
