package com.girtel.osmclient;


import javafx.util.Pair;

import java.util.List;

/**
 * This class represents Open Source MANO Virtual-Link-Desciptor (VLD) component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class VirtualLinkDescriptor {


    private String id, name, description;
    private List<Pair<String, String>> connPointRefList;
    private boolean isMgmtNetwork;

    /**
     * Constructor
     * @param vldId VLD identifier
     * @param vldName VLD name
     * @param vldDescription VLD description
     * @param connPointRefList VLD ConnectionPoint references (a reference is represented by two identifiers, which identify two connected interfaces)
     * @param vldIsMgmtNetwork true if this VLD belongs to Management Network, false if not
     */
    protected VirtualLinkDescriptor(String vldId, String vldName, String vldDescription, List<Pair<String, String>> connPointRefList, boolean vldIsMgmtNetwork)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.connPointRefList = connPointRefList;
        this.isMgmtNetwork = isMgmtNetwork;
    }

    /**
     * Gets VLD identifier
     * @return VLD identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets VLD name
     * @return VLD name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets VLD description
     * @return VLD description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets VLD connection-point references
     * @return VLD connection-point references
     */
    public List<Pair<String, String>> getConnectionPointRefList()
    {
        return connPointRefList;
    }

    /**
     * Gets info about the membership of this VLD to Management Network
     * @return true if this VLD belongs to Management Network, false if not
     */
    public boolean isMgmtNetwork()
    {
        return isMgmtNetwork;
    }
}
