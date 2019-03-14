package com.girtel.osmclient.utils;

import com.girtel.osmclient.json.JSONObject;
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

/**
 * This class represents a HTTP Response
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class HTTPResponse
{
    private Integer code;
    private String message, content;
    /**
     * Constructor
     *
     * @param code HTTP code
     * @param message HTTP message
     */
    private HTTPResponse(Integer code, String message, String content)
    {
        this.code = code;
        this.message = message;
        this.content = content;
    }

    /**
     * Obtains HTTP code, e.c. 200
     * @return HTTP code
     */
    public Integer getCode()
    {
        return code;
    }

    /**
     * Obtains HTTP message, e.c. OK
     * @return HTTP message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Obtains HTTP response content
     * @return HTTP response content
     */
    public String getContent()
    {
         return content;
    }

    @Override
    public String toString()
    {
        return "(" +code+", "+message+")\r\n" +
                "Content -> "+content+"\r\n";
    }

    /**
     * Obtains a HTTP Response from a HTTP Connection
     * @param conn HTTP Connection to process
     * @return HTTP Response
     */
    public static HTTPResponse getResponseFromHTTPConnection(HttpURLConnection conn)
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

}
