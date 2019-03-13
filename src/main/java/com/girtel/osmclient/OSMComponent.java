package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents an abstract object to model OSM Components.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class OSMComponent
{
    private String name;
    private OSMConstants.OSMComponentType type;
    private List<String> tags;
    private Map<String, String> attributes;

    protected OSMComponent(String name, OSMConstants.OSMComponentType osmType)
    {
        this.name = name;
        this.type = osmType;
        this.tags = new LinkedList<>();
        this.attributes = new LinkedHashMap<>();
    }

    /**
     * Obtains OSM Component name
     * @return OSM Component name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Obtains OSM Component type
     * @return OSM Component type
     */
    public OSMConstants.OSMComponentType getOSMComponentType()
    {
        return type;
    }

    /**
     * Adds a new tag to this component
     * @param tag tag to add
     */
    public void addTag(String tag)
    {
        if(tags.contains(tag))
            return;
        tags.add(tag);
    }

    /**
     * Checks if this component has a specific tag
     * @param tag tag to check
     * @return true if this component has the tag, false if not
     */
    public boolean hasTag(String tag)
    {
        return tags.contains(tag);
    }

    /**
     * Obtains all tags associated to this component
     * @return list of tags
     */
    public List<String> getTags()
    {
        return this.tags;
    }

    /**
     * Adds a new attribute to this component
     * @param attribute attribute name
     * @param value attribute value
     */
    public void addAttribute(String attribute, String value)
    {
        this.attributes.put(attribute, value);
    }

    /**
     * Obtains the value of a specific attribute
     * @param attribute attribute name
     * @return attribute value
     */
    public String getAttribute(String attribute)
    {
        return this.attributes.get(attribute);
    }

    /**
     * Obtains the list of attribute associated to this component
     * @return list of attributes
     */
    public Map<String, String> getAttributes()
    {
        return this.attributes;
    }

    @Override
    public String toString()
    {
        return this.type + " " + this.name;
    }
}
