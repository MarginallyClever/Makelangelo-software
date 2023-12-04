package com.marginallyclever.makelangelo.config;

import com.marginallyclever.makelangelo.select.*;

public class ConfigPanel extends SelectPanel {
    private final ConfigList configList;

    public ConfigPanel(ConfigList configList) {
        super();
        this.configList = configList;

        for(String key : configList.getKeys()) {
            ConfigParameter<?> parameter = configList.get(key);
            Select select;
            switch(parameter.getType()) {
                case "Double"  :
                    select = new SelectDouble(parameter.getKey(), parameter.getKey(), (Double)parameter.getValue() );
                    select.addSelectListener(e -> ((ConfigParameter<Double>)parameter).setValue((double)e.getNewValue()));
                    break;
                case "Integer" :
                    select = new SelectInteger(parameter.getKey(), parameter.getKey(), (Integer)parameter.getValue() );
                    select.addSelectListener(e -> ((ConfigParameter<Integer>)parameter).setValue((int)e.getNewValue()));
                    break;
                case "Boolean" :
                    select = new SelectBoolean(parameter.getKey(), parameter.getKey(), (Boolean)parameter.getValue() );
                    select.addSelectListener(e -> ((ConfigParameter<Boolean>)parameter).setValue((boolean)e.getNewValue()));
                    break;
                case "String"  :
                    select = new SelectTextField(parameter.getKey(), parameter.getKey(), (String)parameter.getValue() );
                    select.addSelectListener(e -> ((ConfigParameter<String>)parameter).setValue((String)e.getNewValue()));
                    break;
                default : throw new RuntimeException("Unknown type: " + parameter.getType());
            };
            select.setName(parameter.getKey());
            add(select);
        }
    }
}
