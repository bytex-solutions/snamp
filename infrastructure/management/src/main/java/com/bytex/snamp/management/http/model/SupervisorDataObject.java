package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("supervisor")
public final class SupervisorDataObject extends AbstractDataObject<SupervisorConfiguration> {
    private final Map<String, ScriptletDataObject> attributeCheckers;
    private ScriptletDataObject trigger;
    private String supervisorType;
    private String connectionStringTemplate;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SupervisorDataObject(){
        attributeCheckers = new HashMap<>();
        supervisorType = SupervisorConfiguration.DEFAULT_TYPE;
        connectionStringTemplate = "";
    }

    public SupervisorDataObject(final SupervisorConfiguration configuration) {
        super(configuration);
        attributeCheckers = Exportable.importEntities(configuration.getHealthCheckConfig().getAttributeCheckers(), ScriptletDataObject::new);
        trigger = new ScriptletDataObject(configuration.getHealthCheckConfig().getTrigger());
        supervisorType = configuration.getType();
        connectionStringTemplate = configuration.getDiscoveryConfig().getConnectionStringTemplate();
    }

    @Override
    public void exportTo(@Nonnull final SupervisorConfiguration entity) {
        super.exportTo(entity);
        if (trigger == null)
            ScriptletConfiguration.fillByDefault(entity.getHealthCheckConfig().getTrigger());
        else
            trigger.exportTo(entity.getHealthCheckConfig().getTrigger());
        Exportable.exportEntities(attributeCheckers, entity.getHealthCheckConfig().getAttributeCheckers());
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
}
