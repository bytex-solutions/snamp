package com.bytex.snamp.testing.adapters.snmp;


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.bytex.snamp.internal.Utils;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.*;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schemaextractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaextractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaloader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;


/**
 * A simple example exposing how to embed Apache Directory Server from the bleeding trunk
 * into an application.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class EmbeddedADSVerTrunk{
    static final String PASSWORD = "snampSimplePassword";
    static final String PRIVACY_KEY = "snampSimpleEncryptionKey";
    public static final int SERVER_PORT = 10389;
    /** The directory service */
    private DirectoryService service;

    /** The LDAP server */
    private LdapServer server;


    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @param dnFactory the DN factory
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition( String partitionId, String partitionDn, DnFactory dnFactory ) throws Exception
    {
        // Create a new partition with the given partition id
        JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
        partition.setId( partitionId );
        partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
        partition.setSuffixDn( new Dn( partitionDn ) );
        service.addPartition( partition );

        return partition;
    }


    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs The list of attributes to index
     */
    @SuppressWarnings("unchecked")
    private void addIndex( Partition partition, String... attrs )
    {
        // Index some attributes on the apache partition
        Set indexedAttributes = new HashSet();

        for ( String attribute : attrs )
        {
            indexedAttributes.add( new JdbmIndex( attribute, false ) );
        }

        ( ( JdbmPartition ) partition ).setIndexedAttributes( indexedAttributes );
    }

    private static String addAttribute(final SchemaManager schema, final String attrName, final String typeName) throws LdapException {
        final LdapSyntax attrSyntax = new LdapSyntax("1.10.0.1.1." + Math.abs(typeName.hashCode()));
        attrSyntax.setHumanReadable(true);
        attrSyntax.setNames(Collections.singletonList(typeName));
        attrSyntax.setSyntaxChecker(new SyntaxChecker() {
            @Override
            public boolean isValidSyntax(final Object o) {
                return o instanceof  String;
            }
        });
        schema.add(attrSyntax);
        final String attributeOid = "1.10.0.1.1." + Math.abs(attrName.hashCode());
        final AttributeType type = new AttributeType(attributeOid){
            {
                syntax = attrSyntax;
                syntaxOid = attrSyntax.getOid();
            }
        };
        type.setNames(attrName);
        schema.add(type);
        return attributeOid;
    }

    /**
     * initialize the schema manager and add the schema partition to diectory service
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition() throws Exception
    {
        InstanceLayout instanceLayout = service.getInstanceLayout();

        final File schemaPartitionDirectory = new File( instanceLayout.getPartitionsDirectory(), "schema" );

        // Extract the schema on disk (a brand new one) and load the registries
        if ( schemaPartitionDirectory.exists() )
        {
            System.out.println( "schema partition already exists, skipping schema extraction" );
        }
        else
        {
            SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( instanceLayout.getPartitionsDirectory() );
            extractor.extractOrCopy();
        }

        SchemaLoader loader = new LdifSchemaLoader( schemaPartitionDirectory );
        SchemaManager schemaManager = new DefaultSchemaManager( loader );
        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix Dn
        schemaManager.loadAllEnabled();
        //security level
        addAttribute(schemaManager, "snamp-snmp-security-level", "SNAMP_SNMP_SECLEVEL");
        //access rights
        addAttribute(schemaManager, "snamp-snmp-allowed-operation", "SNAMP_SNMP_ACCRIGHTS");
        //auth protocol
        final String authProtocolOID =
                addAttribute(schemaManager, "snamp-snmp-auth-protocol", "SNAMP_SNMP_AUTHPROT");
        //priv protocol
        final String privProtocolOID =
                addAttribute(schemaManager, "snamp-snmp-priv-protocol", "SNAMP_SNMP_PRIVPROT");
        //priv key
        final String provKeyOID =
                addAttribute(schemaManager, "snamp-snmp-priv-key", "SNAMP_SNMP_PRIVKEY");
        final ObjectClass snampUserClass = new ObjectClass("1.10.1.2.1"){
            private static final long serialVersionUID = -2390095923917668183L;

            {
                objectClassType = ObjectClassTypeEnum.AUXILIARY;
            }
        };
        snampUserClass.setNames("snampUser");
        snampUserClass.getMustAttributeTypeOids().add(authProtocolOID);
        snampUserClass.getMustAttributeTypeOids().add(privProtocolOID);
        snampUserClass.getMustAttributeTypeOids().add(provKeyOID);
        schemaManager.add(snampUserClass);
        List<Throwable> errors = schemaManager.getErrors();

        if ( errors.size() != 0 )
        {
            throw new Exception( I18n.err( I18n.ERR_317, Exceptions.printErrors( errors ) ) );
        }

        service.setSchemaManager(schemaManager);

        // Init the LdifPartition with schema
        LdifPartition schemaLdifPartition = new LdifPartition( schemaManager, service.getDnFactory() );
        schemaLdifPartition.setPartitionPath( schemaPartitionDirectory.toURI() );

        // The schema partition
        SchemaPartition schemaPartition = new SchemaPartition( schemaManager );
        schemaPartition.setWrappedPartition( schemaLdifPartition );
        service.setSchemaPartition( schemaPartition );
    }


    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
     */
    private void initDirectoryService( File workDir ) throws Exception
    {
        // Initialize the LDAP service
        service = new DefaultDirectoryService();
        service.setInstanceLayout( new InstanceLayout( workDir ) );
        service.setAllowAnonymousAccess(true);

        final CacheService cacheService = new CacheService();
        //this line is necessary to valid loading of EHCACHE ReadWriteCopyStrategy
        //see CopyStrategyConfiguration, line 69
        final Partition userPartition = Utils.withContextClassLoader(cacheService.getClass().getClassLoader(), (Callable<Partition>) () -> {
                cacheService.initialize( service.getInstanceLayout() );
                service.setCacheService( cacheService );
                // first load the schema
                initSchemaPartition();

                // then the system partition
                // this is a MANDATORY partition
                // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
                // while initializing
                JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager(), service.getDnFactory());
                systemPartition.setId( "system" );
                systemPartition.setPartitionPath( new File( service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId() ).toURI() );
                systemPartition.setSuffixDn( new Dn( ServerDNConstants.SYSTEM_DN ) );
                systemPartition.setSchemaManager( service.getSchemaManager() );

                // mandatory to call this method to set the system partition
                // Note: this system partition might be removed from trunk
                service.setSystemPartition( systemPartition );

                // Disable the ChangeLog system
                service.getChangeLog().setEnabled( false );
                service.setDenormalizeOpAttrsEnabled( true );

                // Now we can create as many partitions as we need
                // Create some new partitions named 'foo', 'bar' and 'apache'.
                Partition userPartition1 = addPartition( "users", "dc=ad,dc=microsoft,dc=com", service.getDnFactory() );

                // Index some attributes on the apache partition
                addIndex(userPartition1, "objectClass", "ou", "uid" );

                // And start the service
                service.startup();
                return userPartition1;
            });

        // Inject the context entry for dc=foo,dc=com partition if it does not already exist
        try
        {
            service.getAdminSession().lookup( userPartition.getSuffixDn() );
        }
        catch ( LdapException lnnfe )
        {
            Entry e = service.newEntry( new Dn( "dc=ad,dc=microsoft,dc=com" ) );
            e.add( "objectClass", "top", "domain", "extensibleObject" );
            e.add("dc", "department");
            e.add("snamp-snmp-security-level", "authPriv");
            e.add("snamp-snmp-allowed-operation", "read", "write", "notify");
            service.getAdminSession().add(e);
            e = service.newEntry(new Dn("cn=Roman,dc=ad,dc=microsoft,dc=com"));
            e.add("objectClass", "top", "person", "snampUser");
            e.add("snamp-snmp-auth-protocol", "sha");
            e.add("snamp-snmp-priv-protocol", "aes128");
            e.add("snamp-snmp-priv-key", PRIVACY_KEY);
            e.add("userpassword", PASSWORD);
            e.add("sn", "Sakno");
            service.getAdminSession().add(e);
            //modify admin password
            final Dn adminAccount = new Dn("uid=admin,ou=system");
            service.getAdminSession().modify(adminAccount, new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "userPassword", "1-2-3-4-5-password"));
        }

        // We are all done !
    }


    /**
     * Creates a new instance of EmbeddedADS. It initializes the directory service.
     *
     * @throws Exception If something went wrong
     */
    public EmbeddedADSVerTrunk( File workDir ) throws Exception
    {
        initDirectoryService( workDir );
    }


    /**
     * starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer() throws Exception
    {
        server = new LdapServer();
        server.setTransports( new TcpTransport(SERVER_PORT) );
        server.setDirectoryService( service );
        server.start();
    }

    public void stopServer() throws Exception
    {
        service.shutdown();
        server.stop();
    }


    /**
     * Main class.
     *
     * @param args Not used.
     */
    public static void main(final String[] args )
    {
        try
        {
            final File workDir = new File(System.getProperty("java.io.tmpdir") + "/server-work");

            workDir.mkdirs();

            // Create the server
            EmbeddedADSVerTrunk ads = new EmbeddedADSVerTrunk(workDir);

            // Read an entry
            Entry result = ads.service.getAdminSession().lookup( new Dn( "dc=apache,dc=org" ) );

            // And print it if available
            System.out.println( "Found entry : " + result );

            // optionally we can start a server too
            ads.startServer();
        }
        catch (final Exception e )
        {
            // Ok, we have something wrong going on ...
            e.printStackTrace();
        }
    }
}