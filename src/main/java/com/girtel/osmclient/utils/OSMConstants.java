package com.girtel.osmclient.utils;

/**
 * Provides different sets of constants to work with OSM
 *
 * @author Cesar San-Nicolas-Martinez
 */
public class OSMConstants {


    private OSMConstants(){}


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
     * OSM Component types
     */
    public enum OSMComponentType
    {
        CONFIG_AGENT("Configuration Agent"), CONNECTION_POINT("Connection Point"),
        MONITORING_PARAMETER("Monitoring Parameter"), NS("NS"), NSD("NSD"), VDU("VDU"),
        VIM("VIM"), VLD("VLD"), VNF("VNF"), VNFD("VNFD");

        private String type;

        OSMComponentType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return type;
        }
    }

}

