package com.girtel.osmclient;

import com.girtel.osmclient.json.*;
import com.girtel.osmclient.utils.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OSMController005
{
    private OSMAPIConnector005 osmConnector005;
    private OSMClient osmClient;
    private String emptyJSON = "{}";

    protected OSMController005(OSMClient osmClient)
    {
       this.osmClient = osmClient;
       this.osmConnector005 = new OSMAPIConnector005(osmClient);
    }

    protected HTTPResponse createSessionToken()
    {
        return osmConnector005.establishConnectionToCreateSessionToken();
    }

    protected List<VirtualInfrastructureManager> parseVIMList()
    {
        List<VirtualInfrastructureManager> vims = new LinkedList<>();
        HTTPResponse vimResponse = osmConnector005.establishConnectionToReceiveVIMList();
        String vimResponseContent = vimResponse.getContent();

        JSONArray vimsArray = new JSONArray(vimResponseContent);
        for(JSONValue item : vimsArray)
        {
            JSONObject ob = item.getValue();
            VirtualInfrastructureManager vim = parseVIM(ob);
            vims.add(vim);
        }
        return vims;
    }

    protected List<VirtualNetworkFunctionDescriptor> parseVNFDList()
    {
        List<VirtualNetworkFunctionDescriptor> vnfds = new LinkedList<>();
        HTTPResponse vnfdResponse = osmConnector005.establishConnectionToReceiveVNFDList();
        String vnfdResponseContent = vnfdResponse.getContent();

        JSONArray vnfdsArray = new JSONArray(vnfdResponseContent);
        for(JSONValue item : vnfdsArray)
        {
            JSONObject ob = item.getValue();
            VirtualNetworkFunctionDescriptor vnfd = parseVNFD(ob);
            vnfds.add(vnfd);
        }
        return vnfds;
    }

    protected List<NetworkServiceDescriptor> parseNSDList()
    {
        List<NetworkServiceDescriptor> nsds = new LinkedList<>();
        HTTPResponse nsdResponse = osmConnector005.establishConnectionToReceiveNSDList();
        String nsdResponseContent = nsdResponse.getContent();
        String nsdResponseContent_mod = nsdResponseContent.replace(",            \"userDefinedData\": {}","");

        JSONArray nsdsArray = new JSONArray(nsdResponseContent_mod);
        for(JSONValue item : nsdsArray)
        {
            JSONObject ob = item.getValue();
            NetworkServiceDescriptor nsd = parseNSD(ob);
            nsds.add(nsd);
        }
        return nsds;
    }

    protected List<NetworkService> parseNSList()
    {
        List<NetworkService> nss = new LinkedList<>();
        HTTPResponse nsResponse = osmConnector005.establishConnectionToReceiveNSList();
        String nsResponseContent = nsResponse.getContent();
        System.out.println(nsResponseContent);
        String nsResponseContent_mod = nsResponseContent.replace("\"orchestration-progress\": {},","").replace(",                \"userDefinedData\": {}","");

        JSONArray nssArray = new JSONArray(nsResponseContent_mod);
        for(JSONValue item : nssArray)
        {
            JSONObject ob = item.getValue();
            NetworkService ns = parseNS(ob);
            nss.add(ns);
        }
        return nss;
    }

    protected HTTPResponse createNS(String name, String nsdName, String vim, NSConfiguration... nsConfiguration)
    {
        JSONObject nsJSON = new JSONObject();
        nsJSON.put("nsDescription",new JSONValue("default"));
        String vimId = osmClient.getVIM(vim).getID();
        String nsdId = osmClient.getNSD(nsdName).getId();
        nsJSON.put("vimAccountId",new JSONValue(vimId));
        nsJSON.put("nsdId", new JSONValue(nsdId));
        nsJSON.put("nsName", new JSONValue(name));

        return osmConnector005.establishConnectionToCreateNS(nsJSON);
    }

    protected HTTPResponse deleteNS(String name)
    {
        String nsId = osmClient.getNS(name).getId();
        return osmConnector005.establishConnectionToDeleteNS(nsId);
    }

    private VirtualInfrastructureManager parseVIM(JSONObject ob)
    {
        String name = ob.get("name").getValue();
        String id = ob.get("_id").getValue();
        String url = ob.get("vim_url").getValue();
        String type = ob.get("vim_type").getValue();
        VirtualInfrastructureManager vim = new VirtualInfrastructureManager(name,id,url,type);
        return vim;
    }

    private VirtualDeploymentUnit parseVDU(JSONObject vduOb)
    {

        String vduId = vduOb.get("id").getValue();
        String vduName = vduOb.get("name").getValue();
        String vduImage = vduOb.get("image").getValue();

        JSONObject flavorJSON = vduOb.get("vm-flavor").getValue();
        double storageGb = flavorJSON.get("storage-gb").getValue();
        double vcpuCount = flavorJSON.get("vcpu-count").getValue();
        double memoryMb = flavorJSON.get("memory-mb").getValue();

        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit(vduId, vduName, vduImage,storageGb,vcpuCount,memoryMb);

        return vdu;
    }


    private ConnectionPoint parseConnectionPoint(JSONObject ob)
    {
        String cpName = ob.get("name").getValue();
        String cpMACAddress = ob.get("mac-address").getValue();
        String cpIPAddress = ob.get("ip-address").getValue();

        ConnectionPoint connPoint = new ConnectionPoint(cpName, cpMACAddress, cpIPAddress);
        return connPoint;
    }

    private Pair<String, String> parseConnectionPointRef(JSONObject ob)
    {
        String vnfdConnPointName = ob.get("vnfd-connection-point-ref").getValue();
        String vnfdId = ob.get("vnfd-id-ref").getValue();
        Pair<String, String> cpRef = new Pair(vnfdId,vnfdConnPointName);
        return cpRef;
    }

    private VirtualNetworkFunctionDescriptor parseVNFD(JSONObject ob)
    {

        String id = ob.get("_id").getValue();
        String name = ob.get("name").getValue();
        String description = ob.get("description").getValue();
        JSONArray vduJSON = ob.get("vdu").getValue();

        List<VirtualDeploymentUnit> vduList = new ArrayList<>();

        for(JSONValue vduItem : vduJSON)
        {
            JSONObject vduOb = vduItem.getValue();
            VirtualDeploymentUnit vdu = parseVDU(vduOb);
            vduList.add(vdu);
        }

        VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor(id,name,description,vduList);
        return vnfd;

    }

    private VirtualNetworkFunction parseVNF(JSONObject ob)
    {
        String id = ob.get("id").getValue();
        String name = ob.get("name").getValue();
        String description = ob.get("description").getValue();
        String status = ob.get("operational-status").getValue();

        JSONObject vnfdJSON = ob.get("vnfd").getValue();


        VirtualNetworkFunctionDescriptor finalVNFD = parseVNFD(vnfdJSON);


        JSONValue cpValue = ob.get("connection-point");
        List<ConnectionPoint> connPointList = new ArrayList<>();

        if(cpValue != null)
        {
            JSONArray cpJSON = ob.get("connection-point").getValue();

            for(JSONValue cp : cpJSON)
            {
                JSONObject cpJSONOb = cp.getValue();
                ConnectionPoint connPoint = parseConnectionPoint(cpJSONOb);
                connPointList.add(connPoint);
            }
        }



        JSONValue monParamValue = ob.get("monitoring-param");
        List<MonitoringParameter> monParamList = new ArrayList<>();

        if(monParamValue != null) {

            JSONArray monParamJSON = monParamValue.getValue();

            for (JSONValue mp : monParamJSON) {
                JSONObject mpJSONOb = mp.getValue();
                MonitoringParameter monParam = parseMonitoringParameter(mpJSONOb);
                monParamList.add(monParam);
            }

        }

        String nsId = ob.get("nsr-id-ref").getValue();

        VirtualNetworkFunction vnf = new VirtualNetworkFunction(id, name, description, status, nsId, finalVNFD, connPointList, monParamList);
        return vnf;

    }

    private VirtualLinkDescriptor parseVLD(JSONObject ob)
    {
        String vldId = ob.get("id").getValue();
        String vldName = ob.get("name").getValue();
        JSONValue vldDescriptionValue = ob.get("description");
        String vldDescription = "";
        if(vldDescriptionValue != null)
            vldDescription = ob.get("description").getValue();
        else
            vldDescription = "No description";

        boolean vldIsMgmtNetwork = Boolean.parseBoolean(ob.get("mgmt-network").getValue());
        JSONArray connPointRefJSON = ob.get("vnfd-connection-point-ref").getValue();

        List<Pair<String, String>> connPointRefList = new ArrayList<>();

        for(JSONValue item : connPointRefJSON)
        {
            JSONObject cpRefOb = item.getValue();
            Pair<String, String> cpRef = parseConnectionPointRef(cpRefOb);
            connPointRefList.add(cpRef);
        }

        VirtualLinkDescriptor vld = new VirtualLinkDescriptor(vldId, vldName, vldDescription, connPointRefList, vldIsMgmtNetwork);
        return vld;
    }

    private MonitoringParameter parseMonitoringParameter(JSONObject ob)
    {
        String mpId = ob.get("id").getValue();
        String mpName = ob.get("name").getValue();
        String mpDescription = ob.get("description").getValue();
        String mpUnits = ob.get("units").getValue();

        String mpType = ob.get("value-type").getValue();
        String mpValue = "";

        if(mpType.equals("INT"))
        {
            mpValue = ob.get("value-integer").getValue();
        }
        else if(mpType.equals("STRING"))
        {
            mpValue = ob.get("value-string").getValue();
        }

        MonitoringParameter mp = new MonitoringParameter(mpId, mpName, mpValue, mpUnits, mpDescription);
        return mp;

    }

    private NetworkServiceDescriptor parseNSD(JSONObject ob)
    {
        String nsdId = ob.get("_id").getValue();
        String nsdName = ob.get("name").getValue();

        /*List<VirtualLinkDescriptor> vldList = new ArrayList<>();
        JSONArray vldJSON = ob.get("vld").getValue();

        for(JSONValue item : vldJSON)
        {
            JSONObject vldOb = item.getValue();
            VirtualLinkDescriptor vld = parseVLD(vldOb);
            vldList.add(vld);
        }

        JSONArray constituentVNFDJSON = ob.get("constituent-vnfd").getValue();
        List<VirtualNetworkFunctionDescriptor> constituentVNFDs = new ArrayList<>();

        for(JSONValue item: constituentVNFDJSON)
        {
            JSONObject cVNFDOb = item.getValue();
            String cVNFDId = cVNFDOb.get("vnfd-id-ref").getValue();
            //VirtualNetworkFunctionDescriptor cVNFD = osmClient005.getVNFDById(cVNFDId);
            //constituentVNFDs.add(cVNFD);
        }
        */

        NetworkServiceDescriptor nsd = new NetworkServiceDescriptor(nsdId,nsdName,"",null,null);
        return nsd;

    }

    private NetworkService parseNS(JSONObject ob)
    {
        String nsId = ob.get("id").getValue();
        String nsName = ob.get("name").getValue();
        String nsDatacenter = ob.get("datacenter").getValue();
        String nsDescription = "";
        String nsStatus = ob.get("admin-status").getValue();


        NetworkService ns = new NetworkService(nsId,nsName,nsDescription,nsStatus,nsDatacenter,null,null);

        return ns;

    }

}
