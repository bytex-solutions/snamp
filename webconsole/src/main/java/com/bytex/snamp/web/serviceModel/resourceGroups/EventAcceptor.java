package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.supervision.SupervisionEventListener;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface EventAcceptor extends HealthStatusEventListener, SupervisionEventListener {
}
