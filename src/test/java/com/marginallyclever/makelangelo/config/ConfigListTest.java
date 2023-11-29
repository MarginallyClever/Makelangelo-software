package com.marginallyclever.makelangelo.config;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

public class ConfigListTest {
    public ConfigList buildTestList() {
        ConfigList before = new ConfigList();
        // add some double values
        before.add(new ConfigParameter<>("pen_up", 0.0, v-> v>=0.0 && v<=90.0 ));
        before.add(new ConfigParameter<>("pen_down", 0.0, v-> v>=0.0 && v<=90.0 ));
        before.add(new ConfigParameter<>("pen_lift", 0.0, v-> v>=0.0 && v<=90.0 ));
        // add some int values
        before.add(new ConfigParameter<>("pen_delay", 0, v-> v>=0 ));
        before.add(new ConfigParameter<>("pen_up_delay", 0, v-> v>=0 ));
        // add some booleans
        before.add(new ConfigParameter<>("handle_small_segments", false, null));
        before.add(new ConfigParameter<>("is_registered", false, null));
        // add some strings
        before.add(new ConfigParameter<>("port", "COM1", null));
        before.add(new ConfigParameter<>("language", "en", null));
        return before;
    }

    @Test
    public void testSet() {
        ConfigList before = buildTestList();
        // check you cannot initialize with a bad value/predicate pair
        Assertions.assertThrows(InvalidParameterException.class,()->{
            before.add(new ConfigParameter<>("bad", 1, v-> v==0 ));
        });

        // check they were added
        Assertions.assertFalse(before.getKeys().isEmpty());

        // check you cannot set to a bad value
        Assertions.assertFalse(before.getDouble("pen_up").setValue(100.0));
        // check after bad set value did not change
        Assertions.assertEquals(0.0,before.getDouble("pen_up").getValue());

        // check you can set a good value
        before.getDouble("pen_up").setValue(90.0);
        Assertions.assertEquals(90.0,before.getDouble("pen_up").getValue());
    }

    @Test
    public void saveAndLoad() {
        // save
        ConfigList before = buildTestList();
        before.getDouble("pen_up").setValue(90.0);
        JSONObject record = before.save();

        // load
        ConfigList after = buildTestList();
        after.load(record);
        Assertions.assertEquals(before.toString(),after.toString());
        Assertions.assertEquals(90.0,after.getDouble("pen_up").getValue());
    }
}
