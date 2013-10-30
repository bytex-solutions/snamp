package com.snamp.licensing;

import com.snamp.Activator;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents license descriptor of the JMX connector.
 * @author roman
 */
@XmlRootElement(name = "JmxConnector")
public final class JmxConnectorLicenseLimitations extends LicenseLimitations {

    /**
     * Initializes a new limitation descriptor for the JMX connector.
     */
    public JmxConnectorLicenseLimitations(){

    }

    private JmxConnectorLicenseLimitations(final long maxAttributeCount){
        this.maxAttributeCount = MaxRegisteredAttributeCountAdapter.createLimitation(maxAttributeCount);
    }

    /**
     * Returns the currently loaded limitations.
     * @return
     */
    public static JmxConnectorLicenseLimitations current(){
        return LicenseReader.getLimitations(JmxConnectorLicenseLimitations.class, new Activator<JmxConnectorLicenseLimitations>() {
            @Override
            public JmxConnectorLicenseLimitations newInstance() {
                return new JmxConnectorLicenseLimitations(0);
            }
        });
    }

    private static final class MaxRegisteredAttributeCountAdapter extends ExpectationAdapter<Comparable<Long>, Long, MaxValueLimitation<Long>>{

        public static final MaxValueLimitation<Long> createLimitation(final Long expectedValue){
            return new MaxValueLimitation<Long>(expectedValue) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("The maximum number of registered attributes(%s) is reached.", expectedValue));
                }
            };
        }

        @Override
        public final MaxValueLimitation<Long> unmarshal(final Long expectedValue) {
            return createLimitation(expectedValue);
        }
    }

    private static final class MaxInstanceCountAdatper extends ExpectationAdapter<Comparable<Long>, Long, MaxValueLimitation<Long>>{
        public static final MaxValueLimitation<Long> createLimitation(final Long expectedValue){
            return new MaxValueLimitation<Long>(expectedValue) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("The maximum number of JMX connector instances(%s) is reached.", expectedValue));
                }
            };
        }

        @Override
        public MaxValueLimitation<Long> unmarshal(final Long expectedValue) throws Exception {
            return createLimitation(expectedValue);
        }
    }

    @XmlJavaTypeAdapter(MaxRegisteredAttributeCountAdapter.class)
    @XmlElement(type = Long.class)
    private MaxValueLimitation<Long> maxAttributeCount;

    @XmlJavaTypeAdapter(MaxInstanceCountAdatper.class)
    @XmlElement(type = Long.class)
    private MaxValueLimitation<Long> maxInstanceCount;

    public final void verifyMaxAttributeCount(final Long currentAttributeCount) throws LicensingException{
        verify(maxAttributeCount, currentAttributeCount);

    }

    public final void verifyMaxInstanceCount(final Long currentInstanceCount) throws LicensingException{
        verify(maxInstanceCount, currentInstanceCount);
    }
}
