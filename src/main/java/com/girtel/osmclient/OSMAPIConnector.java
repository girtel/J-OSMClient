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

    private void configureConnection(HttpURLConnection conn)
    {
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("Accept","application/vnd.yand.data+json");
        conn.setRequestProperty("Authorization",credentials);
    }

    private void configureConnectionToUploadPackage(HttpURLConnection conn, File file, MultipartEntity entity)
    {
        conn.setRequestProperty("Content-Encoding", "gzip");
        conn.setRequestProperty("Accept","*application/json*");
        conn.setRequestProperty("Content-Type",entity.getContentType().getValue());
        conn.setRequestProperty("Content-length", String.valueOf(file.length()));
        conn.setRequestProperty("Authorization",credentials);
    }

    private HttpURLConnection establishConnection(String url, HTTPMethod method)
    {

        URL url_;
        HttpURLConnection conn = null;
        try {
            url_ = new URL(url);

            conn = (HttpURLConnection)url_.openConnection();
            conn.setRequestMethod(method.toString());
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            configureConnection(conn);
            conn.connect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return conn;
    }

    private void sendJSON(HttpURLConnection conn, JSONObject json)
    {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(JSON.write(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(HttpURLConnection conn, File fileToUpload)
    {
        FileBody fileBody = new FileBody(fileToUpload);
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);
        entity.addPart("package",fileBody);
        configureConnectionToUploadPackage(conn, fileToUpload, entity);

        OutputStream out = null;
        try {
            out = conn.getOutputStream();
            entity.writeTo(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private HTTPResponse processResponse(HttpURLConnection conn)
    {
        BufferedReader in = null;
        int code = 0;
        String message = "";
        String response = "";

        try {
            code = conn.getResponseCode();
            message = conn.getResponseMessage();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while((line = in.readLine()) != null)
            {
                response += line;
            }

            in.close();
            conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(code, message, response);
    }


    public HTTPResponse establishConnectionToReceiveVNFDList()
    {

        String url = "https://"+osmIPAddress+":8008"+ VNFD_URL.replace("{projectname}",project);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveVNFList()
    {
        String url = "https://"+osmIPAddress+":8008"+ VNF_URL.replace("{projectname}",project);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveNSDList()
    {
        String url = "https://"+osmIPAddress+":8008"+ NSD_URL.replace("{projectname}",project);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveNSList()
    {
        String url = "https://"+osmIPAddress+":8008"+ NS_URL.replace("{projectname}",project);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }


    public HTTPResponse establishConnectionToReceiveDatacenterList(String tenantId)
    {
        String url = "http://"+osmIPAddress+":9090"+ DATACENTER_LIST_URL.replace("{tenant_id}",tenantId);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }


    public HTTPResponse establishConnectionToReceiveConfigAgentList()
    {
        String url = "https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL.replace("{projectname}",project);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToCreateDatacenter(JSONObject dataCenterJSON)
    {
        String url = "http://"+osmIPAddress+":9090"+ DATACENTERS_URL);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.POST);
        sendJSON(conn, dataCenterJSON);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToAttachDatacenterToOSM(String tenantId, String datacenterId, JSONObject dataCenterJSON)
    {
        String url = "http://"+osmIPAddress+":9090"+ ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterId);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.POST);
        sendJSON(conn, dataCenterJSON);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveOSMTenant()
    {
        String url = "http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm";
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveDefaultROAccount()
    {
        String url = "https://"+osmIPAddress+":8008"+ DEFAULTROACCOUNT_URL;
        HttpURLConnection conn = establishConnection(url, HTTPMethod.GET);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToUpdateROAccount(JSONObject updateJSON)
    {
        String url = "https://"+osmIPAddress+":8008"+ UPDATEROACCOUNT_URL;
        HttpURLConnection conn = establishConnection(url, HTTPMethod.POST);
        sendJSON(conn, updateJSON);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToUploadPackageToOSM(File packageToUpload)
    {
        String url = UPLOAD_PACKAGE_URL.replace("{osm_ip}",osmIPAddress);
        HttpURLConnection conn = establishConnection(url, HTTPMethod.POST);
        sendFile(conn, packageToUpload);
        HTTPResponse response = processResponse(conn);
        return response;
    }

    public HTTPResponse establishConnectionToCreateNS(JSONObject ob)
    {
        URL url = null;
        int responseCode = 0;
        String responseMessage = "";
        String receivedJSON = "";

        try {

            url = new URL("https://"+osmIPAddress+":8008"+ CREATE_NS_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            out.writeBytes(JSON.write(ob));

            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();

            out.close();
            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(responseCode, responseMessage);
    }

    public HTTPResponse establishConnectionToAddConfigAgent(JSONObject finalJSON)
    {
        URL url = null;
        int responseCode = 0;
        String responseMessage = "";
        String receivedJSON = "";

        try {

            url = new URL("https://"+osmIPAddress+":8008" + CONFIG_AGENT_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            out.writeBytes(JSON.write(finalJSON));

            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();

            out.close();
            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(responseCode, responseMessage);
    }

    public String establishConnectionToDetachDatacenter(String tenantId, String datacenterName)
    {
        URL url = null;
        String receivedJSON = "";

        try {

            String attachDetachDatacenterURL = ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterName);
            url = new URL("http://"+osmIPAddress+":9090"+ attachDetachDatacenterURL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            while((line = in.readLine()) != null)
            {
                receivedJSON = receivedJSON + line;
            }

            in.close();


            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedJSON;
    }

    public HTTPResponse establishConnectionToDeleteDatacenter(String datacenterName)
    {
        URL url = null;
        String responseMessage = "";
        int responseCode = 0;

        try {

            url = new URL("http://"+osmIPAddress+":9090"+ DATACENTERS_URL+"/"+datacenterName);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();


            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();


            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(responseCode, responseMessage);
    }

    public String establishConnectionToDeleteNS(String id)
    {

        URL url = null;
        String response = "";

        try {

            String deleteURL = NS_DELETE_URL.replace("{ns_id}",id);
            url = new URL("https://"+osmIPAddress+":8008"+ deleteURL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            while((line = in.readLine()) != null)
            {
                response = response + line;
            }

            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public String establishConnectionToDeleteNSD(String id)
    {

        URL url = null;
        String response = "";

        try {

            String deleteURL = NSD_DELETE_URL.replace("{nsd_id}",id);
            url = new URL("https://"+osmIPAddress+":8008"+ deleteURL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            while((line = in.readLine()) != null)
            {
                response = response + line;
            }

            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public String establishConnectionToDeleteVNFD(String id)
    {

        URL url = null;
        String response = "";

        try {

            String deleteURL = VNFD_DELETE_URL.replace("{vnfd_id}",id);
            url = new URL("https://"+osmIPAddress+":8008"+ deleteURL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            while((line = in.readLine()) != null)
            {
                response = response + line;
            }

            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public HTTPResponse establishConnectionToDeleteConfigAgent(String name)
    {
        URL url = null;
        int responseCode = 0;
        String responseMessage = "";
        String receivedJSON = "";

        try {

            url = new URL("https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL+"/account/"+name);
            System.out.println(url);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();


            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();

            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(responseCode, responseMessage);
    }

    public HTTPResponse establishConnectionToScaleNS(String nsId, String group, JSONObject scaleJSON)
    {
        URL url = null;
        int responseCode = 0;
        String responseMessage = "";
        String receivedJSON = "";

        try {

            String scaleURL = SCALE_URL.replace("{ns_id}",nsId).replace("{group",group);

            url = new URL("https://"+osmIPAddress+":8008"+ scaleURL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            System.out.println("SCALE NS -> "+scaleJSON.toString());

            out.writeBytes(scaleJSON.toString());

            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();

            out.close();
            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(responseCode, responseMessage);
    }

    public String establishConnectionToReceiveNSOperationalData(String id)
    {
        URL url = null;
        String receivedJSON = "";
        try {

            String opDataUrl = NS_OPDATA_URL.replace("{ns_id}",id);

            url = new URL("https://"+osmIPAddress+":8008"+ opDataUrl);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            while((line = in.readLine()) != null)
            {
                receivedJSON = receivedJSON + line;
            }

            System.out.println("OP DATA -> "+receivedJSON);

            in.close();
            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedJSON;
    }

}
