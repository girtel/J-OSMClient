package com.girtel.osmclient;


import com.girtel.osmclient.utils.OSMConstants;

/**
 * This class represents Open Source MANO Configuration Agent component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class ConfigAgent extends OSMComponent
{

    private String ipAddress, name, type, user, port;

    /**
     * Constructor
     * @param name agent's name
     * @param type agent's type, e.c. Juju
     * @param user agent's user
     * @param ipAddress agent's IP Address
     * @param port agent's port
     */
    protected ConfigAgent(String name, String type, String user, String ipAddress, String port)
    {
        super(name, OSMConstants.OSMComponentType.CONFIG_AGENT);
        this.name = name;
        this.type = type;
        this.user = user;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Gets agent's name
     * @return agent's name
     */
    public String getName()
    {
            return name;
    }

    /**
     * Gets agent's type, e.c. Juju
     * @return agent's type
     */
    public String getAgentType()
        {
            return type;
        }

    /**
     * Gets agent's user
     * @return agent's user
     */
    public String getUser()
        {
            return user;
        }

    /**
     * Gets agent's IP Address
     * @return agent's IP Address
     */
    public String getIPAddress()
        {
            return ipAddress;
        }

    /**
     * Gets agent's port
     * @return port where this agent is running
     */
    public String getPort()
        {
            return port;
        }
}
