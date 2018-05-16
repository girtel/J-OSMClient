package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

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

    protected OSMComponent(String name, OSMConstants.OSMComponentType osmType)
    {
        this.name = name;
        this.type = osmType;
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

    @Override
    public String toString()
    {
        return this.type + " " + this.name;
    }
}
