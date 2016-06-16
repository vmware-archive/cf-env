package io.pivotal.labs.cfenv;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class EntriesMatcher {

    public static <K, V> Matcher<Map<K, V>> entries(Matcher<Iterable<? extends Map.Entry<K, V>>> matcher) {
        return new FeatureMatcher<Map<K, V>, Set<Map.Entry<K, V>>>(matcher, "entries", "entries") {
            @Override
            protected Set<Map.Entry<K, V>> featureValueOf(Map<K, V> actual) {
                return actual.entrySet();
            }
        };
    }

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
    }

}
