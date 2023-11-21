package com.marginallyclever.makelangelo.config;

import java.security.InvalidParameterException;
import java.util.function.Predicate;

/**
 * A key, a value, a type, and predicate validator.
 * @author Dan Royer
 * @since 7.50.3
 */
public class ConfigParameter<T> {
    private final String key;
    private final String type;
    private T value;
    private final Predicate<T> predicate;

    public ConfigParameter(String key, T value, Predicate<T> predicate) throws InvalidParameterException {
        this.key = key;
        this.value = value;
        this.type = value.getClass().getSimpleName();
        this.predicate = predicate;
        if(!isValid(value)) throw new InvalidParameterException("Invalid value for "+key);
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    /**
     * Set the value of this ConfigParameter if it passes the validator.
     * @param value the new value
     * @return true if the value was set.
     */
    public boolean setValue(T value) {
        if(!isValid(value)) return false;
        this.value = value;
        return true;
    }

    /**
     * Compare two ConfigParameters.
     * @param b the other ConfigParameter
     * @return true if the types and values are the same.
     */
    public boolean equals(ConfigParameter<?> b) {
        if(!b.getType().equals(getType())) return false;
        if(getValue()==null && b.getValue()==null) return true;
        return (b.getValue().equals(getValue()));
    }

    public boolean isValid(T arg0) {
        if(predicate ==null) return true;
        return predicate.test(arg0);
    }
}
