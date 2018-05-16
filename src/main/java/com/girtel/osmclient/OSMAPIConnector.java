package com.girtel.osmclient;


import com.girtel.osmclient.utils.HTTPResponse;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONObject;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

class OSMAPIConnector {

    final String NS_URL = "/api/running/project/{projectname}/ns-instance-config";
    final String NSD_URL = "/api/running/project/{projectname}/nsd-catalog/nsd";
    final String VNF_URL = "/v1/api/operational/project/{projectname}/vnfr-catalog/vnfr";
    final String VNFD_URL = "/api/running/project/{projectname}/vnfd-catalog/vnfd";
    final String CONFIG_AGENT_URL = "/api/config/project/{projectname}/config-agent";
    final String DATACENTER_LIST_URL = "/openmano/{tenant_id}/datacenters";
    final String DEFAULTROACCOUNT_URL = "/api/operational/project/{projectname}/ro-account";
    final String UPDATEROACCOUNT_URL = "/api/operations/update-ro-account-status";
    final String DATACENTERS_URL = "/openmano/datacenters";
    final String GET_TENANTS_URL = "/openmano/tenants/";
    final String ATTACH_DETACH_DATACENTER_URL = "/openmano/{osm_id}/datacenters/{dc_id}";
    final String UPLOAD_PACKAGE_URL = "https://{osm_ip}:8443/composer/upload?api_server=https://localhost&upload_server=https://{osm_ip}&project_name={projectname}";
    final String CREATE_NS_URL = "/api/config/project/{projectname}/ns-instance-config/nsr";
    final String NS_DELETE_URL = "/api/config/project/{projectname}/ns-instance-config/nsr/{ns_id}";
    final String NSD_DELETE_URL = "/api/running/project/{projectname}/nsd-catalog/nsd/{nsd_id}";
    final String VNFD_DELETE_URL = "/api/running/project/{projectname}/vnfd-catalog/vnfd/{vnfd_id}";
    final String SCALE_URL = "/v1/api/config/project/{projectname}/ns-instance-config/nsr/{ns_id}/scaling-group/{group}/instance";
    final String NS_OPDATA_URL = "/api/operational/project/{projectname}/ns-instance-opdata/nsr/{ns_id}?deep";

    private String credentials;
    private String osmIPAddress;
    private String project;

    protected OSMAPIConnector(OSMClient osmClient)
    {
        this.osmIPAddress = osmClient.getOSMIPAddress();
        this.credentials = osmClient.getEncodedCredentials();
        this.project = osmClient.getProject();
        configureSecurity();
    }


    /**
     * HTTP methods
     */
    private enum HTTPMethod
    {
        GET("GET"), POST("POST"), DELETE("DELETE");

        private String method;

        HTTPMethod(String method)
        {
            this.method = method;
        }

        @Override
        public String toString()
        {
            return method;
        }
    }

