package com.bytex.snamp.connectors.md;

import org.apache.htrace.core.Sampler;
import org.apache.htrace.core.TraceScope;
import org.apache.htrace.core.Tracer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HTraceResearch extends Assert {
    @Test
    public void htraceTest(){
        Tracer tracer = new Tracer.Builder("abc")
                .build();
        tracer.addSampler(new Sampler() {
            @Override
            public boolean next() {
                return true;
            }
        });
        try(final TraceScope scope = tracer.newScope("scope1")){
            System.out.println(scope.getSpan().toJson());
            try(final TraceScope scope2 = tracer.newScope("scope2")){
                System.out.println(scope2.getSpan().toJson());
                try(final TraceScope scope3 = tracer.newScope("scope3")){
                    System.out.println(scope3.getSpan().toJson());
                }
            }
        }
    }
}
