package com.cfsnm.utils;

/**
 * This class represents a HTTP Response (code, message) ,e.c. (200,OK)
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class HTTPResponse extends javafx.util.Pair<Integer, String>
{

    private Integer code;
    private String message;
    /**
     * Constructor
     *
     * @param code HTTP code
     * @param message HTTP message
     */
    public HTTPResponse(Integer code, String message)
    {
        super(code, message);
        this.code = code;
        this.message = message;
    }

    public Integer getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return "(" +code+", "+message+")";
    }
}
