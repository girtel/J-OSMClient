package com.girtel.osmclient;

import com.girtel.osmclient.internal.OSMException;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.internal.JSONObject;
import com.girtel.osmclient.utils.JSONUtils;
import com.girtel.osmclient.internal.JSONValue;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class OSMAPIConnector005
{
    private static String TOKEN_URL_005 = "/osm/admin/v1/tokens";
    private static String VIM_URL_005 = "/osm/admin/v1/vim_accounts";
    private static String VNFD_URL_005 = "/osm/vnfpkgm/v1/vnf_packages";
    private static String VNF_URL_005 = "/osm/nslcm/v1/vnfrs";
    private static String NSD_URL_005 = "/osm/nsd/v1/ns_descriptors";
    private static String NS_URL_005 = "/osm/nslcm/v1/ns_instances_content";


    private OSMClient005 osmClient005;
    private String osmIPAddress;
    private String project;
    private JSONObject authJSON;

    public OSMAPIConnector005(OSMClient005 osmClient005)
    {
        this.osmClient005 = osmClient005;
        this.osmIPAddress = osmClient005.getOSMIPAddress();
        this.project = osmClient005.getProject();
        this.authJSON = new JSONObject();
        authJSON.put("username",new JSONValue(osmClient005.getOSMUser()));
        authJSON.put("password",new JSONValue(osmClient005.getOSMPassword()));
        authJSON.put("project_id",new JSONValue(osmClient005.getProject()));
        configureSecurity();
    }

    /**
     * HTTP methods
     */
    private enum HTTPMethod
    {
        GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");

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

    private void configureConnection(HttpURLConnection conn, boolean includeAuth, MultipartEntity... optionalMultiPartEntity)
    {
        if(optionalMultiPartEntity.length == 0)
        {
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Accept","application/json");
            if(includeAuth)
            {
                conn.setRequestProperty("Authorization","Bearer "+osmClient005.getSessionToken());
            }

        }
        else if (optionalMultiPartEntity.length == 1)
        {
            MultipartEntity entity = optionalMultiPartEntity[0];
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setRequestProperty("Accept","*application/json*");
            conn.setRequestProperty("Content-Type",entity.getContentType().getValue());
            conn.setRequestProperty("Content-length", String.valueOf(entity.getContentLength()));
            if(includeAuth)
            {
                conn.setRequestProperty("Authorization","Bearer "+osmClient005.getSessionToken());
            }

        }
        else
            throw new RuntimeException("No more than one multipartEntity is allowed");


    }

    private HTTPResponse establishHTTPConnection(String url, OSMAPIConnector005.HTTPMethod method, boolean includeAuth, Object... optionalObjectToSend)
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
                configureConnection(conn, includeAuth);
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

                    configureConnection(conn, includeAuth, entity);
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
                    configureConnection(conn, includeAuth);
                    conn.connect();

                    JSONObject json = (JSONObject)obj;
                    DataOutputStream out = null;
                    try {
                        out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(JSONUtils.write(json));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    response = HTTPResponse.getResponseFromHTTPConnection(conn);
                }
                else{
                    throw new OSMException("Unsupported file type in OSM -> "+obj.getClass());
                }
            }
            else{
                throw new OSMException("Sending to OSM more than one object is not allowed");
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

    public HTTPResponse establishConnectionToCreateSessionToken()
    {
        String url = "https://"+osmIPAddress+":9999"+TOKEN_URL_005.replace("{project}",project);
        HTTPResponse response =  establishHTTPConnection(url, HTTPMethod.POST, false, authJSON);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveVIMList()
    {
        String url = "https://"+osmIPAddress+":9999"+VIM_URL_005.replace("{project}",project);
        HTTPResponse response =  establishHTTPConnection(url, HTTPMethod.GET, true);
        return response;
    }

    public HTTPResponse establishConnectionToReceiveVNFDList()
    {
        String url = "https://"+osmIPAddress+":9999"+ VNFD_URL_005.replace("{project}",project);
        return establishHTTPConnection(url, HTTPMethod.GET, true);
    }

    public HTTPResponse establishConnectionToReceiveVNFList()
    {
        String url = "https://"+osmIPAddress+":9999"+ VNF_URL_005.replace("{project}",project);
        return establishHTTPConnection(url, HTTPMethod.GET, true);
    }

    public HTTPResponse establishConnectionToReceiveNSDList()
    {
        String url = "https://"+osmIPAddress+":9999"+ NSD_URL_005.replace("{project}",project);
        return establishHTTPConnection(url, HTTPMethod.GET, true);
    }

    public HTTPResponse establishConnectionToReceiveNSList()
    {
        String url = "https://"+osmIPAddress+":9999"+ NS_URL_005.replace("{project}",project);
        return establishHTTPConnection(url, HTTPMethod.GET, true);
    }

    public HTTPResponse establishConnectionToCreateNS(JSONObject nsJSON)
    {
        String url = "https://"+osmIPAddress+":9999"+NS_URL_005.replace("{project}",project);
        HTTPResponse response =  establishHTTPConnection(url, HTTPMethod.POST, true, nsJSON);
        return response;
    }

    public HTTPResponse establishConnectionToDeleteNS(String nsId)
    {
        String url = "https://"+osmIPAddress+":9999"+NS_URL_005+"/"+nsId;
        HTTPResponse response =  establishHTTPConnection(url, HTTPMethod.DELETE, true);
        return response;
    }


}
