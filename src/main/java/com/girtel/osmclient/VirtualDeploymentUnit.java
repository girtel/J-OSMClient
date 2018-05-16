package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

/**
 * This class represents Open Source MANO Virtual-Deployment-Unit (VDU) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class VirtualDeploymentUnit extends OSMComponent{

    private String id, name, image;
    private double storageInGB, numberOfCPUs, RAMMemoryInMB;

    /**
     * Constructor
     * @param id VDU identifier
     * @param name VDU name
     * @param image VDU image
     * @param storageInGB VDU HD storage (GB)
     * @param numberOfCPUs VDU CPU number (CPU)
     * @param RAMMemoryInMB VDU RAM memory (MB)
     */
    protected VirtualDeploymentUnit(String id, String name, String image, double storageInGB, double numberOfCPUs, double RAMMemoryInMB)
    {
        super(name, OSMConstants.OSMComponentType.VDU);
        this.id = id;
        this.name = name;
        this.image = image;
        this.storageInGB = storageInGB;
        this.numberOfCPUs = numberOfCPUs;
        this.RAMMemoryInMB = RAMMemoryInMB;
    }

    /**
     * Gets VDU identifier
     * @return VDU identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets VDU name
     * @return VDU name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets VDU image
     * @return VDU image
     */
    public String getImage()
    {
        return image;
    }

    /**
     * Gets VDU HD storage in GB
     * @return VDU HD storage in GB
     */
    public double getStorageInGB()
    {
        return storageInGB;
    }

    /**
     * Gets VDU CPU number
     * @return VDU CPU number
     */
    public double getNumberOfCPUs()
    {
        return numberOfCPUs;
    }

    /**
     * Gets VDU RAM memory in MB
     * @return VDU RAM memory in MB
     */
    public double getRAMMemoryInMB()
    {
        return RAMMemoryInMB;
    }
}
