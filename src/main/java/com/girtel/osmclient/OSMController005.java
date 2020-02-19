package com.girtel.osmclient;

import com.girtel.osmclient.json.JSONArray;
import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.json.JSONValue;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.NSConfiguration;
import com.girtel.osmclient.utils.OSMConstants;
import com.girtel.osmclient.utils.VIMConfiguration;
import javafx.util.Pair;

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
        org.json.JSONArray vnfdsArray = new org.json.JSONArray(vnfdResponseContent_mod);
        for(Object item : vnfdsArray)
        {
            org.json.JSONObject ob = new org.json.JSONObject(item.toString());
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
        JSONArray vnfsArray = new JSONArray(vnfResponseContent);
        for(JSONValue item : vnfsArray)
        {
            JSONObject ob = item.getValue();
            VirtualNetworkFunction vnf = parseVNF(ob);
            vnfs.add(vnf);
        }

        return vnfs;
    }

    protected List<NetworkServiceDescriptor> parseNSDList()
    {
        List<NetworkServiceDescriptor> nsds = new LinkedList<>();
        HTTPResponse nsdResponse = HTTPUtils.establishHTTPConnectionWithOSM("https://"+osmIPAddress+":9999"+ NSD_URL_005, HTTPUtils.HTTPMethod.GET, OSMConstants.OSMClientVersion.SOL_005, true, credentials);
        String nsdResponseContent = nsdResponse.getContent();
        String nsdResponseContent_mod = nsdResponseContent.replace(",            \"userDefinedData\": {}","");

        //System.out.println(nsdResponseContent_mod);

        org.json.JSONArray nsdsArray = new org.json.JSONArray(nsdResponseContent_mod);
        //System.out.println(nsdsArray);
        for(Object item : nsdsArray)
        {
            //System.out.println(new org.json.JSONObject(item.toString()).toString());
            org.json.JSONObject ob = new org.json.JSONObject(item.toString());

           // System.out.println(ob);
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

        if(nsConfiguration.length == 1)
        {
            NSConfiguration thisNSConfiguration = nsConfiguration[0];
            if(!thisNSConfiguration.IsVLDConfigurationEmpty())
            {
                JSONArray vldOptions = thisNSConfiguration.getVLDOptions();
                for(JSONValue vldOption : vldOptions)
                {
                    JSONObject vldOptionJSON = vldOption.getValue();
                }
            }

            if(!thisNSConfiguration.isVNFConfigurationEmpty())
            {
                JSONArray vnfOptions = thisNSConfiguration.getVNFOptions();
                for(JSONValue vnfOption : vnfOptions)
                {
                    JSONObject vnfOptionJSON = vnfOption.getValue();
                    String vnfIndex = vnfOptionJSON.get("member-vnf-index").getValue();
                    String vimName = vnfOptionJSON.get("vim_account").getValue();

                    VirtualInfrastructureManager vimOb = osmClient.getVIM(vimName);

                    vnfOptionJSON.put("vimAccountId", new JSONValue(vimOb.getId()));

                    vnfOptionJSON.remove("vim_account");

                }

                if(!vnfOptions.isEmpty())
                    nsJSON.put("vnf", new JSONValue(vnfOptions));
            }
        }

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

    private VirtualDeploymentUnit parseVDU(org.json.JSONObject vduOb)
    {

        String vduId = vduOb.getString("id");
        String vduName = vduOb.getString("name");
        String vduImage = vduOb.getString("image");

        org.json.JSONObject flavorJSON = vduOb.getJSONObject("vm-flavor");
        String storageGb = flavorJSON.getString("storage-gb");
        double vcpuCount = flavorJSON.getDouble("vcpu-count");
        String memoryMb = flavorJSON.getString("memory-mb");

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

    private Pair<String, String> parseConnectionPointRef(org.json.JSONObject ob)
    {
        String vnfdConnPointName = ob.getString("vnfd-connection-point-ref");
        String vnfdId = ob.getString("vnfd-id-ref");
        Pair<String, String> cpRef = new Pair(vnfdId,vnfdConnPointName);
        return cpRef;
    }

    private VirtualNetworkFunctionDescriptor parseVNFD(org.json.JSONObject ob)
    {

        String id = ob.getString("_id");
        String name = ob.getString("name");
        String description = ob.getString("description");
        org.json.JSONArray vduJSON = ob.getJSONArray("vdu");

        List<VirtualDeploymentUnit> vduList = new ArrayList<>();

        for(Object vduItem : vduJSON)
        {
            org.json.JSONObject vduOb = new org.json.JSONObject(vduItem.toString());
            VirtualDeploymentUnit vdu = parseVDU(vduOb);
            vduList.add(vdu);
        }

        VirtualNetworkFunctionDescriptor vnfd = new VirtualNetworkFunctionDescriptor(id,name,description,vduList);
        return vnfd;

    }

    private VirtualNetworkFunction parseVNF(JSONObject ob)
    {
        String id = ob.get("id").getValue();
        String vimId = ob.get("vim-account-id").getValue();
        String vnfdId = ob.get("vnfd-id").getValue();
        String nsId = ob.get("nsr-id-ref").getValue();

        VirtualNetworkFunctionDescriptor vnfd = osmClient.getVNFDById(vnfdId);

        VirtualNetworkFunction vnf = new VirtualNetworkFunction(id, "vnf-"+id, "No description", "Unknown", nsId, vnfd, new LinkedList<>(), new LinkedList<>());
        return vnf;
    }

    private VirtualLinkDescriptor parseVLD(org.json.JSONObject ob)
    {
        //System.out.println(ob);
        String vldId = ob.getString("id");
        String vldName = ob.getString("name");
        boolean vldDescriptionValue = ob.has("description");
        String vldDescription = "";
        if(vldDescriptionValue != false)
            vldDescription = ob.getString("description");
        else
            vldDescription = "";

        boolean vldIsMgmtNetwork = (ob.has("mgmt-network") == false) ? false : ob.getBoolean("mgmt-network");
        org.json.JSONArray connPointRefJSON = ob.getJSONArray("vnfd-connection-point-ref");

        List<Pair<String, String>> connPointRefList = new ArrayList<>();

        for(Object item : connPointRefJSON)
        {
            org.json.JSONObject cpRefOb = new org.json.JSONObject(item.toString());
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

    private NetworkServiceDescriptor parseNSD( org.json.JSONObject ob)
    {
        String nsdId = ob.getString("_id");
        String nsdName = ob.getString("name");
        String description = (ob.getString("description") == null) ? "" : ob.getString("description");

        List<VirtualLinkDescriptor> vldList = new ArrayList<>();
        org.json.JSONArray vldJSON = ob.getJSONArray("vld");

        for(Object item : vldJSON)
        {
            org.json.JSONObject vldOb =  new org.json.JSONObject(item.toString());
            VirtualLinkDescriptor vld = parseVLD(vldOb);
            vldList.add(vld);
        }

        org.json.JSONArray constituentVNFDJSON = ob.getJSONArray("constituent-vnfd");
        List<VirtualNetworkFunctionDescriptor> constituentVNFDs = new ArrayList<>();

        for(Object item: constituentVNFDJSON)
        {
            org.json.JSONObject cVNFDOb = new org.json.JSONObject(item.toString());
            String cVNFDName = cVNFDOb.getString("vnfd-id-ref");
            VirtualNetworkFunctionDescriptor cVNFD = osmClient.getVNFD(cVNFDName);
            constituentVNFDs.add(cVNFD);
        }

        NetworkServiceDescriptor nsd = new NetworkServiceDescriptor(nsdId,nsdName,description,constituentVNFDs,vldList);
        return nsd;

    }

    private NetworkService parseNS(JSONObject ob)
    {
        String nsId = ob.get("id").getValue();
        String nsName = ob.get("name").getValue();
        String nsDatacenter = ob.get("datacenter").getValue();
        String nsDescription = (ob.get("description") == null) ? "" : ob.get("description").getValue();
        String nsStatus = ob.get("admin-status").getValue();
        String nsdName = ob.get("nsd-ref").getValue();

        VirtualInfrastructureManager vim = osmClient.getVIMById(nsDatacenter);
        NetworkServiceDescriptor nsd = osmClient.getNSD(nsdName);

        JSONArray vnfArrays = ob.get("constituent-vnfr-ref").getValue();
        List<VirtualNetworkFunction> vnfs = new LinkedList<>();
        for(JSONValue it : vnfArrays)
        {
            String vnfId = it.getValue();
            VirtualNetworkFunction vnf = osmClient.getVNFById(vnfId);
            vnfs.add(vnf);
        }

        NetworkService ns = new NetworkService(nsId,nsName,nsDescription,nsStatus,vim,nsd,vnfs);

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
