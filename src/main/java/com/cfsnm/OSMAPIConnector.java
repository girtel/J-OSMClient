package com.cfsnm;


import com.cfsnm.utils.HTTPResponse;
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

    final String NS_URL = "/api/running/project/default/ns-instance-config";
    final String NSD_URL = "/api/running/project/default/nsd-catalog/nsd";
    final String VNF_URL = "/v1/api/operational/project/default/vnfr-catalog/vnfr";
    final String VNFD_URL = "/api/running/project/default/vnfd-catalog/vnfd";
    final String CONFIG_AGENT_URL = "/api/config/project/default/config-agent";
    final String DATACENTER_LIST_URL = "/openmano/{tenant_id}/datacenters";
    final String DEFAULTROACCOUNT_URL = "/api/operational/project/default/ro-account";
    final String UPDATEROACCOUNT_URL = "/api/operations/update-ro-account-status";
    final String DATACENTERS_URL = "/openmano/datacenters";
    final String GET_TENANTS_URL = "/openmano/tenants/";
    final String ATTACH_DETACH_DATACENTER_URL = "/openmano/{osm_id}/datacenters/{dc_id}";
    final String UPLOAD_PACKAGE_URL = "https://{osm_ip}:8443/composer/upload?api_server=https://localhost&upload_server=https://{osm_ip}&project_name=default";
    final String CREATE_NS_URL = "/api/config/project/default/ns-instance-config/nsr";
    final String NS_DELETE_URL = "/api/config/project/default/ns-instance-config/nsr/{ns_id}";
    final String NSD_DELETE_URL = "/api/running/project/default/nsd-catalog/nsd/{nsd_id}";
    final String VNFD_DELETE_URL = "/api/running/project/default/vnfd-catalog/vnfd/{vnfd_id}";
    final String SCALE_URL = "/v1/api/config/project/default/ns-instance-config/nsr/{ns_id}/scaling-group/{group}/instance";
    final String NS_OPDATA_URL = "/api/operational/project/default/ns-instance-opdata/nsr/{ns_id}?deep";

    private String credentials;

    private String osmIPAddress;

    protected OSMAPIConnector(String osmIPAddress, String credentials)
    {
        this.osmIPAddress = osmIPAddress;
        this.credentials = credentials;
        configureSecurity();
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
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

    }

    private void configureConnection(HttpURLConnection conn)
    {
        configureSecurity();
        String userCred = "admin:admin";
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("Accept","application/vnd.yand.data+json");
        conn.setRequestProperty("Authorization",credentials);
    }

    private void configureConnectionToUploadPackage(HttpURLConnection conn, File file, MultipartEntity entity)
    {
        configureSecurity();
        String userCred = "admin:admin";
        conn.setRequestProperty("Content-Encoding", "gzip");
        conn.setRequestProperty("Accept","*application/json*");
        conn.setRequestProperty("Content-Type",entity.getContentType().getValue());
        conn.setRequestProperty("Content-length", String.valueOf(file.length()));
        conn.setRequestProperty("Authorization",credentials);
    }

    public String establishConnectionToReceiveVNFDList()
    {
        URL url;
        String receivedJSON = "";
        try {
            url = new URL("https://"+osmIPAddress+":8008"+ VNFD_URL);


            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            receivedJSON = in.readLine();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedJSON;
    }

    public String establishConnectionToReceiveVNFList()
    {
        URL url = null;
        String receivedJSON = "";
        try {
            url = new URL("https://"+osmIPAddress+":8008"+ VNF_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            receivedJSON = in.readLine();

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

    public String establishConnectionToReceiveNSDList()
    {
        URL url = null;
        String receivedJSON = "";
        try {

            url = new URL("https://"+osmIPAddress+":8008"+ NSD_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            receivedJSON = in.readLine();

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

    public String establishConnectionToReceiveNSList()
    {
        URL url = null;
        String receivedJSON = "";
        try {

            url = new URL("https://"+osmIPAddress+":8008"+ NS_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            receivedJSON = in.readLine();

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


        public String establishConnectionToReceiveDatacenterList(String tenantId)
    {
        URL url = null;
        String receivedJSON = "";
        try {

            String datacenterListURL = DATACENTER_LIST_URL.replace("{tenant_id}",tenantId);
            url = new URL("http://"+osmIPAddress+":9090"+ datacenterListURL);

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


    public String establishConnectionToReceiveConfigAgentList()
    {
        URL url;
        String receivedJSON = "";
        try {
            url = new URL("https://"+osmIPAddress+":8008"+ CONFIG_AGENT_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            receivedJSON = in.readLine();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedJSON;

    }

    public String establishConnectionToCreateDatacenter(JSONObject dataCenterJSON)
    {
        URL url = null;
        String receivedJSON = "";

        try {

            url = new URL("http://"+osmIPAddress+":9090"+ DATACENTERS_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.connect();

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            out.writeBytes(JSON.write(dataCenterJSON));


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            while((line = in.readLine()) != null)
            {
                receivedJSON = receivedJSON + line;
            }

            in.close();
            out.close();
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

    public HTTPResponse establishConnectionToAttachDatacenterToOSM(String tenantId, String datacenterId, JSONObject datacenterJSON)
    {
        URL url = null;
        String receivedMessage = "";
        int receivedCode = 0;

        try {

        String attachDatacenterURL = ATTACH_DETACH_DATACENTER_URL.replace("{osm_id}",tenantId).replace("{dc_id}",datacenterId);
        url = new URL("http://"+osmIPAddress+":9090"+ attachDatacenterURL);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        configureConnection(conn);

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.connect();

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        out.writeBytes(JSON.write(datacenterJSON));


        receivedCode = conn.getResponseCode();
        receivedMessage = conn.getResponseMessage();


        conn.disconnect();
    } catch (MalformedURLException e3) {
        e3.printStackTrace();
    } catch (ProtocolException e2) {
        e2.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

        return new HTTPResponse(receivedCode, receivedMessage);
}

    public String establishConnectionToReceiveOSMTenant()
    {
        URL url = null;
        String receivedJSON = "";
        try {

            url = new URL("http://"+osmIPAddress+":9090"+ GET_TENANTS_URL+"osm");

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

    public String establishConnectionToReceiveDefaultROAccount()
    {
        URL url = null;
        String receivedJSON = "";
        try {

            url = new URL("https://"+osmIPAddress+":8008"+ DEFAULTROACCOUNT_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            configureConnection(conn);
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

    public HTTPResponse establishConnectionToUpdateROAccount(JSONObject updateJSON)
    {
        URL url = null;
        String receivedMessage = "";
        int receivedCode = 0;

        try {

            url = new URL("https://"+osmIPAddress+":8008"+ UPDATEROACCOUNT_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            configureConnection(conn);

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);


            conn.connect();

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());

            out.writeBytes(JSON.write(updateJSON));

            receivedCode = conn.getResponseCode();
            receivedMessage = conn.getResponseMessage();


            conn.disconnect();
        } catch (MalformedURLException e3) {
            e3.printStackTrace();
        } catch (ProtocolException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HTTPResponse(receivedCode, receivedMessage);
    }

    public HTTPResponse establishConnectionToUploadPackageToOSM(File packageToUpload)
    {

        String uploadURL = UPLOAD_PACKAGE_URL.replace("{osm_ip}",osmIPAddress);
        URL url = null;
        String receivedMessage = "";
        int receivedCode = 0;

        try
        {
            url = new URL(uploadURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            FileBody fileBody = new FileBody(packageToUpload);
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);

            entity.addPart("package",fileBody);

            configureConnectionToUploadPackage(conn, packageToUpload, entity);

            conn.connect();

            OutputStream out = conn.getOutputStream();

            entity.writeTo(out);

            out.close();

            receivedCode = conn.getResponseCode();
            receivedMessage = conn.getResponseMessage();



        } catch (Exception e) {
            e.printStackTrace();
        }


        return new HTTPResponse(receivedCode,receivedMessage);
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
