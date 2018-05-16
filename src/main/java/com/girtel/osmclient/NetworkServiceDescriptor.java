package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents Open Source MANO Network-Service-Descriptor (NSD) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class NetworkServiceDescriptor extends OSMComponent{


    private String id, name, description;
    private List<VirtualNetworkFunctionDescriptor> vnfdList;
    private List<VirtualLinkDescriptor> vldList;

    /**
     * Constructor
     * @param id NSD identifier
     * @param name NSD name
     * @param description NSD description
     * @param vnfdList NSD constituent VNFDs
     * @param vldList NSD constituent VLDs
     */
    protected NetworkServiceDescriptor(String id, String name, String description, List<VirtualNetworkFunctionDescriptor> vnfdList, List<VirtualLinkDescriptor> vldList)
    {
        super(name, OSMConstants.OSMComponentType.NSD);
        this.id = id;
        this.name = name;
        this.description = description;
        this.vnfdList = vnfdList;
        this.vldList = vldList;
    }

    /**
     * Gets NSD identifier
     * @return NSD identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets NSD name
     * @return NSD name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets NSD description
     * @return NSD description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets NSD constituent VNFDs
     * @return NSD constituent VNFDs
     */
    public List<VirtualNetworkFunctionDescriptor> getConstituentVNFDs()
    {
        return vnfdList;
    }

    /**
     * Gets NSD VLD list
     * @return NSD VLD list
     */
    public List<VirtualLinkDescriptor> getVLDList()
    {
        return vldList;
    }
}