    private void configureSecurity()
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        return;
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        return;
                    }
                }
        };


        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session)
            {
                boolean verify = false;
                if (urlHostName.equalsIgnoreCase(session.getPeerHost()))
                {
                    verify = true;
                }
                return verify;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

    }
    
    private void configureConnection(HttpURLConnection conn, MultipartEntity... optionalMultiPartEntity)
    {
        if(optionalMultiPartEntity.length == 0)
        {
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Accept","application/vnd.yand.data+json");
            conn.setRequestProperty("Authorization",credentials);
        }
        else if (optionalMultiPartEntity.length == 1)
        {
            MultipartEntity entity = optionalMultiPartEntity[0];
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setRequestProperty("Accept","*application/json*");
            conn.setRequestProperty("Content-Type",entity.getContentType().getValue());
            conn.setRequestProperty("Content-length", String.valueOf(entity.getContentLength()));
            conn.setRequestProperty("Authorization",credentials);
        }
        else
            throw new RuntimeException("No more than one multipartEntity is allowed");


    }

    private HTTPResponse HTTPCommunication(String url, HTTPMethod method, Object... optionalObjectToSend)
    {
        URL url_;
        HttpURLConnection conn = null;
        HTTPResponse response = null;
        try {
            url_ = new URL(url);

            conn = (HttpURLConnection)url_.openConnection();
            conn.setRequestMethod(method.toString());
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            if(optionalObjectToSend.length == 0)
            {
                configureConnection(conn);
                conn.connect();
                response = HTTPResponse.getResponseFromHTTPConnection(conn);
            }
            else if(optionalObjectToSend.length == 1)
            {
                Object obj = optionalObjectToSend[0];
                if(obj instanceof File)
                {
                    File file = (File)obj;
                    FileBody fileBody = new FileBody(file);
                    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);
                    entity.addPart("package",fileBody);

                    configureConnection(conn, entity);
                    conn.connect();

                    OutputStream out = null;
                    try {
                        out = conn.getOutputStream();
                        entity.writeTo(out);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    response = HTTPResponse.getResponseFromHTTPConnection(conn);

                }
                else if(obj instanceof JSONObject)
                {
                    configureConnection(conn);
                    conn.connect();

                    JSONObject json = (JSONObject)obj;
                    DataOutputStream out = null;
                    try {
                        out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(JSON.write(json));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    response = HTTPResponse.getResponseFromHTTPConnection(conn);
                }
                else{
                    throw new RuntimeException("Unsupported file type -> "+obj.getClass());
                }
            }
            else{
                throw new RuntimeException("More than one object is not allowed");
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public HTTPResponse establishConnectionToReceiveVNFDList()
    {
        String url = "https://"+osmIPAddress+":8008"+ VNFD_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToReceiveVNFList()
    {
        String url = "https://"+osmIPAddress+":8008"+ VNF_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToReceiveNSDList()
    {
        String url = "https://"+osmIPAddress+":8008"+ NSD_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToReceiveNSList()
    {
        String url = "https://"+osmIPAddress+":8008"+ NS_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }


    public HTTPResponse establishConnectionToReceiveDatacenterList(String tenantId)
    {
        String url = "http://"+osmIPAddress+":9090"+ DATACENTER_LIST_URL.replace("{tenant_id}",tenantId);
        return HTTPCommunication(url, HTTPMethod.GET);
    }


    public HTTPResponse establishConnectionToReceiveConfigAgentList()
    {
        String url = "https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToCreateDatacenter(JSONObject dataCenterJSON)
    {
        String url = "http://"+osmIPAddress+":9090"+ DATACENTERS_URL;
        return HTTPCommunication(url, HTTPMethod.POST, dataCenterJSON);
    }

    public HTTPResponse establishConnectionToAttachDatacenterToOSM(String tenantId, String datacenterId, JSONObject dataCenterJSON)
    {
        String url = "http://"+osmIPAddress+":9090"+ ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterId);
        return HTTPCommunication(url, HTTPMethod.POST, dataCenterJSON);
    }

    public HTTPResponse establishConnectionToReceiveOSMTenant()
    {
        String url = "http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm";
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToReceiveDefaultROAccount()
    {
        String url = "https://"+osmIPAddress+":8008"+ DEFAULTROACCOUNT_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

    public HTTPResponse establishConnectionToUpdateROAccount(JSONObject updateJSON)
    {
        String url = "https://"+osmIPAddress+":8008"+ UPDATEROACCOUNT_URL;
        return HTTPCommunication(url, HTTPMethod.POST, updateJSON);
    }

    public HTTPResponse establishConnectionToUploadPackageToOSM(File packageToUpload)
    {
        String url = UPLOAD_PACKAGE_URL.replace("{osm_ip}",osmIPAddress).replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.POST, packageToUpload);
    }

    public HTTPResponse establishConnectionToCreateNS(JSONObject nsJSON)
    {
        String url = "https://"+osmIPAddress+":8008"+ CREATE_NS_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.POST, nsJSON);
    }

    public HTTPResponse establishConnectionToAddConfigAgent(JSONObject configAgentJSON)
    {
        String url = "https://"+osmIPAddress+":8008" + CONFIG_AGENT_URL.replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.POST, configAgentJSON);
    }

    public HTTPResponse establishConnectionToDetachDatacenter(String tenantId, String datacenterName)
    {
        String url = "http://"+osmIPAddress+":9090" + ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterName);
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToDeleteDatacenter(String datacenterName)
    {
        String url = "http://"+osmIPAddress+":9090"+ DATACENTERS_URL+"/"+datacenterName;
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToDeleteNS(String id)
    {
        String url = "https://"+osmIPAddress+":8008" + NS_DELETE_URL.replace("{ns_id}",id).replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToDeleteNSD(String id)
    {
        String url = "https://"+osmIPAddress+":8008" + NSD_DELETE_URL.replace("{nsd_id}",id).replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToDeleteVNFD(String id)
    {
        String url = "https://"+osmIPAddress+":8008" + VNFD_DELETE_URL.replace("{vnfd_id}",id).replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToDeleteConfigAgent(String name)
    {
        String url = "https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL.replace("{projectname}",project)+"/account/"+name;
        return HTTPCommunication(url, HTTPMethod.DELETE);
    }

    public HTTPResponse establishConnectionToScaleNS(String nsId, String group, JSONObject scaleJSON)
    {
        String url = "https://"+osmIPAddress+":8008" +  SCALE_URL.replace("{ns_id}",nsId).replace("{group",group);
        return HTTPCommunication(url, HTTPMethod.POST, scaleJSON);
    }

    public HTTPResponse establishConnectionToReceiveNSOperationalData(String id)
    {
        String url = "https://"+osmIPAddress+":8008" + NS_OPDATA_URL.replace("{ns_id}",id).replace("{projectname}",project);
        return HTTPCommunication(url, HTTPMethod.GET);
    }

}
