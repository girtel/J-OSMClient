package com.girtel.osmclient.json;

import java.util.ArrayList;


public class JSONArray extends ArrayList<JSONValue>
{
    public JSONArray(){}

    public JSONArray(String text)
    {
        try {
            JSONFactory.parseJSONArray(text, this);
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
