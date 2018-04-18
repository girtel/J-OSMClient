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

        public String getType()
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

        public String getType()
        {
            return type;
        }
    };

}

