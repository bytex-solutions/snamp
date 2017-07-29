package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;
import groovy.lang.Closure;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GroovyOperationBuilder extends GroovyFeatureBuilder<OperationConfiguration> {
    private Closure<?> operation;
    private final List<OpenMBeanParameterInfoSupplier<?>> parameters;
    private OpenType<?> returnType;
    private String name;
    private int impact;

    GroovyOperationBuilder(){
        parameters = new LinkedList<>();
        returnType = SimpleType.VOID;
        impact = GroovyOperation.UNKNOWN;
    }

    public <T> void parameter(final String name, final String description, final OpenType<T> type){
        final OpenMBeanParameterInfoSupplier<T> supplier = new OpenMBeanParameterInfoSupplier<>(name, description, type);
        parameters.add(supplier);
    }

    public <T> void parameter(final String name, final OpenType<T> type){
        parameter(name, name, type);
    }

    public void implementation(final Closure<?> value){
        operation = Objects.requireNonNull(value);
    }

    public void returns(final OpenType<?> value){
        returnType = Objects.requireNonNull(value);
    }

    public void name(final String value){
        name = Objects.requireNonNull(value);
    }

    String name(){
        return name;
    }

    public void impact(final int value){
        impact = value;
    }

    @Override
    OperationConfiguration createConfiguration() {
        final OperationConfiguration configuration = createConfiguration(OperationConfiguration.class);
        if (!isNullOrEmpty(name))
            configuration.setAlternativeName(name);
        return configuration;
    }

    GroovyOperation build(final String name, final OperationDescriptor descriptor) {
        return new GroovyOperation(name,
                isNullOrEmpty(description) ? "Groovy Operation" : description,
                parameters,
                returnType,
                impact,
                operation,
                descriptor);
    }

    GroovyOperation build(){
        return build(name, new OperationDescriptor(null, super.parameters));
    }
}
