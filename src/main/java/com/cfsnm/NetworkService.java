package com.cfsnm;

/**
 * This class represents Open Source MANO Network-Service (NS) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class NetworkService {


    private String id, name, description, status;
    private NetworkServiceDescriptor nsd;
    private String datacenterName;

    /**
     * Constructor
     * @param id NS identifier
     * @param name NS name
     * @param description NS description
     * @param status NS current status
     * @param datacenterName datacenter where this NS is instantiated
     * @param nsd NSD which defines this NS
     */
    protected NetworkService(String id, String name, String description, String status, String datacenterName, NetworkServiceDescriptor nsd)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.datacenterName = datacenterName;
        this.nsd = nsd;
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
     * Gets datacenter where this NS is instantiated
     * @return NS datacenter
     */
    public String getDatacenterName() {
        return datacenterName;
    }

    /**
     * Gets NSD which represents this NS
     * @return NSD which represents this NS
     */
    public NetworkServiceDescriptor getNSD() {
        return nsd;
    }
}
