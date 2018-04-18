package com.girtel.osmclient;

/**
 * This class represents Open Source MANO VIM component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class DataCenter {

    private String name, uuid, url, type;

    /**
     * Constructor
     * @param name VIM name
     * @param uuid VIM UUID (unique identifier)
     * @param url VIM URL
     * @param type VIM Type
     */
    protected DataCenter(String name, String uuid, String url, String type)
    {
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
