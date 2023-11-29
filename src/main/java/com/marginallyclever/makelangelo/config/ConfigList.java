package com.marginallyclever.makelangelo.config;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ConfigList {
    private final List<ConfigParameter<?>> list = new ArrayList<>();

    /**
     * Save values from this list into a {@link JSONObject}.
     * @return the JSONObject containing the values
     */
    public JSONObject save() {
        JSONObject destination = new JSONObject();
        for(ConfigParameter<?> p : list) {
            destination.put(p.getKey(),p.getValue());
        }
        return destination;
    }

    /**
     * Load values from a {@link JSONObject} into this list.
     * @param source the source of the values
     * @throws InvalidParameterException if the key is not found or the type is unknown.
     */
    @SuppressWarnings("unchecked")
    public void load(JSONObject source) throws InvalidParameterException {
        for (ConfigParameter<?> p : list) {
            if (source.has(p.getKey())) {
                String type = p.getType();
                switch (type) {
                    case "Double"  -> ((ConfigParameter<Double> )p).setValue(source.getDouble(p.getKey()));
                    case "Integer" -> ((ConfigParameter<Integer>)p).setValue(source.getInt(p.getKey()));
                    case "Boolean" -> ((ConfigParameter<Boolean>)p).setValue(source.getBoolean(p.getKey()));
                    case "String"  -> ((ConfigParameter<String> )p).setValue(source.getString(p.getKey()));
                    default -> throw new InvalidParameterException("Unknown type: " + type);
                }
            }
        }
    }

    /**
     * Add a {@link ConfigParameter} to the list.
     * @param param the parameter to add
     */
    public void add(ConfigParameter<?> param) {
        list.add(param);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(ConfigParameter<?> p : list) {
            sb.append(p.getKey());
            sb.append("=");
            sb.append(p.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get a {@link ConfigParameter} from the list.  This is the most generic form.
     * @param key the key to search for
     * @return the value
     * @throws InvalidParameterException if the key is not found
     */
    public ConfigParameter<?> get(String key) throws InvalidParameterException {
        for(ConfigParameter<?> p : list) {
            if(p.getKey().equals(key)) return p;
        }
        throw new InvalidParameterException("No such key: "+key);
    }

    /**
     * Get a double value from the list.
     * @param key the key to search for
     * @return the value
     * @throws InvalidParameterException if the key is not found or the value is not a double
     */
    @SuppressWarnings("unchecked")
    public ConfigParameter<Double> getDouble(String key) throws InvalidParameterException {
        ConfigParameter<?> p = get(key);
        if(!p.getType().equals("Double")) throw new InvalidParameterException("key is not Double: "+key);
        return (ConfigParameter<Double>)p;
    }

    /**
     * Get an integer value from the list.
     * @param key the key to search for
     * @return the value
     * @throws InvalidParameterException if the key is not found or the value is not an integer
     */
    @SuppressWarnings("unchecked")
    public ConfigParameter<Integer> getInteger(String key) throws InvalidParameterException {
        ConfigParameter<?> p = get(key);
        if(!p.getType().equals("Integer")) throw new InvalidParameterException("key is not Integer: "+key);
        return (ConfigParameter<Integer>)p;
    }

    /**
     * Get a boolean value from the list.
     * @param key the key to search for
     * @return the value
     * @throws InvalidParameterException if the key is not found or the value is not a boolean
     */
    @SuppressWarnings("unchecked")
    public ConfigParameter<Boolean> getBoolean(String key) throws InvalidParameterException {
        ConfigParameter<?> p = get(key);
        if(!p.getType().equals("Boolean")) throw new InvalidParameterException("key is not Boolean: "+key);
        return (ConfigParameter<Boolean>)p;
    }

    /**
     * Get a string value from the list.
     * @param key the key to search for
     * @return the value
     * @throws InvalidParameterException if the key is not found or the value is not a string
     */
    @SuppressWarnings("unchecked")
    public ConfigParameter<String> getString(String key) throws InvalidParameterException {
        ConfigParameter<?> p = get(key);
        if(!p.getType().equals("String")) throw new InvalidParameterException("key is not String: "+key);
        return (ConfigParameter<String>)p;
    }

    /**
     * Get a list of all keys in this list.
     * @return a list of all keys in this list
     */
    public List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        for(ConfigParameter<?> p : list) {
            keys.add(p.getKey());
        }
        return keys;
    }
}
