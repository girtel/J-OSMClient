package com.girtel.osmclient;


import com.girtel.osmclient.utils.OSMException;
import com.girtel.osmclient.json.*;
import com.girtel.osmclient.utils.VIMConfiguration;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.OSMConstants;
import javafx.util.Pair;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


class OSMControllerR3 {

    /** OSM Release 3 endpoints **/
    private final String NS_URL = "/api/running/project/{projectname}/ns-instance-config";
    private final String NSD_URL = "/api/running/project/{projectname}/nsd-catalog/nsd";
    private final String VNF_URL = "/v1/api/operational/project/{projectname}/vnfr-catalog/vnfr";
    private final String VNFD_URL = "/api/running/project/{projectname}/vnfd-catalog/vnfd";
    private final String CONFIG_AGENT_URL = "/api/config/project/{projectname}/config-agent";
    private final String DATACENTER_LIST_URL = "/openmano/{tenant_id}/datacenters";
    private final String DEFAULTROACCOUNT_URL = "/api/operational/project/{projectname}/ro-account";
    private final String UPDATEROACCOUNT_URL = "/api/operations/update-ro-account-status";
    private final String DATACENTERS_URL = "/openmano/datacenters";
    private final String GET_TENANTS_URL = "/openmano/tenants/";
    private final String ATTACH_DETACH_DATACENTER_URL = "/openmano/{osm_id}/datacenters/{dc_id}";
    private final String UPLOAD_PACKAGE_URL = "https://{osm_ip}:8443/composer/upload?api_server=https://localhost&upload_server=https://{osm_ip}&project_name={projectname}";
    private final String CREATE_NS_URL = "/api/config/project/{projectname}/ns-instance-config/nsr";
    private final String NS_DELETE_URL = "/api/config/project/{projectname}/ns-instance-config/nsr/{ns_id}";
    private final String NSD_DELETE_URL = "/api/running/project/{projectname}/nsd-catalog/nsd/{nsd_id}";
    private final String VNFD_DELETE_URL = "/api/running/project/{projectname}/vnfd-catalog/vnfd/{vnfd_id}";

    private OSMClient osmClient;
    private String emptyJSON = "{}";
    private String osmIPAddress, project, credentials;


    protected OSMControllerR3(OSMClient osmClient)
    {
        this.osmClient = osmClient;
        this.osmIPAddress = osmClient.getOSMIPAddress();
        this.project = osmClient.getOSMProject();
        String user = osmClient.getOSMUser();
        String pass = osmClient.getOSMPassword();
        String userCred = user + ":" + pass;
        this.credentials = "Basic " + DatatypeConverter.printBase64Binary(userCred.getBytes());
        HTTPUtils.configureSecurity();
    }

