import com.bytex.snamp.Aggregator
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService
import com.google.common.collect.ImmutableMap

import java.util.concurrent.atomic.AtomicInteger

final class ResourceNameGenerator extends AtomicInteger{
    final static ResourceNameGenerator INSTANCE = new ResourceNameGenerator();

    String nextName(){
        return "provisioned-resource-" + Integer.toString(getAndIncrement())
    }
}

public void scaleIn(final Aggregator provisioningContext){
    //just remove resource
    final ResourceDiscoveryService service = provisioningContext.queryObject(ResourceDiscoveryService.class).orElse(null)
    for(final String resourceName: resources)
        if (resourceName.startsWith("provisioned-resource-"))
            service.removeResource(resourceName)
}

public void scaleOut(final Aggregator provisioningContext) {
    //just register another resource
    final ResourceDiscoveryService service = provisioningContext.queryObject(ResourceDiscoveryService.class).orElse(null)
    service.registerResource(ResourceNameGenerator.INSTANCE.nextName(), "", ImmutableMap.of())

}