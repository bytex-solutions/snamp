/**
 * Profiles help to reduce complexity of resource adapter implementation when you need
 * to implement polymorphic behavior. A set of behaviors must be grouped into profiles and controlled
 * by {@link com.itworks.snamp.adapters.profiles.ResourceAdapterProfile#PROFILE_NAME} configuration parameter.
 * The administrator may specify the name of the profile in SNAMP configuration. So, you can place each behavior
 * in the separated profile. Polymorphic resource adapter should derive from {@link com.itworks.snamp.adapters.profiles.PolymorphicResourceAdapter}
 * class
 * @see com.itworks.snamp.adapters.profiles.ResourceAdapterProfile
 * @see com.itworks.snamp.adapters.profiles.PolymorphicResourceAdapter
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
package com.itworks.snamp.adapters.profiles;