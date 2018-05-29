package com.girtel.osmclient;


import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.OSMConstants;
import com.girtel.osmclient.utils.UUIDUtils;
import com.shc.easyjson.*;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


class OSMController {

    private OSMAPIConnector osmConnector;
    private OSMClient osmClient;
    private String emptyJSON = "{}";


    protected OSMController(OSMClient osmClient)
    {
        this.osmClient = osmClient;
        this.osmConnector = new OSMAPIConnector(osmClient);
    }


    public List<VirtualNetworkFunctionDescriptor> parseVFNDList()
    {

        List<VirtualNetworkFunctionDescriptor> vnfdList = new ArrayList<>();
        String receivedJSON = osmConnector.establishConnectionToReceiveVNFDList().getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(receivedJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jarray = jObj.get("project-vnfd:vnfd").getValue();

            for(JSONValue item : jarray)
            {
                JSONObject ob = item.getValue();
                VirtualNetworkFunctionDescriptor vnfd = parseVNFD(ob);
                vnfdList.add(vnfd);
            }
        }

        return vnfdList;
    }

    public List<VirtualNetworkFunction> parseVNFList()
    {
        List<VirtualNetworkFunction> vnfList = new ArrayList<>();
        String receivedJSON = osmConnector.establishConnectionToReceiveVNFList().getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = null;

            if(receivedJSON != null) {
                try {
                    jObj = JSON.parse(receivedJSON);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONArray jArray = jObj.get("vnfr:vnfr").getValue();
                for (JSONValue item : jArray) {
                    JSONObject ob = item.getValue();
                    VirtualNetworkFunction vnf = parseVNF(ob);
                    vnfList.add(vnf);

                }
            }
        }

        return vnfList;
    }

    public List<NetworkServiceDescriptor> parseNSDList()
    {
        List<NetworkServiceDescriptor> nsdList = new ArrayList<>();
        String receivedJSON = osmConnector.establishConnectionToReceiveNSDList().getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(receivedJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jArray = jObj.get("project-nsd:nsd").getValue();
            for(JSONValue item: jArray)
            {
                JSONObject ob = item.getValue();
                NetworkServiceDescriptor nsd = parseNSD(ob);
                nsdList.add(nsd);

            }
        }

        return nsdList;
    }

    public List<NetworkService> parseNSList()
    {
        List<NetworkService> nsList = new ArrayList<>();
        String receivedJSON = osmConnector.establishConnectionToReceiveNSList().getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(receivedJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONObject json = jObj.get("nsr:ns-instance-config").getValue();

            JSONValue nsr = json.get("nsr");

            if(nsr != null)
            {
                JSONArray jArray = nsr.getValue();

                for (JSONValue item : jArray)
                {
                    JSONObject ob = item.getValue();
                    NetworkService ns = parseNS(ob);
                    nsList.add(ns);

                }

            }
        }

        return nsList;

    }

