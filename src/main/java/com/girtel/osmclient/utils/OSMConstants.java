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
        /**
         * OpenStack Type
         */
        OPENSTACK("openstack"),
        /**
         * OpenVIM Type
         */
        OPENVIM("openvim"),
        /**
         * VMWARE Type
         */
        VMWARE("vmware"),
        /**
         * Amazon Web Services Type
         */
        AWS("aws");

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
     * VIMConfiguration Agent Types
     */
    public enum OSMConfigAgentType
    {
        /**
         * Juju type
         */
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
        /**
         * VIMConfiguration Agent type
         */
        CONFIG_AGENT("VIMConfiguration Agent"),
        /**
         * Connection Point type
         */
        CONNECTION_POINT("Connection Point"),
        /**
         * Monitoring Parameter type
         */
        MONITORING_PARAMETER("Monitoring Parameter"),
        /**
         * Network Service type
         */
        NS("NS"),
        /**
         * Network Service Descriptor type
         */
        NSD("NSD"),
        /**
         * Virtual Deployment Unit type
         */
        VDU("VDU"),
        /**
         * Virtual Infrastructure Manager type
         */
        VIM("VIM"),
        /**
         * Virtual Link Descriptor type
         */
        VLD("VLD"),
        /**
         * Virtual Network Function type
         */
        VNF("VNF"),
        /**
         * Virtual Network Function Descriptor type
         */
        VNFD("VNFD");

        private String type;

        /**
         * Constructor
         * @param type OSMComponent type
         */
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

    /**
     * OSM Client version types
     */
    public enum OSMClientVersion
    {
        /**
         * Release three
         */
        RELEASE_THREE,
        /**
         * sol005 version
         */
        SOL_005;
    }

    /**
     * OSM Package type
     */
    public enum OSMPackageType
    {
        /**
         * VNFD type
         */
        VNFD,
        /**
         * NSD type
         */
        NSD;
    }

}

