package com.girtel.osmclient.utils;

/**
 * Provides different sets of constants to work with OSM
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class Constants {


    private Constants(){}


    /**
     * VIM Types
     */
    public enum OSMVimType
    {
        OPENSTACK("openstack"), OPENVIM("openvim"), VMWARE("vmware"), AWS("aws");

        private String type;

        /**
         * Constructor
         * @param type vim type
         */
        OSMVimType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return type;
        }
    };

    /**
     * Configuration Agent Types
     */
    public enum OSMConfigAgentType
    {
        JUJU("juju");

        private String type;

        /**
         * Constructor
         * @param type configuration agent type
         */
        OSMConfigAgentType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return type;
        }
    };

    /**
     * HTTP methods
     */
    public enum HTTPMethod
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

}

