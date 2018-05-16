package com.girtel.osmclient;

import com.girtel.osmclient.utils.OSMConstants;

/**
 * This class represents Open Source MANO Monitoring-Parameter component.
 *
 * @author Cesar San-Nicolas-Martinez
 *
 */
public class MonitoringParameter extends OSMComponent{

    private String id, name, value, units, description;

    /**
     * Constructor
     * @param id monitoring parameter's identifier
     * @param name monitoring parameter's name
     * @param value monitoring parameter's current value
     * @param units monitoring parameter's measurement units
     * @param description monitoring parameter's description
     */
    protected MonitoringParameter(String id, String name, String value, String units, String description)
    {
        super(name, OSMConstants.OSMComponentType.MONITORING_PARAMETER);
        this.id = id;
        this.name = name;
        this.value = value;
        this.units = units;
        this.description = description;
    }

    /**
     * Gets monitoring parameter's identifier
     * @return monitoring parameter's identifier
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets monitoring parameter's name
     * @return monitoring parameter's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets monitoring parameter's current value
     * @return monitoring parameter's value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Gets monitoring parameter's measurement units
     * @return monitoring parameter's measurement units
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * Gets monitoring parameter's description
     * @return monitoring parameter's description
     */
    public String getDescription()
    {
        return description;
    }

}
