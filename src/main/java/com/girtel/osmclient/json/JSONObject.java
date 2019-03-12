package com.girtel.osmclient.json;

import java.util.HashMap;


public class JSONObject extends HashMap<String, JSONValue>
{
    public JSONObject(){}

    public JSONObject(String text)
    {
        try {
            JSONFactory.parseJSONObject(text, this);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public String toString()
    {
        return JSONFactory.write(this);
    }
}
