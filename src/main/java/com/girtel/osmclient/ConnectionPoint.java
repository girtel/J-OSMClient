package com.girtel.osmclient;


/**
 * This class represents Open Source MANO Connection-Point component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class ConnectionPoint {

    private String name, macAddress, ipAddress;

    /**
     * Constructor
     * @param name connection point's name
     * @param macAddress connection point's MAC Address
     * @param ipAddress connection point's IP Address
     */
    protected ConnectionPoint(String name, String macAddress, String ipAddress)
    {
        this.name = name;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }

    /**
     * Gets connection point's name
     * @return connection point's name
     */
    public String getName(){ return name; }

    /**
     * Gets connection point's MAC Address
     * @return connection point's MAC Address
     */
    public String getMACAddress(){ return macAddress; }

    /**
     * Gets connection point's IP Address
     * @return connection point's IP Address
     */
    public String getIPAddress(){ return ipAddress; }
}
