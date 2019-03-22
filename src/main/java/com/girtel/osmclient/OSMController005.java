package com.girtel.osmclient;

import com.girtel.osmclient.json.*;
import com.girtel.osmclient.utils.*;
import javafx.util.Pair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OSMController005
{
    /** OSM sol005 endpoints **/
    private String TOKEN_URL_005 = "/osm/admin/v1/tokens";
    private String VIM_URL_005 = "/osm/admin/v1/vim_accounts";
    private String VNFD_URL_005 = "/osm/vnfpkgm/v1/vnf_packages";
    private String VNF_URL_005 = "/osm/nslcm/v1/vnfrs";
    private String NSD_URL_005 = "/osm/nsd/v1/ns_descriptors";
    private String NS_URL_005 = "/osm/nslcm/v1/ns_instances_content";
    private String UPLOAD_VNFD_URL_005 = "/osm/vnfpkgm/v1/vnf_packages_content";
    private String UPLOAD_NSD_URL_005 = "/osm/nsd/v1/ns_descriptors_content";

    private OSMClient osmClient;
    private String osmIPAddress, credentials;
    private JSONObject authJSON;
    private String emptyJSON = "{}";


    protected OSMController005(OSMClient osmClient)
    {
       this.osmClient = osmClient;
       this.osmIPAddress = osmClient.getOSMIPAddress();
       this.authJSON = new JSONObject();
       authJSON.put("username",new JSONValue(osmClient.getOSMUser()));
       authJSON.put("password",new JSONValue(osmClient.getOSMPassword()));
       authJSON.put("project_id",new JSONValue(osmClient.getOSMProject()));
       HTTPUtils.configureSecurity();
       createSessionToken();
    }

    private void createSessionToken()
    {
        HTTPResponse tokenResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+TOKEN_URL_005, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.SOL_005,false, null, authJSON);
        String tokenContent = tokenResponse.getContent();
        JSONObject tokenJSON = new JSONObject(tokenContent);
        String tokenID = tokenJSON.get("_id").getValue();
        this.credentials = tokenID;
    }
    public String getToken(){return this.credentials;}

    protected List<VirtualInfrastructureManager> parseVIMList()
    {
        List<VirtualInfrastructureManager> vims = new LinkedList<>();
        HTTPResponse vimResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+VIM_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
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
        HTTPResponse vnfdResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+ VNFD_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
        String vnfdResponseContent = vnfdResponse.getContent();
        String vnfdResponseContent_mod = vnfdResponseContent.replace(",            \"userDefinedData\": {}","");
        JSONArray vnfdsArray = new JSONArray(vnfdResponseContent_mod);
        for(JSONValue item : vnfdsArray)
        {
            JSONObject ob = item.getValue();
            VirtualNetworkFunctionDescriptor vnfd = parseVNFD(ob);
            vnfds.add(vnfd);
        }
        return vnfds;
    }

    protected List<VirtualNetworkFunction> parseVNFList()
    {
        List<VirtualNetworkFunction> vnfs = new LinkedList<>();
        HTTPResponse vnfResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+ VNF_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
        String vnfResponseContent = vnfResponse.getContent();
        String vnfResponseContent_mod = vnfResponseContent;
        System.out.println(vnfResponseContent);
        JSONArray vnfsArray = new JSONArray(vnfResponseContent_mod);
        /*for(JSONValue item : vnfsArray)
        {
            JSONObject ob = item.getValue();
            VirtualNetworkFunction vnf = parseVNF(ob);
            vnfs.add(vnf);
        }*/

        return vnfs;
    }

    protected List<NetworkServiceDescriptor> parseNSDList()
    {
        List<NetworkServiceDescriptor> nsds = new LinkedList<>();
        HTTPResponse nsdResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+ NSD_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
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
        HTTPResponse nsResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+ NS_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
        String nsResponseContent = nsResponse.getContent();
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

    protected HTTPResponse createNS(String name, String description, String nsdName, String vim, NSConfiguration... nsConfiguration)
    {
        JSONObject nsJSON = new JSONObject();
        nsJSON.put("nsDescription",new JSONValue(description));
        String vimId = osmClient.getVIM(vim).getId();
        String nsdId = osmClient.getNSD(nsdName).getId();
        nsJSON.put("vimAccountId",new JSONValue(vimId));
        nsJSON.put("nsdId", new JSONValue(nsdId));
        nsJSON.put("nsName", new JSONValue(name));

        return HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+NS_URL_005, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.SOL_005,true, credentials, nsJSON);
    }

    protected HTTPResponse deleteNS(String name)
    {
        String nsId = osmClient.getNS(name).getId();
        return HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+NS_URL_005+"/"+nsId, HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.SOL_005,true, credentials);
    }

    protected HTTPResponse createVIM(String name, String description, OSMConstants.OSMVimType osmVimType, String user, String pass, String authURL, String tenant, VIMConfiguration... VIMConfiguration)
    {
        JSONObject createVIMJSON = new JSONObject();
        createVIMJSON.put("name", new JSONValue(name));
        createVIMJSON.put("vim_type", new JSONValue(osmVimType.toString()));
        createVIMJSON.put("description", new JSONValue(description));
        createVIMJSON.put("vim_url", new JSONValue(authURL));
        createVIMJSON.put("vim_user", new JSONValue(user));
        createVIMJSON.put("vim_password", new JSONValue(pass));
        createVIMJSON.put("vim_tenant_name", new JSONValue(tenant));
        if(VIMConfiguration.length == 1)
            createVIMJSON.put("config", new JSONValue(VIMConfiguration[0].toJSON()));

        return HTTPUtils.establishHTTPConnectionWithOSM("https://" + osmIPAddress + ":9999"+VIM_URL_005, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.SOL_005, true, credentials, createVIMJSON);
    }

    protected HTTPResponse deleteVIM(String vimName)
    {
        String vimId = osmClient.getVIM(vimName).getId();
        return HTTPUtils.establishHTTPConnectionWithOSM("https://" + osmIPAddress + ":9999"+VIM_URL_005+"/"+vimId+"?FORCE=True", HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
    }

    protected HTTPResponse deleteNSD(String nsdName)
    {
        String nsdId = osmClient.getNSD(nsdName).getId();
        return HTTPUtils.establishHTTPConnectionWithOSM("https://" + osmIPAddress + ":9999"+NSD_URL_005+"/"+nsdId+"?FORCE=True", HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
    }

    protected HTTPResponse deleteVNFD(String vnfdName)
    {
        String vnfdId = osmClient.getVNFD(vnfdName).getId();
        return HTTPUtils.establishHTTPConnectionWithOSM("https://" + osmIPAddress + ":9999"+VNFD_URL_005+"/"+vnfdId+"?FORCE=True", HTTPUtils.HTTPMethod.DELETE, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
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
        String storageGb = flavorJSON.get("storage-gb").getValue();
        double vcpuCount = flavorJSON.get("vcpu-count").getValue();
        String memoryMb = flavorJSON.get("memory-mb").getValue();

        VirtualDeploymentUnit vdu = new VirtualDeploymentUnit(vduId, vduName, vduImage, Double.parseDouble(storageGb), vcpuCount, Double.parseDouble(memoryMb));

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

    public HTTPResponse uploadPackage(File file, OSMConstants.OSMPackageType type)
    {
        HTTPResponse response = null;
        switch (type)
        {
            case NSD:
                response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+UPLOAD_NSD_URL_005, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.SOL_005, true, credentials, file);
                break;
            case VNFD:
                response = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+UPLOAD_VNFD_URL_005, HTTPUtils.HTTPMethod.POST, OSMConstants.OSMClientVersion.SOL_005, true, credentials, file);
                break;
        }
        return response;
    }

}
