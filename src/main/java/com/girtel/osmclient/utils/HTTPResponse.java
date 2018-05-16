package com.girtel.osmclient.utils;

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
    public HTTPResponse(Integer code, String message, String content)
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
     * Returns a HTTPResponse which represents an error
     * @param content error content
     * @return Error HTTPResponse
     */
    public static HTTPResponse errorResponse(String content)
    {
        return new HTTPResponse(0,"ERROR",content);
    }

    public static HTTPResponse EMPTY_RESPONSE = new HTTPResponse(0,"EMPTY","EMPTY");
}
