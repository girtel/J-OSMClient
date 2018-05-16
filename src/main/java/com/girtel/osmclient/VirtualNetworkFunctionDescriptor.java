package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

import java.util.List;

/**
 * This class represents Open Source MANO Virtual-Network-Function-Descriptor (VNFD) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class VirtualNetworkFunctionDescriptor extends OSMComponent
{

    private String name, id, description;
    private List<VirtualDeploymentUnit> vduList;

    /**
     * Constructor
     * @param id VNFD identifier
     * @param name VNFD name
     * @param description VNFD description
     * @param vduList VNFD list of constituent VDUs
     */
    protected VirtualNetworkFunctionDescriptor(String id, String name, String description, List<VirtualDeploymentUnit> vduList)
    {
        super(name, OSMConstants.OSMComponentType.VNFD);
        this.id = id;
        this.name = name;
        this.description = description;
        this.vduList = vduList;
    }

    /**
     * Gets VNFD name
     * @return VNFD name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets VNFD identifier
     * @return VNFD identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets VNFD description
     * @return VNFD description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets VNFD list of constituent VDUs
     * @return VNFD list of constituent VDUs
     */
    public List<VirtualDeploymentUnit> getVDUList()
    {
        return vduList;
    }

}
