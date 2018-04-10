package com.girtel;





import com.girtel.utils.HTTPResponse;
import com.girtel.utils.OSMConstants;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>REST Client targeted to work with Open Source MANO.</h2>
 *
 * <p>Based on <a href="https://osm.etsi.org/wikipub/index.php/OsmClient">Python OSMClient </a>.</p>
 *
 * <p>For more information, see <a href="https://osm.etsi.org/">OSM-ETSI</a>.</p>
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class OSMClient {

    private String osmIPAddress, credentials;
    private OSMController osmController;

    /**
     * OSMClient constructor
     * @param osmIPAddress IP Address where OSM is running
     */

    public OSMClient(String osmIPAddress, String user, String password)
    {
        this.osmIPAddress = osmIPAddress;
        String userCred = user + ":" + password;
        this.credentials = "Basic " + DatatypeConverter.printBase64Binary(userCred.getBytes());
        this.osmController = new OSMController(this);
    }

    /**
     * Obtains OSM server address
     * @return IP Address where OSM is running
     */
    public String getOSMIPAddress()
    {
        return this.osmIPAddress;
    }

    /**
     * Obtains OSM user and password encoded
     * @return Encoded credentials
     */
    public String getEncodedCredentials()
    {
        return this.credentials;
    }

    /**
     * Adds a new configuration agent, e.c. Juju
     * @param name new agent's name
     * @param type new agent's type
     * @param serverIP ip address where the agent is running
     * @param user  new agent's user
     * @param secret new agent's password
     * @return Response where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse addConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret)
    {
        HTTPResponse response = osmController.addConfigAgent(name, type, serverIP, user, secret);
        return response;
    }

    /**
     * Creates a new datacenter (VIM)
     * @param name new datacenter's name
     * @param osmVimType new datacenter's type (Openvim, Openstack or AWS)
     * @param user datacenter's user
     * @param password datacenter's password
     * @param authURL authentication URL, e.c. in Openstack: http://(IP_ADDRESS)/identity/v3
     * @param tenant datacenter's tenant to instantiate VMs
     * @param usingFloatingIPs true if you want to assign floating ips automatically, false if not
     * @param keyPairName SSH key pair name (optional)
     * @return Response where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse createDatacenter(String name, OSMConstants.OSMVimType osmVimType, String user, String password, String authURL, String tenant, boolean usingFloatingIPs, String... keyPairName)
    {
        HTTPResponse response = osmController.createDataCenter(name,osmVimType,user,password,authURL,tenant,usingFloatingIPs, keyPairName);
        return response;
    }

    /**
     * Creates a new network service
     * @param nsName new network service name
     * @param nsdName ns descritptor name
     * @param datacenterName vim name where ns will be instantiated
     * @return Response where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse createNS(String nsName, String nsdName, String datacenterName)
    {
        HTTPResponse response = osmController.createNS(nsName, nsdName, datacenterName);
        return response;
    }

    /**
     * Deletes a configuration agent
     * @param name agent's name to delete
     * @return Response where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse deleteConfigAgent(String name)
    {
        HTTPResponse response = osmController.deleteConfigAgent(name);
        return response;
    }

    /**
     * Deletes a datacenter
     * @param name datacenter's name to delete
     * @return Response where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse deleteDatacenter(String name)
    {
        HTTPResponse response  = osmController.deleteDatacenter(name);
        return response;
    }

    /**
     * Deletes a network service
     * @param name NetworkService name to delete
     * @return OSM server response (success or failure)
     */
    public String deleteNS(String name)
    {
        String response = osmController.deleteNS(name);
        return response;
    }

    /**
     * Deletes a network service descriptor
     * @param name NSD name to delete
     * @return OSM server response (success or failure)
     */
    public String deleteNSD(String name)
    {
        String response = osmController.deleteNSD(name);
        return response;
    }

    /**
     * Deletes a virtual network function descriptor
     * @param name VNFD name to delete
     * @return OSM server response (success or failure)
     */
    public String deleteVNFD(String name)
    {
        String response = osmController.deleteVNFD(name);
        return response;
    }

    /**
     * Obtains configuration agent's list
     * @return configuration agent's list
     */
    public List<ConfigAgent> getConfigAgentList()
    {
        return osmController.parseConfigAgentList();
    }

    /**
     * Obtains datacenter (vim) 's list
     * @return vim's list
     */
    public List<DataCenter> getDataCenterList()
    {
        return osmController.parseDatacenterList();
    }

    /**
     * Obtains VNFD list
     * @return VNFD list
     */
    public List<VirtualNetworkFunctionDescriptor> getVNFDList()
    {
        return osmController.parseVFNDList();
    }

    /**
     * Obtains VNF list
     * @return VNF list
     */
    public List<VirtualNetworkFunction> getVNFList()
    {
        return osmController.parseVNFList();
    }

    /**
     * Obtains NSD list
     * @return NSD list
     */
    public List<NetworkServiceDescriptor> getNSDList()
    {
        return osmController.parseNSDList();
    }

    /**
     * Obtains NS list
     * @return NS list
     */
    public List<NetworkService> getNSList()
    {
        return osmController.parseNSList();
    }

    /**
     * Obtains monitoring parameters associated to a VNF
     * @param name VNF name
     * @return VNF's monitoring parameters list
     */
    public List<MonitoringParameter> getVNFMonitoringParameterList(String name)
    {
        return this.getVNF(name).getMonitoringParameterList();
    }

    /**
     * Obtains monitoring parameters associated to a NS
     * @param name NS name
     * @return NS's monitoring parameters list
     */
    public List<MonitoringParameter> getNSMonitoringParameterList(String name)
    {
        NetworkService ns = this.getNS(name);
        List<VirtualNetworkFunction> vnfList = osmController.parseVNFList();
        List<MonitoringParameter> nsMonParamList = new ArrayList<>();
        for(VirtualNetworkFunction vnf: vnfList)
        {
            if(ns.getId().equals(vnf.getNSID()))
            {
                nsMonParamList.addAll(vnf.getMonitoringParameterList());
            }
        }

        return nsMonParamList;
    }

    /**
     * Obtains VNFD from its name
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
     * Obtains VNFD from its id
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
     * Obtains VNF from its name
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
     * Obtains Datacenter (VIM) from its name
     * @param name VIM name
     * @return Datacenter (VIM) named (name)
     */
    public DataCenter getDatacenter(String name)
    {
        DataCenter finalDC = null;
        for(DataCenter dc : getDataCenterList())
        {
            if(dc.getName().equals(name))
            {
                finalDC = dc;
                break;
            }
        }

        return finalDC;
    }

    /**
     * Uploads a package (VNFD or NSD)
     * @param file file which represents VNFD or NSD
     * @return Pair where first parameter is http message and second is http code, e.c. (200,OK)
     */
    public HTTPResponse uploadPackage(File file)
    {
        HTTPResponse response = osmController.uploadPackage(file);
        return response;
    }


}