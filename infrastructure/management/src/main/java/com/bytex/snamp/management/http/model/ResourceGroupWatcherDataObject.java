package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceGroupWatcherDataObject extends AbstractDataObject<ManagedResourceGroupWatcherConfiguration> {
    private final Map<String, ScriptletDataObject> attributeCheckers;
    private ScriptletDataObject trigger;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ResourceGroupWatcherDataObject(){
        attributeCheckers = new HashMap<>();
    }

    public ResourceGroupWatcherDataObject(final ManagedResourceGroupWatcherConfiguration configuration){
        super(configuration);
        attributeCheckers = Exportable.importEntities(configuration.getAttributeCheckers(), ScriptletDataObject::new);
        trigger = new ScriptletDataObject(configuration.getTrigger());
    }

    @Override
    public void exportTo(final ManagedResourceGroupWatcherConfiguration entity) {
        super.exportTo(entity);
        trigger.exportTo(entity.getTrigger());
        Exportable.exportEntities(attributeCheckers, entity.getAttributeCheckers());
    }

    @JsonProperty("attributeCheckers")
    public Map<String, ScriptletDataObject> getAttributeCheckers(){
        return attributeCheckers;
    }

    public void setAttributeCheckers(final Map<String, ScriptletDataObject> value){
        attributeCheckers.clear();
        attributeCheckers.putAll(value);
    }

    @JsonProperty("trigger")
    public ScriptletDataObject getTrigger(){
        return trigger;
    }

    public void setTrigger(final ScriptletDataObject value){
        trigger = Objects.requireNonNull(value);
    }
}
