package com.girtel.osmclient;

import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.utils.HTTPResponse;
import com.girtel.osmclient.utils.OSMConstants;
import com.girtel.osmclient.utils.OSMException;
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

public class HTTPUtils
{
    /**
     * HTTP methods
     */
    public enum HTTPMethod
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

    protected static void configureSecurity()
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

    protected static void configureHeadersToConnectWithOSMReleaseThree(HttpURLConnection conn, String credentials, MultipartEntity... optionalMultiPartEntity)
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

    private static void configureHeadersToConnectWithOSMsol005(HttpURLConnection conn, boolean includeAuth, String credentials, MultipartEntity... optionalMultiPartEntity)
    {
        if(optionalMultiPartEntity.length == 0)
        {
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Accept","application/json");
            if(includeAuth)
            {
                conn.setRequestProperty("Authorization","Bearer "+credentials);
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
                conn.setRequestProperty("Authorization","Bearer "+credentials);
            }

        }
        else
            throw new RuntimeException("No more than one multipartEntity is allowed");


    }

    protected static HTTPResponse establishHTTPConnectionWithOSM(String url, HTTPMethod method, OSMConstants.OSMClientVersion version, boolean includeAuth, String credentials, Object... optionalObjectToSend)
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
                switch(version)
                {
                    case RELEASE_THREE:
                        configureHeadersToConnectWithOSMReleaseThree(conn, credentials);
                        break;

                    case SOL_005:
                        configureHeadersToConnectWithOSMsol005(conn, includeAuth, credentials);
                        break;
                }
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

                    switch(version)
                    {
                        case RELEASE_THREE:
                            configureHeadersToConnectWithOSMReleaseThree(conn, credentials, entity);
                            break;

                        case SOL_005:
                            configureHeadersToConnectWithOSMsol005(conn, includeAuth, credentials, entity);
                            break;
                    }
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
                    switch(version)
                    {
                        case RELEASE_THREE:
                            configureHeadersToConnectWithOSMReleaseThree(conn, credentials);
                            break;

                        case SOL_005:
                            configureHeadersToConnectWithOSMsol005(conn, includeAuth, credentials);
                            break;
                    }
                    conn.connect();

                    JSONObject json = (JSONObject)obj;
                    DataOutputStream out = null;
                    try {
                        out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(json.toString());
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
}
