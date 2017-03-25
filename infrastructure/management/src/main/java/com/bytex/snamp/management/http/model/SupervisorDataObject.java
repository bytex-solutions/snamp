package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SupervisorDataObject extends AbstractDataObject<SupervisorConfiguration> {
    private final Map<String, ScriptletDataObject> attributeCheckers;
    private ScriptletDataObject trigger;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SupervisorDataObject(){
        attributeCheckers = new HashMap<>();
    }

    public SupervisorDataObject(final SupervisorConfiguration configuration){
        super(configuration);
        attributeCheckers = Exportable.importEntities(configuration.getHealthCheckConfig().getAttributeCheckers(), ScriptletDataObject::new);
        trigger = new ScriptletDataObject(configuration.getHealthCheckConfig().getTrigger());
    }

    @Override
    public void exportTo(@Nonnull final SupervisorConfiguration entity) {
        super.exportTo(entity);
        trigger.exportTo(entity.getHealthCheckConfig().getTrigger());
        Exportable.exportEntities(attributeCheckers, entity.getHealthCheckConfig().getAttributeCheckers());
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

    public void setTrigger(@Nonnull final ScriptletDataObject value){
        trigger = value;
    }
}
