package com.girtel.osmclient;

import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.utils.OSMException;
import com.girtel.osmclient.utils.NSConfiguration;
import com.girtel.osmclient.utils.VIMConfiguration;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.OSMConstants;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h2>REST Client targeted to work with Open Source MANO.</h2>
 *
 * <p>Based on <a href="https://osm.etsi.org/wikipub/index.php/OsmClient">Python OSMClient </a>.</p>
 *
 * <p>For more information, see <a href="https://osm.etsi.org/">OSM-ETSI</a>.</p>
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class OSMClient
{

    private String osmIPAddress, project, user, password, sessionToken;
    private OSMConstants.OSMClientVersion version;
    private OSMControllerR3 osmControllerR3;
    private OSMController005 osmController005;

    /**
     * OSMClient constructor
     * @param version OSM version (release three or sol005)
     * @param osmIPAddress IP Address where OSM is running
     * @param user OSM user
     * @param password OSM password
     * @param project OSM project (optional). If it is not specified, default project will be used
     */
    public OSMClient(OSMConstants.OSMClientVersion version, String osmIPAddress, String user, String password, String... project)
    {
        this.osmIPAddress = osmIPAddress;
        this.user = user;
        this.password = password;
        this.sessionToken = "";
        this.version = version;
        switch(version)
        {
            case RELEASE_THREE:
                if(project.length == 0)
                    this.project = "default";
                else if(project.length == 1)
                    this.project = project[0];
                else
                    throw new OSMException("More than one project is not allowed in OSM");

                this.osmControllerR3 = new OSMControllerR3(this);
                break;

            case SOL_005:
                if(project.length == 0)
                    this.project = "admin";
                else if(project.length == 1)
                    this.project = project[0];
                else
                    throw new OSMException("More than one project is not allowed in OSM");

                this.osmController005 = new OSMController005(this);
                break;
        }


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
        return this.password;
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
        switch(version)
        {
            case RELEASE_THREE:
                throw new OSMException("Token feature is not supported on OSM release three");

            case SOL_005:
                if(this.sessionToken == null)
                {
                    HTTPResponse resp = osmController005.createSessionToken();
                    String token = resp.getContent();
                    JSONObject tokenJSON = new JSONObject(token);
                    String tokenID = tokenJSON.get("_id").getValue();
                    this.sessionToken = tokenID;
                }
        }
        return this.sessionToken;
    }

    /**
     * Adds a new configuration agent, e.c. Juju
     * @param name new agent's name
     * @param type new agent's type
     * @param serverIP ip address where the agent is running
     * @param user  new agent's user
     * @param secret new agent's password
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse addConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.createConfigAgent(name, type, serverIP, user, secret);
                break;
            case SOL_005:
                //response = osmController005.createConfigAgent(name, type, serverIP, user, secret);
                break;
        }
        return response;
    }

    /**
     * Creates a new VIM
     * @param name new VIM name
     * @param osmVimType new VIM type (Openvim, Openstack, VMWare or AWS)
     * @param user VIM user
     * @param pass VIM password
     * @param authURL authentication URL, e.c. in Openstack: http://(IP_ADDRESS)/identity/v3
     * @param tenant VIM tenant to instantiate VMs
     * @param VIMConfiguration optional vim Configuration parameters
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse createVIM(String name, OSMConstants.OSMVimType osmVimType, String user, String pass, String authURL, String tenant, VIMConfiguration... VIMConfiguration)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.createVIM(name,osmVimType,user,pass,authURL,tenant, VIMConfiguration);
                break;
            case SOL_005:
                //response = osmController005.createVIM(name,osmVimType,user,pass,authURL,tenant, VIMConfiguration);
                break;
        }
        return response;
    }

    /**
     * Creates a new network service
     * @param nsName new network service name
     * @param nsdName ns descritptor name
     * @param datacenterName VIM name where ns will be instantiated
     * @param nsConfiguration optional network service Configuration parameters (NOT SUPPORTED IN RELEASE 3)
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse createNS(String nsName, String nsdName, String datacenterName, NSConfiguration... nsConfiguration)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                if(nsConfiguration.length >= 1)
                    throw new OSMException("Network Service configuration is not supported on OSM Release 3");
                response = osmControllerR3.createNS(nsName, nsdName, datacenterName);
                break;

            case SOL_005:
                response = osmController005.createNS(nsName, nsdName, datacenterName, nsConfiguration);
                break;
        }
        return response;
    }

    /**
     * Deletes all configuration agents
     * @return Map where key is agent's name and value is http response
     */
    public Map<String, HTTPResponse> deleteAllConfigAgents()
    {
        List<ConfigAgent> configAgents = this.getConfigAgentList();
        Map<String, HTTPResponse> configAgentsResponseMap = configAgents.stream().collect(Collectors.toMap(agent -> agent.getName(), agent -> this.deleteConfigAgent(agent.getName())));
        return configAgentsResponseMap;
    }

    /**
     * Deletes all VIMs
     * @return Map where key is agent's name and value is http response
     */
    public Map<String, HTTPResponse> deleteAllVIM()
    {
        List<VirtualInfrastructureManager> vims = this.getVIMList();
        Map<String, HTTPResponse> vimsResponseMap = vims.stream().collect(Collectors.toMap(vim -> vim.getName(), vim -> this.deleteVIM(vim.getName())));
        return vimsResponseMap;
    }

    /**
     * Deletes all Network Services (NS)
     * @return Map where key is agent's name and value is OSM response
     */
    public Map<String, HTTPResponse> deleteAllNS()
    {
        List<NetworkService> networkServices = this.getNSList();
        Map<String, HTTPResponse> networkServicesResponseMap = networkServices.stream().collect(Collectors.toMap(ns -> ns.getName(), ns -> this.deleteNS(ns.getName())));
        return networkServicesResponseMap;
    }

    /**
     * Deletes all Network Service Descriptors (NSD)
     * @return Map where key is agent's name and value is OSM response
     */
    public Map<String, HTTPResponse> deleteAllNSD()
    {
        List<NetworkServiceDescriptor> networkServiceDescriptors = this.getNSDList();
        Map<String, HTTPResponse> networkServiceDescriptorsResponseMap = networkServiceDescriptors.stream().collect(Collectors.toMap(nsd -> nsd.getName(), nsd -> this.deleteNSD(nsd.getName())));
        return networkServiceDescriptorsResponseMap;
    }

    /**
     * Deletes all Virtual Network Function Descriptors (VNFD)
     * @return Map where key is agent's name and value is OSM response
     */
    public Map<String, HTTPResponse> deleteAllVNFD()
    {
        List<VirtualNetworkFunctionDescriptor> virtualNetworkFunctionDescriptors = this.getVNFDList();
        Map<String, HTTPResponse> virtualNetworkFunctionDescriptorsResponseMap = virtualNetworkFunctionDescriptors.stream().collect(Collectors.toMap(vnfd -> vnfd.getName(), vnfd -> this.deleteVNFD(vnfd.getName())));
        return virtualNetworkFunctionDescriptorsResponseMap;
    }

    /**
     * Deletes a configuration agent
     * @param name agent's name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteConfigAgent(String name)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.deleteConfigAgent(name);
                break;

            case SOL_005:
                //response = osmController005.deleteConfigAgent(name);
                break;
        }
        return response;
    }

    /**
     * Deletes a VIM
     * @param name VIM name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteVIM(String name)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.deleteVIM(name);
                break;

            case SOL_005:
                //response = osmController005.deleteVIM(name);
                break;
        }
        return response;
    }

    /**
     * Deletes a network service
     * @param name NetworkService name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteNS(String name)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.deleteNS(name);
                break;

            case SOL_005:
                response = osmController005.deleteNS(name);
                break;
        }
        return response;
    }

    /**
     * Deletes a network service descriptor
     * @param name NSD name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteNSD(String name)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.deleteNSD(name);
                break;

            case SOL_005:
                //response = osmController005.deleteNSD(name);
                break;
        }
        return response;
    }

    /**
     * Deletes a virtual network function descriptor
     * @param name VNFD name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteVNFD(String name)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.deleteVNFD(name);
                break;

            case SOL_005:
                //response = osmController005.deleteVNFD(name);
                break;
        }
        return response;
    }

    /**
     * Obtains a list with every component in OSM (CA, VIM, VNFD, VNF, NSD, NS)
     * @return OSM Component list
     */
    public List<OSMComponent> getOSMComponentList()
    {
        List<OSMComponent> componentList = new LinkedList<>();
        if(version.equals(OSMConstants.OSMClientVersion.RELEASE_THREE))
            componentList.addAll(getConfigAgentList());
        componentList.addAll(getVIMList());
        componentList.addAll(getVNFDList());
        componentList.addAll(getVNFList());
        componentList.addAll(getNSDList());
        componentList.addAll(getNSList());
        return componentList;
    }

    /**
     * Obtains configuration agent's list
     * @return configuration agent's list
     */
    public List<ConfigAgent> getConfigAgentList()
    {
        List<ConfigAgent> configAgents = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                configAgents.addAll(osmControllerR3.parseConfigAgentList());
                break;

            case SOL_005:
                throw new OSMException("Config Agent is not supported on OSM sol005");
        }
        return configAgents;
    }

    /**
     * Obtains datacenter (vim) 's list
     * @return vim's list
     */
    public List<VirtualInfrastructureManager> getVIMList()
    {
        List<VirtualInfrastructureManager> vims = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                vims.addAll(osmControllerR3.parseVIMList());
                break;

            case SOL_005:
                vims.addAll(osmController005.parseVIMList());
                break;
        }
        return vims;
    }

    /**
     * Obtains VNFD list
     * @return VNFD list
     */
    public List<VirtualNetworkFunctionDescriptor> getVNFDList()
    {
        List<VirtualNetworkFunctionDescriptor> vnfds = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                vnfds.addAll(osmControllerR3.parseVFNDList());
                break;

            case SOL_005:
                vnfds.addAll(osmController005.parseVNFDList());
                break;
        }
        return vnfds;
    }

    /**
     * Obtains VNF list
     * @return VNF list
     */
    public List<VirtualNetworkFunction> getVNFList()
    {
        List<VirtualNetworkFunction> vnfs = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                vnfs.addAll(osmControllerR3.parseVNFList());
                break;

            case SOL_005:
                //vnfs.addAll(osmController005.parseVNFList());
                break;
        }
        return vnfs;
    }

    /**
     * Obtains NSD list
     * @return NSD list
     */
    public List<NetworkServiceDescriptor> getNSDList()
    {
        List<NetworkServiceDescriptor> nsds = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                nsds.addAll(osmControllerR3.parseNSDList());
                break;

            case SOL_005:
                nsds.addAll(osmController005.parseNSDList());
                break;
        }
        return nsds;
    }

    /**
     * Obtains NS list
     * @return NS list
     */
    public List<NetworkService> getNSList()
    {
        List<NetworkService> nss = new LinkedList<>();
        switch(version)
        {
            case RELEASE_THREE:
                nss.addAll(osmControllerR3.parseNSList());
                break;

            case SOL_005:
                nss.addAll(osmController005.parseNSList());
                break;
        }
        return nss;
    }

    /**
     * Obtains a VNFD from its name
     * @param name VNFD name
     * @return VNFD named (name)
     */
    public VirtualNetworkFunctionDescriptor getVNFD(String name)
    {
        VirtualNetworkFunctionDescriptor vnfd = null;
        for(VirtualNetworkFunctionDescriptor vnfdItem : getVNFDList())
        {
            if(vnfdItem.getName().equals(name))
            {
                vnfd = vnfdItem;
                break;
            }
        }

        return vnfd;
    }

    /**
     * Obtains a VNFD from its id
     * @param id VNFD id
     * @return VNFD identified by (id)
     */
    public VirtualNetworkFunctionDescriptor getVNFDById(String id)
    {
        VirtualNetworkFunctionDescriptor vnfd = null;
        for(VirtualNetworkFunctionDescriptor vnfdItem : getVNFDList())
        {
            if(vnfdItem.getId().equals(id))
            {
                vnfd = vnfdItem;
                break;
            }
        }

        return vnfd;
    }

    /**
     * Obtains a VNF from its name
     * @param name VNF name
     * @return VNF named (name)
     */
    public VirtualNetworkFunction getVNF(String name)
    {
        VirtualNetworkFunction vnf = null;
        for(VirtualNetworkFunction vnfItem : getVNFList())
        {
            if(vnfItem.getName().equals(name))
            {
                vnf = vnfItem;
                break;
            }
        }

        return vnf;
    }

    /**
     * Obtains a VNF from its id
     * @param id VNF id
     * @return VNF identified by (id)
     */
    public VirtualNetworkFunction getVNFById(String id)
    {
        VirtualNetworkFunction vnf = null;
        for(VirtualNetworkFunction vnfItem : getVNFList())
        {
            if(vnfItem.getId().equals(id))
            {
                vnf = vnfItem;
                break;
            }
        }

        return vnf;
    }

    /**
     * Obtains NSD from its name
     * @param name NSD name
     * @return NSD named (name)
     */
    public NetworkServiceDescriptor getNSD(String name)
    {
        NetworkServiceDescriptor nsd = null;
        for(NetworkServiceDescriptor nsdItem : getNSDList())
        {
            if(nsdItem.getName().equals(name))
            {
                nsd = nsdItem;
                break;
            }
        }

        return nsd;
    }

    /**
     * Obtains a NSD from its id
     * @param id NSD id
     * @return NSD identified by (id)
     */
    public NetworkServiceDescriptor getNSDById(String id)
    {
        NetworkServiceDescriptor nsd = null;
        for(NetworkServiceDescriptor nsdItem : getNSDList())
        {
            if(nsdItem.getId().equals(id))
            {
                nsd = nsdItem;
                break;
            }
        }

        return nsd;
    }

    /**
     * Obtains NS from its name
     * @param name NS name
     * @return NS named (name)
     */
    public NetworkService getNS(String name)
    {
        NetworkService ns = null;
        for(NetworkService nsItem : getNSList())
        {
            if(nsItem.getName().equals(name))
            {
                ns = nsItem;
                break;
            }
        }

        return ns;
    }

    /**
     * Obtains a NS from its id
     * @param id NS id
     * @return NS identified by (id)
     */
    public NetworkService getNSById(String id)
    {
        NetworkService ns = null;
        for(NetworkService nsItem : getNSList())
        {
            if(nsItem.getId().equals(id))
            {
                ns = nsItem;
                break;
            }
        }

        return ns;
    }

    /**
     * Obtains VIM from its name
     * @param name VIM name
     * @return VIM named (name)
     */
    public VirtualInfrastructureManager getVIM(String name)
    {
        VirtualInfrastructureManager finalVIM = null;
        for(VirtualInfrastructureManager vim : getVIMList())
        {
            if(vim.getName().equals(name))
            {
                finalVIM = vim;
                break;
            }
        }

        return finalVIM;
    }

    /**
     * Obtains a VIM from its id
     * @param id VIM id
     * @return VIM identified by (id)
     */
    public VirtualInfrastructureManager getVIMById(String id)
    {
        VirtualInfrastructureManager finalVIM = null;
        for(VirtualInfrastructureManager vim : getVIMList())
        {
            if(vim.getId().equals(id))
            {
                finalVIM = vim;
                break;
            }
        }

        return finalVIM;
    }

    /**
     * Uploads a package (VNFD or NSD)
     * @param file file which represents a VNFD or NSD package
     * @param type package type (VNFD or NSD)
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse uploadPackage(File file, OSMConstants.OSMPackageType type)
    {
        HTTPResponse response = null;
        switch(version)
        {
            case RELEASE_THREE:
                response = osmControllerR3.uploadPackage(file);
                break;

            case SOL_005:
                //response = osmController005.uploadPackage(file, type);
                break;
        }
        return response;
    }

}
