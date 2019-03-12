package com.girtel.osmclient;

import com.girtel.osmclient.internal.OSMException;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.json.JSONFactory;
import com.girtel.osmclient.json.ParseException;

import java.util.List;

public class OSMClient005
{
    private String osmIPAddress, project, user, pass, sessionToken;
    private OSMController005 osmController005;

    public OSMClient005(String osmIPAddress, String user, String password, String... project)
    {
        this.osmIPAddress = osmIPAddress;
        this.user = user;
        this.pass = password;
        if(project.length == 0)
            this.project = "admin";
        else if(project.length == 1)
            this.project = project[0];
        else
            throw new OSMException("More than one project is not allowed in OSM");

        this.osmController005 = new OSMController005(this);
    }

    /**
     * Obtains OSM server address
     * @return IP Address where OSM is running
     */
    protected String getOSMIPAddress()
    {
        return this.osmIPAddress;
    }

    /**
     * Obtains OSM user
     * @return OSM user
     */
    protected String getOSMUser()
    {
        return this.user;
    }

    /**
     * Obtains OSM password
     * @return OSM password
     */
    protected String getOSMPassword()
    {
        return this.pass;
    }

    /**
     * Obtains Session Token
     * @return Session Token
     */
    protected String getSessionToken()
    {
        if(this.sessionToken == null)
        {
            HTTPResponse resp = osmController005.createSessionToken();
            String token = resp.getContent();
            JSONObject tokenJSON = new JSONObject(token);
            String tokenID = tokenJSON.get("_id").getValue();
            this.sessionToken = tokenID;
        }
        return this.sessionToken;
    }
    /**
     * Obtains OSM project
     * @return Project
     */
    protected String getProject()
    {
        return this.project;
    }

    public List<VirtualInfrastructureManager> getVIMList()
    {
        return osmController005.parseVIMList();
    }

    /*public List<VirtualNetworkFunctionDescriptor> getVNFDList()
    {
        return osmController005.parseVNFDList();
    }*/

    public List<NetworkServiceDescriptor> getNSDList()
    {
        return osmController005.parseNSDList();
    }

    public List<NetworkService> getNSList()
    {
        return osmController005.parseNSList();
    }

    public HTTPResponse createNS(String nsName, String nsdName, String vimName)
    {
        return osmController005.createNS(nsName, nsdName, vimName);
    }

    public HTTPResponse deleteNS(String name)
    {
        return osmController005.deleteNS(name);
    }

    public VirtualInfrastructureManager getVIMByName(String name)
    {
        List<VirtualInfrastructureManager> vims = this.getVIMList();
        VirtualInfrastructureManager finalVim = null;
        for(VirtualInfrastructureManager vim : vims)
        {
            if(vim.getName().equalsIgnoreCase(name))
            {
                finalVim = vim;
                break;
            }
        }

        return finalVim;
    }

    public NetworkServiceDescriptor getNSDByName(String name)
    {
        List<NetworkServiceDescriptor> nsds = this.getNSDList();
        NetworkServiceDescriptor finalNSD = null;
        for(NetworkServiceDescriptor nsd : nsds)
        {
            if(nsd.getName().equalsIgnoreCase(name))
            {
                finalNSD = nsd;
                break;
            }
        }

        return finalNSD;
    }

    public NetworkService getNSByName(String name)
    {
        List<NetworkService> nss = this.getNSList();
        NetworkService finalNS = null;
        for(NetworkService ns : nss)
        {
            if(ns.getName().equalsIgnoreCase(name))
            {
                finalNS = ns;
                break;
            }
        }

        return finalNS;
    }

    public static void main(String [] args)
    {
        OSMClient005 osmClient005 = new OSMClient005("10.0.2.15","admin","admin");
        System.out.println(osmClient005.getSessionToken());
        System.out.println(osmClient005.getVIMList());
        System.out.println(osmClient005.getNSDList());
        System.out.println(osmClient005.getNSList());
        //System.out.println(osmClient005.createNS("pruebaca","vimtwo","nat-tiny-nsd"));
        System.out.println(osmClient005.deleteNS("pruebaca"));
    }
}