    public List<VirtualInfrastructureManager> parseVIMList()
    {
        List<VirtualInfrastructureManager> datacenterList = new ArrayList<>();
        String osmTenant = osmConnector.establishConnectionToReceiveOSMTenant().getContent();

        if(!osmTenant.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObjDatacenters = null;
            try {
                jObjDatacenters = JSON.parse(osmTenant);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String tenantId = ((JSONObject)jObjDatacenters.get("tenant").getValue()).get("uuid").getValue();
            String receivedDatacenterAdvancedInfo = osmConnector.establishConnectionToReceiveDatacenterList(tenantId).getContent();

            if(receivedDatacenterAdvancedInfo != null)
            {
                JSONObject jObj = null;
                try {
                    jObj = JSON.parse(receivedDatacenterAdvancedInfo);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                JSONArray dataCenterArray = jObj.get("datacenters").getValue();

                for(JSONValue dcJSON : dataCenterArray)
                {
                    JSONObject jOb = dcJSON.getValue();
                    VirtualInfrastructureManager dc = parseVIM(jOb);
                    datacenterList.add(dc);
                }
            }

        }

        return datacenterList;
    }

    public List<ConfigAgent> parseConfigAgentList()
    {
        List<ConfigAgent> configAgentList = new ArrayList<>();
        String receivedJSON = osmConnector.establishConnectionToReceiveConfigAgentList().getContent();

        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(receivedJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jArray = ((JSONObject)jObj.get("rw-config-agent:config-agent").getValue()).get("account").getValue();

            for(JSONValue item : jArray)
            {
                JSONObject ob = item.getValue();
                ConfigAgent ca = parseConfigAgent(ob);
                configAgentList.add(ca);
            }
        }

        return configAgentList;
    }

    private ConfigAgent parseConfigAgent(JSONObject ob)
    {
        String name = ob.get("name").getValue();
        String type = ob.get("account-type").getValue();
        JSONObject infoOb = ob.get(type).getValue();

        String user = infoOb.get("user").getValue();
        String ipAddress = infoOb.get("ip-address").getValue();
        String port = infoOb.get("port").getValue().toString();
        port = port.substring(0,port.length() - 2);

        ConfigAgent ca = new ConfigAgent(name, type, user, ipAddress, port);
        return ca;

    }

    private VirtualDeploymentUnit parseVDU(JSONObject vduOb) {

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

        String id = ob.get("id").getValue();
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

    private VirtualInfrastructureManager parseVIM(JSONObject ob)
    {
        String name = ob.get("name").getValue();
        String id = ob.get("uuid").getValue();
        String url = ob.get("vim_url").getValue();
        String type = ob.get("type").getValue();
        VirtualInfrastructureManager dc = new VirtualInfrastructureManager(name,id,url,type);
        return dc;
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
        String nsdId = ob.get("id").getValue();
        String nsdName = ob.get("name").getValue();

        List<VirtualLinkDescriptor> vldList = new ArrayList<>();
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
            VirtualNetworkFunctionDescriptor cVNFD = osmClient.getVNFDById(cVNFDId);
            constituentVNFDs.add(cVNFD);
        }


        NetworkServiceDescriptor nsd = new NetworkServiceDescriptor(nsdId,nsdName,"",constituentVNFDs,vldList);
        return nsd;

    }

    public NetworkService parseNS(JSONObject ob)
    {
        String nsId = ob.get("id").getValue();
        String nsName = ob.get("name").getValue();
        String nsDatacenter = ob.get("rw-nsr:datacenter").getValue();
        String nsDescription = "";
        String nsStatus = ob.get("admin-status").getValue();

        JSONObject nsdJSON = ob.get("nsd").getValue();
        NetworkServiceDescriptor nsNSD = parseNSD(nsdJSON);

        List<VirtualNetworkFunction> vnfs = osmClient.getVNFList();
        List<VirtualNetworkFunction> thisNS_VNFs = vnfs.stream().filter(vnf -> vnf.getNSID().equalsIgnoreCase(nsId)).collect(Collectors.toList());

        NetworkService ns = new NetworkService(nsId,nsName,nsDescription,nsStatus,nsDatacenter,nsNSD,thisNS_VNFs);

        return ns;

    }

    public HTTPResponse deleteNS(String name)
    {
        String nsJSON = osmConnector.establishConnectionToReceiveNSList().getContent();
        List<NetworkService> nsList = osmClient.getNSList();
        HTTPResponse response;

        if(nsList.size() == 0)
        {
            response = HTTPResponse.EMPTY_RESPONSE;
        }
        else{
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(nsJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONObject json = jObj.get("nsr:ns-instance-config").getValue();
            JSONArray jArray = json.get("nsr").getValue();

            String nsIdToDelete = "";

            for(JSONValue item : jArray)
            {
                JSONObject ob = item.getValue();
                String nsName = ob.get("name").getValue();
                if(nsName.equals(name))
                {
                    nsIdToDelete = ob.get("id").getValue();
                    break;
                }
            }


            if(nsIdToDelete.equals(""))
            {
                response = HTTPResponse.EMPTY_RESPONSE;
            }
            else
            {
                response = osmConnector.establishConnectionToDeleteNS(nsIdToDelete);
            }

        }

        return response;
    }

    public HTTPResponse deleteNSD(String name)
    {
        HTTPResponse response;
        String nsdJSON = osmConnector.establishConnectionToReceiveNSDList().getContent();
        List<NetworkServiceDescriptor> nsdList = osmClient.getNSDList();
        if(nsdList.size() == 0)
            response = HTTPResponse.EMPTY_RESPONSE;
        else{
            JSONObject jObj = null;
            try {
                jObj = JSON.parse(nsdJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jArray = jObj.get("project-nsd:nsd").getValue();

            String nsdIdToDelete = "";

            for(JSONValue item : jArray)
            {
                JSONObject ob = item.getValue();
                String nsName = ob.get("name").getValue();
                if(nsName.equals(name))
                {
                    nsdIdToDelete = ob.get("id").getValue();
                    break;
                }
            }

            if(nsdIdToDelete.equals(""))
            {
                response = HTTPResponse.EMPTY_RESPONSE;
            }
            else
            {
                response = osmConnector.establishConnectionToDeleteNSD(nsdIdToDelete);
            }
        }

        return response;

    }

    public HTTPResponse deleteVNFD(String name)
    {
        HTTPResponse response;
        String vnfdJSON = osmConnector.establishConnectionToReceiveVNFDList().getContent();
        List<VirtualNetworkFunctionDescriptor> vnfdList = osmClient.getVNFDList();
        if(vnfdList.size() == 0)
            response = HTTPResponse.EMPTY_RESPONSE;
        else{

            JSONObject jObj = null;
            try {
                jObj = JSON.parse(vnfdJSON);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jArray = jObj.get("project-vnfd:vnfd").getValue();

            String vnfdIdToDelete = "";

            for(JSONValue item : jArray)
            {
                JSONObject ob = item.getValue();
                String nsName = ob.get("name").getValue();
                if(nsName.equals(name))
                {
                    vnfdIdToDelete = ob.get("id").getValue();
                    break;
                }
            }

            if(vnfdIdToDelete.equals(""))
            {
                response = HTTPResponse.EMPTY_RESPONSE;
            }
            else
            {
                response = osmConnector.establishConnectionToDeleteVNFD(vnfdIdToDelete);
            }
        }

        return response;

    }


    public HTTPResponse deleteConfigAgent(String name)
    {
        HTTPResponse response = osmConnector.establishConnectionToDeleteConfigAgent(name);
        return response;
    }

    public HTTPResponse deleteVIM(String name)
    {
        String tenantJSON = osmConnector.establishConnectionToReceiveOSMTenant().getContent();

        JSONObject tenantJSONOb = null;

        try {
            tenantJSONOb = JSON.parse(tenantJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String tenantId = ((JSONObject)tenantJSONOb.get("tenant").getValue()).get("uuid").getValue();

        osmConnector.establishConnectionToDetachDatacenter(tenantId, name).getContent();

        osmConnector.establishConnectionToDeleteDatacenter(name);

        String roAccJSON = osmConnector.establishConnectionToReceiveDefaultROAccount().getContent();

        JSONObject defaultROJSON = null;

        try {
            defaultROJSON = JSON.parse(roAccJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        defaultROJSON = ((JSONArray)((JSONObject)defaultROJSON.get("rw-ro-account:ro-account").getValue()).get("account").getValue()).get(0).getValue();

        String defaultROType = defaultROJSON.get("ro-account-type").getValue();

        HTTPResponse finalResponse = null;
        if(!defaultROType.equals("openmano"))
        {
            finalResponse = HTTPResponse.errorResponse("Error, openmano is not default account");
        }
        else {

            String defaultROName = defaultROJSON.get("name").getValue();

            JSONObject refreshBodyJSON = new JSONObject();
            JSONObject updateROJSON = new JSONObject();
            updateROJSON.put("ro-account", new JSONValue(defaultROName));
            updateROJSON.put("project-name", new JSONValue("default"));
            refreshBodyJSON.put("input",new JSONValue(updateROJSON));

            finalResponse = osmConnector.establishConnectionToUpdateROAccount(refreshBodyJSON);
        }

        return finalResponse;
    }

    public HTTPResponse createVIM(String name, OSMConstants.OSMVimType osmVimType, String user, String password, String authURL, String tenant, boolean usingFloatingIPs, String... keyPairName)
    {
        JSONObject finalJSON = new JSONObject();
        JSONObject dataCenterJSON = new JSONObject();

        dataCenterJSON.put("name",new JSONValue(name));
        dataCenterJSON.put("type",new JSONValue(osmVimType.toString()));

        JSONObject configJSON = new JSONObject();
        configJSON.put("use_floating_ip",new JSONValue(usingFloatingIPs));
        if(keyPairName.length == 1)
            configJSON.put("keypair",new JSONValue(keyPairName[0]));

        dataCenterJSON.put("config",new JSONValue(configJSON));
        dataCenterJSON.put("vim_url",new JSONValue(authURL));
        dataCenterJSON.put("vim_url_admin",new JSONValue(authURL));
        dataCenterJSON.put("description",new JSONValue("default"));
        dataCenterJSON.put("vim_username",new JSONValue(user));
        dataCenterJSON.put("vim_password",new JSONValue(password));
        dataCenterJSON.put("vim_tenant_name",new JSONValue(tenant));

        finalJSON.put("datacenter",new JSONValue(dataCenterJSON));

        String createDatacenterResponse = osmConnector.establishConnectionToCreateDatacenter(finalJSON).getContent();
        String osmTenant = osmConnector.establishConnectionToReceiveOSMTenant().getContent();

        JSONObject datacenterJSON = null;
        JSONObject tenantJSON = null;

        try {
            datacenterJSON = JSON.parse(createDatacenterResponse);
            tenantJSON = JSON.parse(osmTenant);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String datacenterId = ((JSONObject)datacenterJSON.get("datacenter").getValue()).get("uuid").getValue();
        String tenantId = ((JSONObject)tenantJSON.get("tenant").getValue()).get("uuid").getValue();

        HTTPResponse response = osmConnector.establishConnectionToAttachDatacenterToOSM(tenantId, datacenterId, finalJSON);

        if(response.getCode() > 299)
        {
            try {
                throw new RuntimeException("Error!! Unable to attach datacenter to OSM");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String roAccJSON = osmConnector.establishConnectionToReceiveDefaultROAccount().getContent();

        JSONObject defaultROJSON = null;

        try {
            defaultROJSON = JSON.parse(roAccJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        defaultROJSON = ((JSONArray)((JSONObject)defaultROJSON.get("rw-ro-account:ro-account").getValue()).get("account").getValue()).get(0).getValue();

        String defaultROType = defaultROJSON.get("ro-account-type").getValue();

        HTTPResponse finalResponse = null;
        if(!defaultROType.equals("openmano"))
        {
            finalResponse = HTTPResponse.errorResponse("Error, openmano is not default account");
        }
        else {

            String defaultROName = defaultROJSON.get("name").getValue();

            JSONObject refreshBodyJSON = new JSONObject();
            JSONObject updateROJSON = new JSONObject();
            updateROJSON.put("ro-account", new JSONValue(defaultROName));
            updateROJSON.put("project-name", new JSONValue("default"));
            refreshBodyJSON.put("input",new JSONValue(updateROJSON));

            finalResponse = osmConnector.establishConnectionToUpdateROAccount(refreshBodyJSON);
        }

        return finalResponse;
    }

    public HTTPResponse uploadPackage(File file)
    {
        HTTPResponse response = osmConnector.establishConnectionToUploadPackageToOSM(file);
        return response;
    }

    public HTTPResponse createNS(String name, String nsdName, String datacenter)
    {
        String nsdJSON = osmConnector.establishConnectionToReceiveNSDList().getContent();

        JSONObject nsdJSONOb = null;
        try {
            nsdJSONOb = JSON.parse(nsdJSON);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray jArray = nsdJSONOb.get("project-nsd:nsd").getValue();
        boolean containsNSD = false;

        JSONObject finalNSDJSON = new JSONObject();

        for(JSONValue item : jArray)
        {
            JSONObject ob = item.getValue();
            String this_name = ob.get("name").getValue();
            if (nsdName.equals(this_name))
            {
                finalNSDJSON = ob;
                containsNSD = true;
                break;
            }

        }

        if(!containsNSD)
            try {
                throw new RuntimeException("NSD named "+nsdName+" not found");
            } catch (Exception e) {
                e.printStackTrace();
            }

        JSONObject nsJSON = new JSONObject();
        JSONObject postJSON = new JSONObject();
        JSONArray nsrArray = new JSONArray();

        nsJSON.put("id", new JSONValue(UUIDUtils.generateUUID()));
        nsJSON.put("nsd",new JSONValue(finalNSDJSON));
        nsJSON.put("name",new JSONValue(name));
        nsJSON.put("short-name",new JSONValue(name));
        nsJSON.put("description",new JSONValue("default"));
        nsJSON.put("admin-status",new JSONValue("ENABLED"));
        nsJSON.put("resource-orchestrator",new JSONValue("osmopenmano"));
        nsJSON.put("datacenter",new JSONValue(datacenter));

        nsrArray.add(new JSONValue(nsJSON));
        postJSON.put("nsr",new JSONValue(nsrArray));

        return osmConnector.establishConnectionToCreateNS(postJSON);

    }

    public HTTPResponse addConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret)
    {
        JSONObject finalJSON = new JSONObject();
        JSONArray accountArray = new JSONArray();
        JSONObject accountJSON = new JSONObject();
        JSONObject configJSON = new JSONObject();

        configJSON.put("user",new JSONValue(user));
        configJSON.put("secret",new JSONValue(secret));
        configJSON.put("ip-address",new JSONValue(serverIP));

        accountJSON.put("name",new JSONValue(name));
        accountJSON.put("account-type",new JSONValue(type.toString()));
        accountJSON.put(type.toString(),new JSONValue(configJSON));

        accountArray.add(new JSONValue(accountJSON));

        finalJSON.put("account",new JSONValue(accountArray));

        HTTPResponse response = osmConnector.establishConnectionToAddConfigAgent(finalJSON);
        return response;

    }

}
