package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

/**
 * This class represents Open Source MANO VIM component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class VirtualInfrastructureManager extends OSMComponent{

    private String name, uuid, url, type;

    /**
     * Constructor
     * @param name VIM name
     * @param uuid VIM ID
     * @param url VIM authentication URL
     * @param type VIM Type
     */
    protected VirtualInfrastructureManager(String name, String uuid, String url, String type)
    {
        super(name, OSMConstants.OSMComponentType.VIM);
        this.name = name;
        this.uuid = uuid;
        this.url = url;
        this.type = type;
    }

    /**
     * Gets VIM name
     * @return VIM name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets VIM id
     * @return VIM id
     */
    public String getID()
    {
        return uuid;
    }

    /**
     * Gets VIM authentication URL
     * @return VIM authentication URL
     */
    public String getUrl()

    {
        return url;
    }

    /**
     * Gets VIM Type
     * @return VIM Type
     */
    public String getType()
    {
        return type;
    }

}
