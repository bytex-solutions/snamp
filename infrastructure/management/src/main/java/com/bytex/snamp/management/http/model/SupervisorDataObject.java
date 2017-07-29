package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.json.DurationDeserializer;
import com.bytex.snamp.json.DurationSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents DTO for {@link SupervisorConfiguration}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("supervisor")
public final class SupervisorDataObject extends AbstractDataObject<SupervisorConfiguration> {
    private final Map<String, ScriptletDataObject> attributeCheckers;
    private final Map<String, ScriptletDataObject> scalingPolicies;
    private ScriptletDataObject trigger;
    private String supervisorType;
    private String connectionStringTemplate;
    private boolean autoScaling;
    private int scalingSize;
    private int maxClusterSize;
    private int minClusterSize;
    private Duration cooldownTime;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SupervisorDataObject(){
        attributeCheckers = new HashMap<>();
        scalingPolicies = new HashMap<>();
        supervisorType = SupervisorConfiguration.DEFAULT_TYPE;
        connectionStringTemplate = "";
        cooldownTime = Duration.ZERO;
        maxClusterSize = Integer.MAX_VALUE;
        minClusterSize = 0;
        scalingSize = 1;
    }

    public SupervisorDataObject(final SupervisorConfiguration configuration) {
        super(configuration);
        attributeCheckers = Exportable.importEntities(configuration.getHealthCheckConfig().getAttributeCheckers(), ScriptletDataObject::new);
        scalingPolicies = Exportable.importEntities(configuration.getAutoScalingConfig().getPolicies(), ScriptletDataObject::new);
        trigger = new ScriptletDataObject(configuration.getHealthCheckConfig().getTrigger());
        supervisorType = configuration.getType();
        connectionStringTemplate = configuration.getDiscoveryConfig().getConnectionStringTemplate();
        autoScaling = configuration.getAutoScalingConfig().isEnabled();
        cooldownTime = configuration.getAutoScalingConfig().getCooldownTime();
        scalingSize = configuration.getAutoScalingConfig().getScalingSize();
        maxClusterSize = configuration.getAutoScalingConfig().getMaxClusterSize();
        minClusterSize = configuration.getAutoScalingConfig().getMinClusterSize();
    }

    @Override
    public void exportTo(@Nonnull final SupervisorConfiguration entity) {
        super.exportTo(entity);
        if (trigger == null)
            ScriptletConfiguration.fillByDefault(entity.getHealthCheckConfig().getTrigger());
        else
            trigger.exportTo(entity.getHealthCheckConfig().getTrigger());
        Exportable.exportEntities(attributeCheckers, entity.getHealthCheckConfig().getAttributeCheckers());
        Exportable.exportEntities(scalingPolicies, entity.getAutoScalingConfig().getPolicies());
        entity.getAutoScalingConfig().setEnabled(autoScaling);
        entity.getAutoScalingConfig().setCooldownTime(cooldownTime);
        entity.getAutoScalingConfig().setScalingSize(scalingSize);
        entity.getAutoScalingConfig().setMaxClusterSize(maxClusterSize);
        entity.getAutoScalingConfig().setMinClusterSize(minClusterSize);
        entity.setType(supervisorType);
        entity.getDiscoveryConfig().setConnectionStringTemplate(connectionStringTemplate);
    }

    @JsonProperty("type")
    public String getSupervisorType(){
        return supervisorType;
    }

    public void setSupervisorType(final String value){
        supervisorType = nullToEmpty(value);
    }

    @JsonProperty("attributeCheckers")
    public Map<String, ScriptletDataObject> getAttributeCheckers(){
        return attributeCheckers;
    }

    public void setAttributeCheckers(@Nonnull final Map<String, ScriptletDataObject> value){
        attributeCheckers.clear();
        attributeCheckers.putAll(value);
    }

    @JsonProperty("trigger")
    public ScriptletDataObject getTrigger(){
        return trigger;
    }

    public void setTrigger(final ScriptletDataObject value){
        trigger = value;
    }

    @JsonProperty("connectionStringTemplate")
    public String getConnectionStringTemplate(){
        return connectionStringTemplate;
    }

    public void setConnectionStringTemplate(final String value){
        connectionStringTemplate = nullToEmpty(value);
    }

    @JsonProperty("scalingPolicies")
    public Map<String, ScriptletDataObject> getScalingPolicies(){
        return scalingPolicies;
    }

    public void setScalingPolicies(@Nonnull final Map<String, ScriptletDataObject> value){
        scalingPolicies.clear();
        scalingPolicies.putAll(value);
    }

    @JsonProperty("autoScaling")
    public boolean isAutoScalingEnabled(){
        return autoScaling;
    }

    public void setAutoScalingEnabled(final boolean value){
        autoScaling = value;
    }

    @JsonProperty("cooldownTime")
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getCooldownTime(){
        return cooldownTime;
    }

    @JsonDeserialize(using = DurationDeserializer.class)
    public void setCooldownTime(@Nonnull final Duration value){
        cooldownTime = value;
    }

    @JsonProperty("scalingSize")
    public int getScalingSize(){
        return scalingSize;
    }

    public void setScalingSize(final int value){
        scalingSize = value;
    }

    @JsonProperty("maxClusterSize")
    public int getMaxClusterSize(){
        return maxClusterSize;
    }

    public void setMaxClusterSize(final int value){
        maxClusterSize = value;
    }

    @JsonProperty("minClusterSize")
    public int getMinClusterSize(){
        return minClusterSize;
    }

    public void setMinClusterSize(final int value){
        minClusterSize = value;
    }
}
