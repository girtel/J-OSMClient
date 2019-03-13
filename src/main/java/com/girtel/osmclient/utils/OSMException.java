package com.girtel.osmclient.utils;

/**
 * A exception that is thrown when a connection with OSM is not successful
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class OSMException extends RuntimeException
{
    private String message;

    /**
     * Constructor
     * @param message Message to be shown when the exception is thrown
     */
    public OSMException(String message)
    {
        super(message);
        this.message = message;
    }

    /**
     * Obtains the exception message
     * @return Exception message
     */
    public String getMessage()
    {
        return message;
    }
}
