package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

import java.util.List;

/**
 * This class represents Open Source MANO Network-Service (NS) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class NetworkService extends OSMComponent{


    private String id, name, description, status;
    private NetworkServiceDescriptor nsd;
    private List<VirtualNetworkFunction> vnfList;
    private String vimName;

    /**
     * Constructor
     * @param id NS identifier
     * @param name NS name
     * @param description NS description
     * @param status NS current status
     * @param vimName VIM where this NS is instantiated
     * @param nsd NSD which defines this NS
     * @param vnfList List of VNFs which belong to this NS
     */
    protected NetworkService(String id, String name, String description, String status, String vimName, NetworkServiceDescriptor nsd, List<VirtualNetworkFunction> vnfList)
    {
        super(name, OSMConstants.OSMComponentType.NS);
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.vimName = vimName;
        this.nsd = nsd;
        this.vnfList = vnfList;
    }

    /**
     * Gets NS identifier
     * @return NS identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets NS name
     * @return NS name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets NS description
     * @return NSdescription
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets NS current status
     * @return NS current status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets VIM where this NS is instantiated
     * @return NS VIM
     */
    public String getVIMName() {
        return vimName;
    }

    /**
     * Gets NSD which represents this NS
     * @return NSD which represents this NS
     */
    public NetworkServiceDescriptor getNSD() {
        return nsd;
    }

    /**
     * Gets list of VNF which belong to this NS
     * @return List of VNF
     */
    public List<VirtualNetworkFunction> getVNFList()
    {
        return vnfList;
    }
}
