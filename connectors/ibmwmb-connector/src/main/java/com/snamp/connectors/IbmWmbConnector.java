package com.snamp.connectors;

import java.beans.IntrospectionException;
import java.util.Map;


/**
 * In order to build this file you should locate your own copies of ibm java libs
 * When done, execute following commands
 *
 * # mvn install:install-file -Dfile=.\lib\com.ibm.mq.jar -DgroupId=com.ibm.mq -DartifactId=WebsphereMQClassesForJava -Dversion=6.0.2.2 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\connector.jar -DgroupId=javax.resource.cci -DartifactId=SunConnectorClasses -Dversion=1.3.0 -Dpackaging=jar
 * # mvn install:install-file -Dfile=.\lib\ConfigManagerProxy.jar -DgroupId=com.ibm.broker.config -DartifactId=WMBConfigManagerProxy -Dversion=1.5.0 -Dpackaging=jar
 */
class IbmWmbConnector extends ManagementConnectorBean
{

    /**
     * Initializes a new management connector.
     *
     * @param typeBuilder Type information provider that provides property type converter.
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    protected IbmWmbConnector(EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
    }

    public IbmWmbConnector(Map<String, String> env, EntityTypeInfoFactory typeBuilder) throws IntrospectionException {
        super(typeBuilder);
    }
}