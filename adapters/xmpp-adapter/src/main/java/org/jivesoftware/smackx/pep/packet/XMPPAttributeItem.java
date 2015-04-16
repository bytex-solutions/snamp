package org.jivesoftware.smackx.pep.packet;

import com.itworks.snamp.adapters.xmpp.XMPPAttributePayload;
import org.jivesoftware.smack.util.StringUtils;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XMPPAttributeItem extends PEPItem {
    private final XMPPAttributePayload payload;

    public XMPPAttributeItem(final XMPPAttributePayload payload) {
        super(StringUtils.randomString(10));
        this.payload = Objects.requireNonNull(payload);
    }

    @Override
    public String getNode() {
        return XMPPAttributePayload.NAMESPACE;
    }

    @Override
    public String getItemDetailsXML() {
        return null;
    }
}
