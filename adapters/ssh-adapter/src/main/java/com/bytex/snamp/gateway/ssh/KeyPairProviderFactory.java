package com.bytex.snamp.gateway.ssh;

import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum KeyPairProviderFactory {
    JAVA_KEY {
        @Override
        SimpleGeneratorHostKeyProvider loadPair(final String fileName) {
            return new SimpleGeneratorHostKeyProvider(fileName);
        }
    },

    PEM_KEY{
        @Override
        PEMGeneratorHostKeyProvider loadPair(final String fileName) {
            return new PEMGeneratorHostKeyProvider(fileName);
        }
    };

    abstract KeyPairProvider loadPair(final String fileName);
}
