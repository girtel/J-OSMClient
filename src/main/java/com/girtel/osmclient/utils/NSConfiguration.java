package com.girtel.osmclient.utils;

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
 *               help='ns specific yaml configuration:\nvnf: [member-vnf-index: TEXT, vim_account: TEXT]\n'
 *               'vld: [name: TEXT, vim-network-name: TEXT or DICT with vim_account, vim_net entries]')
 */

public class NSConfiguration
{
    private Yaml yaml;
    private List<Map<String, Object>> vnfList;
    private List<Map<String, Object>> vldList;

    /**
     * Constructor
     */
    public NSConfiguration()
    {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(4);
        this.yaml = new Yaml(options);
        this.vnfList = new LinkedList<>();
        this.vldList = new LinkedList<>();
    }


    /**
     * Adds a new configuration parameter to specify in which VIM this VNF will be instantiated
     * @param VNF_memberIndex VNF memberIndex to instantiate
     * @param vimName VIM name
     */
    public void addVNF_VIM_instantiation(String VNF_memberIndex, String vimName)
    {
        Map<String, Object> thisVNF_map = new LinkedHashMap<>();
        thisVNF_map.put(VNF_memberIndex, vimName);
        this.vnfList.add(thisVNF_map);
    }

    /**
     * Adds a new configuration parameter to scecify to which network this VLD will be connected to
     * @param vldName VLD name to connect
     * @param networkName network name
     */
    public void addVLD_network_connection(String vldName, String networkName)
    {
        Map<String, Object> thisVLD_map = new LinkedHashMap<>();
        thisVLD_map.put(vldName, networkName);
        this.vldList.add(thisVLD_map);
    }

    @Override
    public String toString()
    {
        List<Map<String, Object>> yamlList = new LinkedList<>();
        yamlList.addAll(vnfList);
        yamlList.addAll(vldList);
        return yaml.dump(yamlList);
    }
}
