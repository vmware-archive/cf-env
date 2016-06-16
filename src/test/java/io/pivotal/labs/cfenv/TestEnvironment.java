package io.pivotal.labs.cfenv;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class TestEnvironment {

    public static Environment withVcapServices(String jsonResourceName) throws IOException {
        return withVcapServices(jsonResourceName, Function.identity());
    }

    public static Environment withVcapServices(String jsonResourceName, Function<String, String> tweak) throws IOException {
        String json = ResourceUtils.loadResource(jsonResourceName);
        String tweakedJson = tweak.apply(json);
        return with("VCAP_SERVICES", tweakedJson);
    }

    public static Environment with(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

}
