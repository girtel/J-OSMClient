package com.girtel.osmclient;





import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.OSMConstants;
import org.apache.http.protocol.HTTP;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.ArrayList;
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
public class OSMClient {

    private String osmIPAddress, credentials, project;
    private OSMController osmController;

    /**
     * OSMClient constructor
     * @param osmIPAddress IP Address where OSM is running
     * @param user OSM user
     * @param password OSM password
     * @param project OSM project (optional). If it is not specified, project default will be used
     */

    public OSMClient(String osmIPAddress, String user, String password, String... project)
    {
        this.osmIPAddress = osmIPAddress;
        String userCred = user + ":" + password;
        this.credentials = "Basic " + DatatypeConverter.printBase64Binary(userCred.getBytes());
        if(project.length == 0)
            this.project = "default";
        else if(project.length == 1)
            this.project = project[0];
        else
            throw new RuntimeException("More than one project is not allowed");

        this.osmController = new OSMController(this);
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
     * Obtains OSM user and password encoded
     * @return Encoded credentials
     */
    protected String getEncodedCredentials()
    {
        return this.credentials;
    }

    /**
     * Obtains OSM project
     * @return Project
     */
    protected String getProject()
    {
        return this.project;
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
     * @return HTTPResponse from OSM (code, message, content)
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
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse createNS(String nsName, String nsdName, String datacenterName)
    {
        HTTPResponse response = osmController.createNS(nsName, nsdName, datacenterName);
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
     * Deletes all Datacenters (VIMs)
     * @return Map where key is agent's name and value is http response
     */
    public Map<String, HTTPResponse> deleteAllDatacenters()
    {
        List<DataCenter> datacenters = this.getDataCenterList();
        Map<String, HTTPResponse> datacentersResponseMap = datacenters.stream().collect(Collectors.toMap(dc -> dc.getName(), dc -> this.deleteDatacenter(dc.getName())));
        return datacentersResponseMap;
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
        HTTPResponse response = osmController.deleteConfigAgent(name);
        return response;
    }

    /**
     * Deletes a datacenter
     * @param name datacenter's name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteDatacenter(String name)
    {
        HTTPResponse response  = osmController.deleteDatacenter(name);
        return response;
    }

    /**
     * Deletes a network service
     * @param name NetworkService name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteNS(String name)
    {
        HTTPResponse response = osmController.deleteNS(name);
        return response;
    }

    /**
     * Deletes a network service descriptor
     * @param name NSD name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteNSD(String name)
    {
        HTTPResponse response = osmController.deleteNSD(name);
        return response;
    }

    /**
     * Deletes a virtual network function descriptor
     * @param name VNFD name to delete
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse deleteVNFD(String name)
    {
        HTTPResponse response = osmController.deleteVNFD(name);
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
     * @return HTTPResponse from OSM (code, message, content)
     */
    public HTTPResponse uploadPackage(File file)
    {
        HTTPResponse response = osmController.uploadPackage(file);
        return response;
    }

    public static void main(String [] args)
    {
        //OSMClient osmClient = new OSMClient("192.168.10.115","admin","admin");

        /*System.out.println(osmClient.getDataCenterList());
        System.out.println(osmClient.deleteAllDatacenters());

        System.out.println(osmClient.createDatacenter("vimtwo", OSMConstants.OSMVimType.OPENSTACK,
                "admin","girtelserver", "http://10.0.2.12/identity/v3",
                "admin",true,"keypairvimtwo"));

        System.out.println(osmClient.createDatacenter("vimthree", OSMConstants.OSMVimType.OPENSTACK,
                "admin","girtelserver", "http://10.0.2.13/identity/v3",
                "admin",true,"keypairvimthree"));*/
       // osmClient.deleteAllNSD();
        /*JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int sel = chooser.showOpenDialog(null);
        if(sel == JFileChooser.APPROVE_OPTION)
        {
            File [] selectedFiles = chooser.getSelectedFiles();
            for(File f : selectedFiles)
                System.out.println(osmClient.uploadPackage(f));
        }*/

        //System.out.println(osmClient.createNS("prueba","vnf1-ns-large","vimtwo"));
    }

}
