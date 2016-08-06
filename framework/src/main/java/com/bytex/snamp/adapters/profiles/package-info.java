/**
 * Profiles help to reduce complexity of resource adapter implementation when you need
 * to implement polymorphic behavior. A set of behaviors must be grouped into profiles and controlled
 * by {@link com.bytex.snamp.adapters.profiles.ResourceAdapterProfile#PROFILE_NAME} configuration parameter.
 * The administrator may specify the name of the profile in SNAMP configuration. So, you can place each behavior
 * in the separated profile. Polymorphic resource adapter should derive from {@link com.bytex.snamp.adapters.profiles.PolymorphicResourceAdapter}
 * class
 * @see com.bytex.snamp.adapters.profiles.ResourceAdapterProfile
 * @see com.bytex.snamp.adapters.profiles.PolymorphicResourceAdapter
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
package com.bytex.snamp.adapters.profiles;