    private String generateUUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public List<VirtualNetworkFunctionDescriptor> parseVFNDList()
    {

        List<VirtualNetworkFunctionDescriptor> vnfdList = new ArrayList<>();
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ VNFD_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String receivedJSON = response.getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = new JSONObject(receivedJSON);
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
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ VNF_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String receivedJSON = response.getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            if(receivedJSON != null)
            {

                JSONObject jObj = new JSONObject(receivedJSON);
                JSONArray jArray = jObj.get("vnfr:vnfr").getValue();

                for (JSONValue item : jArray)
                {
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
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ NSD_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials);
        String receivedJSON = response.getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = new JSONObject(receivedJSON);
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
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ NS_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String receivedJSON = response.getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = new JSONObject(receivedJSON);
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
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm", HTTPUtils.HTTPMethod.GET,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials);
        String osmTenant = response.getContent();

        if(!osmTenant.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObjDatacenters = new JSONObject(osmTenant);
            String tenantId = ((JSONObject)jObjDatacenters.get("tenant").getValue()).get("uuid").getValue();
            HTTPResponse vimResponse = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ DATACENTER_LIST_URL.replace("{tenant_id}",tenantId), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
            String receivedDatacenterAdvancedInfo = vimResponse.getContent();

            if(receivedDatacenterAdvancedInfo != null)
            {
                JSONObject jObj = new JSONObject(receivedDatacenterAdvancedInfo);
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
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials);
        String receivedJSON = response.getContent();
        if(!receivedJSON.equalsIgnoreCase(emptyJSON))
        {
            JSONObject jObj = new JSONObject(receivedJSON);
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
        VirtualInfrastructureManager vim = new VirtualInfrastructureManager(name,id,url,type);
        return vim;
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

        List<VirtualLinkDescriptor> vldList = new LinkedList<>();
        JSONArray vldJSON = ob.get("vld").getValue();

        for(JSONValue item : vldJSON)
        {
            JSONObject vldOb = item.getValue();
            VirtualLinkDescriptor vld = parseVLD(vldOb);
            vldList.add(vld);
        }

        JSONArray constituentVNFDJSON = ob.get("constituent-vnfd").getValue();
        List<VirtualNetworkFunctionDescriptor> constituentVNFDs = new LinkedList<>();

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

    private NetworkService parseNS(JSONObject ob)
    {
        String nsId = ob.get("id").getValue();
        String nsName = ob.get("name").getValue();
        String nsDatacenter = ob.get("rw-nsr:datacenter").getValue();
        VirtualInfrastructureManager vim = osmClient.getVIM(nsDatacenter);
        String nsDescription = "";
        String nsStatus = ob.get("admin-status").getValue();

        JSONObject nsdJSON = ob.get("nsd").getValue();
        NetworkServiceDescriptor nsNSD = parseNSD(nsdJSON);

        List<VirtualNetworkFunction> vnfs = osmClient.getVNFList();
        List<VirtualNetworkFunction> thisNS_VNFs = vnfs.stream().filter(vnf -> vnf.getNSID().equalsIgnoreCase(nsId)).collect(Collectors.toList());

        NetworkService ns = new NetworkService(nsId,nsName,nsDescription,nsStatus,vim,nsNSD,thisNS_VNFs);

        return ns;

    }

    public HTTPResponse deleteNS(String name)
    {
        HTTPResponse response = null;
        NetworkService nsToDelete = osmClient.getNS(name);
        if(nsToDelete == null)
        {
            throw new OSMException("No Network Service named "+name+" instantiated in OSM");
        }
        else {
            String nsIdToDelete = nsToDelete.getId();
            response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008" + NS_DELETE_URL.replace("{ns_id}",nsIdToDelete).replace("{projectname}",project), HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        }

        return response;
    }

    public HTTPResponse deleteNSD(String name)
    {
        HTTPResponse response = null;
        NetworkServiceDescriptor nsdToDelete = osmClient.getNSD(name);
        if(nsdToDelete == null)
        {
            throw new OSMException("No Network Service Descriptor named "+name+" stored in OSM");
        }
        else {
            String nsdIdToDelete = nsdToDelete.getId();
            response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008" + NSD_DELETE_URL.replace("{nsd_id}",nsdIdToDelete).replace("{projectname}",project), HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        }

        return response;

    }

    public HTTPResponse deleteVNFD(String name)
    {
        HTTPResponse response = null;
        VirtualNetworkFunctionDescriptor vnfdToDelete = osmClient.getVNFD(name);
        if(vnfdToDelete == null)
        {
            throw new OSMException("No VNFD named "+name+" stored in OSM");
        }
        else {
            String vnfdIdToDelete = vnfdToDelete.getId();
            response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008" + NS_DELETE_URL.replace("{vnfd_id}",vnfdIdToDelete).replace("{projectname}",project), HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        }

        return response;

    }

    public HTTPResponse deleteConfigAgent(String name)
    {
        return HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL.replace("{projectname}",project)+"/account/"+name, HTTPUtils.HTTPMethod.DELETE,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials);
    }

    public HTTPResponse deleteVIM(String name)
    {
        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm", HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String osmTenant = response.getContent();
        JSONObject tenantJSONOb = new JSONObject(osmTenant);
        String tenantId = ((JSONObject)tenantJSONOb.get("tenant").getValue()).get("uuid").getValue();

        HTTPResponse detachResponse = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090" + ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}", name), HTTPUtils.HTTPMethod.DELETE,OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        HTTPResponse deleteResponse = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ DATACENTERS_URL+"/"+name, HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        HTTPResponse defaultROAccountResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ DEFAULTROACCOUNT_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String roAccJSON = defaultROAccountResponse.getContent();
        JSONObject defaultROJSON = new JSONObject(roAccJSON);
        defaultROJSON = ((JSONArray)((JSONObject)defaultROJSON.get("rw-ro-account:ro-account").getValue()).get("account").getValue()).get(0).getValue();
        String defaultROType = defaultROJSON.get("ro-account-type").getValue();

        HTTPResponse finalResponse = null;

        if(!defaultROType.equals("openmano"))
        {
            throw new OSMException("openmano is not default account in OSM");
        }
        else {

            String defaultROName = defaultROJSON.get("name").getValue();

            JSONObject refreshBodyJSON = new JSONObject();
            JSONObject updateROJSON = new JSONObject();
            updateROJSON.put("ro-account", new JSONValue(defaultROName));
            updateROJSON.put("project-name", new JSONValue("default"));
            refreshBodyJSON.put("input",new JSONValue(updateROJSON));

            finalResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ UPDATEROACCOUNT_URL, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials, refreshBodyJSON);
        }

        return finalResponse;
    }

    public HTTPResponse createVIM(String name, String description, OSMConstants.OSMVimType osmVimType, String user, String password, String authURL, String tenant, VIMConfiguration... VIMConfiguration)
    {
        if(VIMConfiguration.length > 1)
            throw new RuntimeException("No more than one VIMConfiguration is allowed");

        JSONObject finalJSON = new JSONObject();
        JSONObject dataCenterJSON = new JSONObject();

        dataCenterJSON.put("name",new JSONValue(name));
        dataCenterJSON.put("type",new JSONValue(osmVimType.toString()));

        JSONObject configJSON = (VIMConfiguration.length == 1) ? VIMConfiguration[0].toJSON() : new JSONObject();
        if(!configJSON.isEmpty())
            dataCenterJSON.put("config",new JSONValue(configJSON));

        dataCenterJSON.put("vim_url",new JSONValue(authURL));
        dataCenterJSON.put("vim_url_admin",new JSONValue(authURL));
        dataCenterJSON.put("description",new JSONValue(description));
        dataCenterJSON.put("vim_username",new JSONValue(user));
        dataCenterJSON.put("vim_password",new JSONValue(password));
        dataCenterJSON.put("vim_tenant_name",new JSONValue(tenant));

        finalJSON.put("datacenter",new JSONValue(dataCenterJSON));

        HTTPResponse createVIMResponse = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ DATACENTERS_URL, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials, finalJSON);
        String createDatacenterResponse = createVIMResponse.getContent();

        HTTPResponse osmTenantResponse = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm", HTTPUtils.HTTPMethod.GET,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials);
        String osmTenant = osmTenantResponse.getContent();

        JSONObject datacenterJSON = new JSONObject(createDatacenterResponse);
        JSONObject tenantJSON = new JSONObject(osmTenant);

        String datacenterId = ((JSONObject)datacenterJSON.get("datacenter").getValue()).get("uuid").getValue();
        String tenantId = ((JSONObject)tenantJSON.get("tenant").getValue()).get("uuid").getValue();

        HTTPResponse response = HTTPUtils.establishHTTPConnectionWithOSM("http://"+osmIPAddress+":9090"+ ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterId), HTTPUtils.HTTPMethod.POST,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials, finalJSON);

        if(response.getCode() > 299)
        {
            try {
                throw new OSMException("Error!! Unable to attach datacenter to OSM");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HTTPResponse defaultROAccountResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ DEFAULTROACCOUNT_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String roAccJSON = defaultROAccountResponse.getContent();

        JSONObject defaultROJSON = new JSONObject(roAccJSON);

        defaultROJSON = ((JSONArray)((JSONObject)defaultROJSON.get("rw-ro-account:ro-account").getValue()).get("account").getValue()).get(0).getValue();

        String defaultROType = defaultROJSON.get("ro-account-type").getValue();

        HTTPResponse finalResponse;
        if(!defaultROType.equals("openmano"))
        {
            throw new OSMException("openmano is not default account in OSM");
        }
        else {

            String defaultROName = defaultROJSON.get("name").getValue();

            JSONObject refreshBodyJSON = new JSONObject();
            JSONObject updateROJSON = new JSONObject();
            updateROJSON.put("ro-account", new JSONValue(defaultROName));
            updateROJSON.put("project-name", new JSONValue("default"));
            refreshBodyJSON.put("input",new JSONValue(updateROJSON));

            finalResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ UPDATEROACCOUNT_URL, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials, refreshBodyJSON);
        }

        return finalResponse;
    }

    public HTTPResponse uploadPackage(File file)
    {
        return HTTPUtils.establishHTTPConnectionWithOSM(UPLOAD_PACKAGE_URL.replace("{osm_ip}",osmIPAddress).replace("{projectname}",project), HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials, file);
    }

    public HTTPResponse createNS(String name, String nsdName, String datacenter)
    {
        HTTPResponse nsdResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ NSD_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials);
        String nsdJSON = nsdResponse.getContent();

        JSONObject nsdJSONOb = new JSONObject(nsdJSON);
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
                throw new OSMException("NSD named "+nsdName+" not found");
            } catch (Exception e) {
                e.printStackTrace();
            }

        JSONObject nsJSON = new JSONObject();
        JSONObject postJSON = new JSONObject();
        JSONArray nsrArray = new JSONArray();

        nsJSON.put("id", new JSONValue(generateUUID()));
        nsJSON.put("nsd",new JSONValue(finalNSDJSON));
        nsJSON.put("name",new JSONValue(name));
        nsJSON.put("short-name",new JSONValue(name));
        nsJSON.put("description",new JSONValue("default"));
        nsJSON.put("admin-status",new JSONValue("ENABLED"));
        nsJSON.put("resource-orchestrator",new JSONValue("osmopenmano"));
        nsJSON.put("datacenter",new JSONValue(datacenter));

        nsrArray.add(new JSONValue(nsJSON));
        postJSON.put("nsr",new JSONValue(nsrArray));

        return HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008"+ CREATE_NS_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.POST,OSMConstants.OSMClientVersion.RELEASE_THREE, true,  credentials, postJSON);
    }

    public HTTPResponse createConfigAgent(String name, OSMConstants.OSMConfigAgentType type, String serverIP, String user, String secret)
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

        return HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":8008" + CONFIG_AGENT_URL.replace("{projectname}",project), HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.RELEASE_THREE, true, credentials, finalJSON);
    }

}
