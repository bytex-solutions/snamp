package com.bytex.snamp.examples.spring;

import com.bytex.snamp.instrumentation.ApplicationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Component
public final class SpringApplicationInfo extends ApplicationInfo {
    public SpringApplicationInfo(@Value("${spring.application.name}") final String applicationName,
                                 @Value("${spring.application.instance}") final String applicationInstance){
        setName(applicationName);
        setInstance(applicationInstance);
    }
}
