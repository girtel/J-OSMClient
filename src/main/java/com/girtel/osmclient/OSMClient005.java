package com.girtel.osmclient;

import com.girtel.osmclient.internal.IOSMClient;
import com.girtel.osmclient.utils.OSMException;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.utils.NSConfiguration;
import com.girtel.osmclient.utils.OSMConstants;
import com.girtel.osmclient.utils.VIMConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public class OSMClient005 implements IOSMClient
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
     * Obtains OSM project
     * @return Project
     */
    protected String getOSMProject()
    {
        return this.project;
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


    public List<VirtualInfrastructureManager> getVIMList()
    {
        return osmController005.parseVIMList();
    }

    public List<VirtualNetworkFunctionDescriptor> getVNFDList()
    {
        return osmController005.parseVNFDList();
    }

    @Override
    public List<VirtualNetworkFunction> getVNFList() {
        return null;
    }

    public List<NetworkServiceDescriptor> getNSDList()
    {
        return osmController005.parseNSDList();
    }

    public List<NetworkService> getNSList()
    {
        return osmController005.parseNSList();
    }



    @Override
    public HTTPResponse uploadPackage(File file) {
        return null;
    }

    public HTTPResponse createNS(String nsName, String nsdName, String vimName)
    {
        return osmController005.createNS(nsName, nsdName, vimName);
    }

    @Override
    public HTTPResponse addConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret) {
        return null;
    }

    @Override
    public HTTPResponse createVIM(String name, OSMConstants.OSMVimType osmVimType, String user, String password, String authURL, String tenant, VIMConfiguration... VIMConfiguration) {
        return null;
    }

    @Override
    public HTTPResponse createNS(String nsName, String nsdName, String datacenterName, NSConfiguration... NSConfiguration) {
        return null;
    }

    @Override
    public Map<String, HTTPResponse> deleteAllConfigAgents() {
        return null;
    }

    @Override
    public Map<String, HTTPResponse> deleteAllVIM() {
        return null;
    }

    @Override
    public Map<String, HTTPResponse> deleteAllNS() {
        return null;
    }

    @Override
    public Map<String, HTTPResponse> deleteAllNSD() {
        return null;
    }

    @Override
    public Map<String, HTTPResponse> deleteAllVNFD() {
        return null;
    }

    @Override
    public HTTPResponse deleteConfigAgent(String name) {
        return null;
    }

    @Override
    public HTTPResponse deleteVIM(String name) {
        return null;
    }

    public HTTPResponse deleteNS(String name)
    {
        return osmController005.deleteNS(name);
    }

    @Override
    public HTTPResponse deleteNSD(String name) {
        return null;
    }

    @Override
    public HTTPResponse deleteVNFD(String name) {
        return null;
    }

    @Override
    public List<OSMComponent> getOSMComponentList() {
        return null;
    }

    @Override
    public List<ConfigAgent> getConfigAgentList() {
        return null;
    }

    public VirtualInfrastructureManager getVIM(String name)
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

    @Override
    public VirtualInfrastructureManager getVIMById(String id)
    {
        return null;
    }

    @Override
    public VirtualNetworkFunctionDescriptor getVNFD(String name)
    {
        return null;
    }

    @Override
    public VirtualNetworkFunctionDescriptor getVNFDById(String id)
    {
        return null;
    }

    @Override
    public VirtualNetworkFunction getVNF(String name)
    {
        return null;
    }

    @Override
    public VirtualNetworkFunction getVNFById(String id)
    {
        return null;
    }

    public NetworkServiceDescriptor getNSD(String name)
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

    @Override
    public NetworkServiceDescriptor getNSDById(String name)
    {
        return null;
    }

    public NetworkService getNS(String name)
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

    @Override
    public NetworkService getNSById(String id)
    {
        return null;
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
