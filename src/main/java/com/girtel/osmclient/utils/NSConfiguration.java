package com.girtel.osmclient.utils;

import com.girtel.osmclient.json.JSONObject;
import com.girtel.osmclient.json.JSONValue;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents Configuration parameters to customize NS creation
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */

public class NSConfiguration
{
    JSONObject vldJSON, vnfJSON, totalJSON;

    /**
     * Constructor
     */
    public NSConfiguration()
    {
        this.vldJSON = new JSONObject();
        this.vnfJSON = new JSONObject();
        this.totalJSON = new JSONObject();
    }

    /**
     * Adds a new configuration option to scecify to which network this VLD will be connected to
     * @param vldName VLD name to connect
     * @param networkName network name
     */
    public void addVLDOption(String vldName, String networkName)
    {
    }

    /**
     * Adds a new configuration option to specify in which VIM this VNF will be instantiated
     * @param VNF_memberIndex VNF memberIndex to instantiate
     * @param vimName VIM name
     */
    public void addVNFoption(String VNF_memberIndex, String vimName)
    {
    }

    public boolean IsVLDConfigurationEmpty()
    {
        return vldJSON.isEmpty();
    }

    public boolean isVNFConfigurationEmpty()
    {
        return vnfJSON.isEmpty();
    }

    @Override
    public String toString()
    {
        totalJSON.clear();
        totalJSON.put("vld", new JSONValue(vldJSON));
        totalJSON.put("vnf", new JSONValue(vnfJSON));
        return totalJSON.toString();
    }
}
