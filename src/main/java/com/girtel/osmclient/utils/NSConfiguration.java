package com.girtel.osmclient.utils;

import com.girtel.osmclient.json.JSONArray;
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
    JSONObject totalJSON;
    JSONArray vldJSON, vnfJSON;

    /**
     * Constructor
     */
    public NSConfiguration()
    {
        this.vldJSON = new JSONArray();
        this.vnfJSON = new JSONArray();
        this.totalJSON = new JSONObject();
    }

    /**
     * Adds a new configuration option to scecify which network a VLD will be connected to
     * @param vldName VLD name to connect
     * @param vimNetworkName network name
     */
    public void addVLDOption(String vldName, String vimNetworkName)
    {
        JSONObject newVLDOption = new JSONObject();
        newVLDOption.put("vldName", new JSONValue(vldName));
        newVLDOption.put("vimNetworkName", new JSONValue(vimNetworkName));
        vldJSON.add(new JSONValue(newVLDOption));
    }

    /**
     * Adds a new configuration option to specify which VIM a VNF will be instantiated in
     * @param vnfIndex VNF Index to instantiate
     * @param vimName VIM name
     */
    public void addVNFoption(String vnfIndex, String vimName)
    {
        JSONObject newVNFOption = new JSONObject();
        newVNFOption.put("member-vnf-index", new JSONValue(vnfIndex));
        newVNFOption.put("vim_account", new JSONValue(vimName));
        vnfJSON.add(new JSONValue(newVNFOption));
    }

    /**
     * Obtains the VLD Configuration options
     * @return JSONArray including all VLD configuration options
     */
    public JSONArray getVLDOptions()
    {
        return vldJSON;
    }

    /**
     * Obtains the VNF Configuration options
     * @return JSONArray including all VNF configuration options
     */
    public JSONArray getVNFOptions()
    {
        return vnfJSON;
    }

    /**
     * Verifies if there is any VLD configuration options
     * @return true if there is at least one VLD configuration option, false if not
     */
    public boolean IsVLDConfigurationEmpty()
    {
        return vldJSON.isEmpty();
    }

    /**
     * Verifies if there is any VNF configuration options
     * @return true if there is at least one VNF configuration option, false if not
     */
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
