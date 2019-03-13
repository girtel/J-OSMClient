package com.girtel.osmclient.internal;

import com.girtel.osmclient.*;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.NSConfiguration;
import com.girtel.osmclient.utils.OSMConstants;
import com.girtel.osmclient.utils.VIMConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Contract that must be fullfilled to work as a OSMClient
 */
public interface IOSMClient
{

    public HTTPResponse addConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret);

    public HTTPResponse createVIM(String name, OSMConstants.OSMVimType osmVimType, String user, String password, String authURL, String tenant, VIMConfiguration... VIMConfiguration);

    public HTTPResponse createNS(String nsName, String nsdName, String datacenterName, NSConfiguration... NSConfiguration);

    public Map<String, HTTPResponse> deleteAllConfigAgents();

    public Map<String, HTTPResponse> deleteAllVIM();

    public Map<String, HTTPResponse> deleteAllNS();

    public Map<String, HTTPResponse> deleteAllNSD();

    public Map<String, HTTPResponse> deleteAllVNFD();

    public HTTPResponse deleteConfigAgent(String name);

    public HTTPResponse deleteVIM(String name);

    public HTTPResponse deleteNS(String name);

    public HTTPResponse deleteNSD(String name);

    public HTTPResponse deleteVNFD(String name);

    public List<OSMComponent> getOSMComponentList();

    public List<ConfigAgent> getConfigAgentList();

    public List<VirtualInfrastructureManager> getVIMList();

    public List<VirtualNetworkFunctionDescriptor> getVNFDList();

    public List<VirtualNetworkFunction> getVNFList();

    public List<NetworkServiceDescriptor> getNSDList();

    public List<NetworkService> getNSList();

    public VirtualNetworkFunctionDescriptor getVNFD(String name);

    public VirtualNetworkFunctionDescriptor getVNFDById(String id);

    public VirtualNetworkFunction getVNF(String name);

    public VirtualNetworkFunction getVNFById(String id);

    public NetworkServiceDescriptor getNSD(String name);

    public NetworkServiceDescriptor getNSDById(String name);

    public NetworkService getNS(String name);

    public NetworkService getNSById(String id);

    public VirtualInfrastructureManager getVIM(String name);

    public VirtualInfrastructureManager getVIMById(String id);

    public HTTPResponse uploadPackage(File file);

}
