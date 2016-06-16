package io.pivotal.labs.cfenv;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class TestEnvironment {

    public static Environment withVcapServicesFrom(String jsonResourceName) throws IOException {
        return withVcapServicesFrom(jsonResourceName, Function.identity());
    }

    public static Environment withVcapServicesFrom(String jsonResourceName, Function<String, String> tweak) throws IOException {
        String json = ResourceUtils.loadResource(jsonResourceName);
        String tweakedJson = tweak.apply(json);
        return withVcapServices(tweakedJson);
    }

    static Environment withVcapServicesContainingService(String name, String credentials) {
        String json = String.format("{\"\": [{\"name\": \"%s\", \"credentials\": %s, \"label\": \"\", \"plan\": \"\", \"tags\": []}]}", name, credentials);
        return withVcapServices(json);
    }

    public static Environment withVcapServices(String json) {
        return with("VCAP_SERVICES", json);
    }

    public static Environment with(String name, String value) {
        Map<String, String> environment = Collections.singletonMap(name, value);
        return environment::get;
    }

}
