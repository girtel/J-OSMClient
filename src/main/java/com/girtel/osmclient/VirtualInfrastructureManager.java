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
     * @param uuid VIM UUID (unique identifier)
     * @param url VIM URL
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
     * Gets VIM UUID
     * @return VIM UUID
     */
    public String getUUID()
    {
        return uuid;
    }

    /**
     * Gets VIM URL
     * @return VIM URL
     */
    public String getUrl()

    {
        return url.split("/")[2];
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
