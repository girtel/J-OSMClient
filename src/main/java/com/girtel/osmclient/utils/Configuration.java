package com.girtel.osmclient.utils;

import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

/**
 * This class represents Configuration parameters when creating a new VIM
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class Configuration
{
    private JSONObject json;

    /**
     * Default constructor
     */
    public Configuration()
    {
        this.json = new JSONObject();
    }

    /**
     * Adds a new configuration parameter
     * @param param parameter name
     * @param value parameter value. It can be String, Double, Boolean, com.shc.easyjson.JSONObject or com.shc.easyjson.JSONArray
     * @return true if parameter has been added, false if not
     */
    public boolean addConfigurationParameter(String param, Object value)
    {
        if(value == null)
            throw new RuntimeException("Cannot add a parameter with a null value");

        int sizeBeforeAddingParameter = json.size();

        if (value instanceof String)
            json.put(param, new JSONValue((String) value));
        else if(value instanceof Double)
            json.put(param, new JSONValue((Double) value));
        else if(value instanceof Boolean)
            json.put(param, new JSONValue((Boolean) value));
        else if(value instanceof JSONObject)
            json.put(param, new JSONValue((JSONObject) value));
        else if(value instanceof JSONArray)
            json.put(param, new JSONValue((JSONArray) value));
        else
            throw new RuntimeException("Unsupported parameter value type");

        int sizeAfterAddingParameter = json.size();

        return ((sizeBeforeAddingParameter + 1 == sizeAfterAddingParameter) && (json.containsKey(param)) && (json.containsValue(value)));
    }

    @Override
    public String toString()
    {
        return JSON.write(json);
    }
}
