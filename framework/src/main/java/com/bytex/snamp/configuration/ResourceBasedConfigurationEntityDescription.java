package com.bytex.snamp.configuration;

import com.bytex.snamp.ResourceReader;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Represents resource-based configuration entity descriptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceBasedConfigurationEntityDescription<T extends EntityConfiguration> extends ResourceReader implements ConfigurationEntityDescription<T> {
    private static final Splitter RELATED_PARAMS_SPLITTER = Splitter.on(',').trimResults();
    private static final String DESCRIPTION_POSTFIX = ".description";
    private static final String REQUIRED_POSTFIX = ".required";
    private static final String PATTERN_POSTFIX = ".pattern";
    private static final String ASSOCIATION_POSTFIX = ".association";
    private static final String EXTENSION_POSTFIX = ".extension";
    private static final String EXCLUSION_POSTFIX = ".exclusion";
    private static final String DEFVAL_POSTIFX = ".default";
    private final Class<T> entityType;
    private final ImmutableSet<String> parameters;

    private ResourceBasedConfigurationEntityDescription(final String baseName,
                                                        final Class<T> entityType,
                                                        final ImmutableSet<String> parameters){
        super(baseName);
        this.entityType = Objects.requireNonNull(entityType);
        this.parameters = parameters == null ? ImmutableSet.of() : parameters;
    }

    /**
     * Initializes a new resource-based descriptor.
     * @param baseName The name of the resource.
     * @param entityType Configuration element type. Cannot be {@literal null}.
     * @param parameters A collection of configuration parameters.
     */
    protected ResourceBasedConfigurationEntityDescription(final String baseName,
                                                          final Class<T> entityType,
                                                          final Collection<String> parameters){
        this(baseName, entityType, ImmutableSet.copyOf(parameters));
    }

    /**
     * Initializes a new resource-based descriptor.
     * @param baseName The name of the resource.
     * @param entityType Configuration element type. Cannot be {@literal null}.
     * @param parameters An array of configuration parameters.
     */
    protected ResourceBasedConfigurationEntityDescription(final String baseName,
                                                          final Class<T> entityType,
                                                          final String... parameters){
        this(baseName, entityType, ImmutableSet.copyOf(parameters));
    }

    /**
     * Returns a type of the configuration entity.
     *
     * @return A type of the configuration entity.
     * @see GatewayConfiguration
     * @see ManagedResourceConfiguration
     * @see EventConfiguration
     * @see AttributeConfiguration
     */
    @Override
    public final Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Determines whether the specified parameter is required.
     * @param parameterName The name of the configuration parameter.
     * @return {@literal true}, if the specified configuration parameter must be presented in the configuration;
     * otherwise, {@literal false}.
     */
    protected boolean isRequiredParameter(final String parameterName) {
        return getBoolean(parameterName + REQUIRED_POSTFIX, null).orElse(false);
    }

    /**
     * Returns description of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc Required localization of the description.
     * @return The localized description of the configuration parameter.
     */
    protected String getParameterDescription(final String parameterName, final Locale loc) {
        return getString(parameterName + DESCRIPTION_POSTFIX, loc).orElse("");
    }

    /**
     * Returns input value pattern (regular expression) of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc Required localization of the pattern.
     * @return The localized input value pattern.
     */
    protected String getParameterValuePattern(final String parameterName, final Locale loc) {
        return getString(parameterName + PATTERN_POSTFIX, loc).orElse("");
    }

    private Collection<String> getRelatedParameters(final String parameterName, final String relationPostfix){
        return getString(parameterName + relationPostfix, null).map(RELATED_PARAMS_SPLITTER::splitToList).orElseGet(Collections::emptyList);
    }

    /**
     * Retrieves a read-only collection of related configuration parameters.
     * @param parameterName The name of the configuration parameters.
     * @param relationship The type of relationship between two configuration parameters.
     * @return A read-only collection of related configuration parameters.
     */
    protected Collection<String> getParameterRelations(final String parameterName, final ParameterRelationship relationship){
        switch (relationship){
            case ASSOCIATION: return getRelatedParameters(parameterName, ASSOCIATION_POSTFIX);
            case EXTENSION: return getRelatedParameters(parameterName, EXTENSION_POSTFIX);
            case EXCLUSION: return getRelatedParameters(parameterName, EXCLUSION_POSTFIX);
            default: return Collections.emptyList();
        }
    }

    /**
     * Returns the default value for of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc The localization of the default value.
     * @return The configuration parameter default value.
     */
    protected String getParameterDefaultValue(final String parameterName, final Locale loc) {
        return getString(parameterName + DEFVAL_POSTIFX, loc).orElse("");
    }

    /**
     * Represents parameter descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    protected class ParameterDescriptionImpl implements ParameterDescription{
        private final String parameterName;

        /**
         * Initializes a new parameter descriptor.
         * @param parameterName The name of the configuration parameter.
         */
        protected ParameterDescriptionImpl(final String parameterName){
            this.parameterName = Objects.requireNonNull(parameterName);
        }

        @Override
        public final String getName() {
            return parameterName;
        }

        @Override
        public final String toString(final Locale loc) {
            return getParameterDescription(parameterName, loc);
        }

        @Override
        public final boolean isRequired() {
            return isRequiredParameter(parameterName);
        }

        @Override
        public final String getValuePattern(final Locale loc) {
            return getParameterValuePattern(parameterName, loc);
        }

        @Override
        public final boolean validateValue(final String value, final Locale loc) {
            if(value == null) return false;
            final String pattern = getValuePattern(loc);
            return pattern == null || pattern.isEmpty() || value.matches(pattern);
        }

        @Override
        public final Collection<String> getRelatedParameters(final ParameterRelationship relationship) {
            return getParameterRelations(parameterName, relationship);
        }

        /**
         * Returns the default value of this configuration parameter.
         *
         * @param loc The localization of the default value. May be {@literal null}.
         * @return The default value of this configuration parameter; or {@literal null} if value is not available.
         */
        @Override
        public final String getDefaultValue(final Locale loc) {
            return getParameterDefaultValue(parameterName, loc);
        }
    }

    /**
     * Creates a new resource-based parameter descriptor.
     * @param parameterName The name of the configuration parameter.
     * @return A new instance of descriptor.
     */
    protected ParameterDescriptionImpl createParameterDescriptor(final String parameterName){
        return new ParameterDescriptionImpl(parameterName);
    }

    /**
     * Returns the description of the specified parameter.
     *
     * @param parameterName The name of the parameter.
     * @return The description of the specified parameter; or {@literal null}, if the specified parameter doesn't exist.
     */
    @Override
    public final ParameterDescription getParameterDescriptor(final String parameterName) {
        return contains(parameterName) ?
                createParameterDescriptor(parameterName) : null;
    }

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this collection
     */
    @Override
    public final int size() {
        return parameters.size();
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public final boolean isEmpty() {
        return parameters.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified
     * element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              (<a href="#optional-restrictions">optional</a>)
     */
    @Override
    public final boolean contains(final Object o) {
        return parameters.contains(o);
    }

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    @Nonnull
    @Override
    public final Iterator<String> iterator() {
        return parameters.iterator();
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * <p/>
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     * <p/>
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    @Override
    @Nonnull
    public final Object[] toArray() {
        return parameters.toArray();
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     * <p/>
     * <p>If this collection fits in the specified array with room to spare
     * (i.e., the array has more elements than this collection), the element
     * in the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any <tt>null</tt> elements.)
     * <p/>
     * <p>If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * <p/>
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     * <p/>
     * <p>Suppose <tt>x</tt> is a collection known to contain only strings.
     * The following code can be used to dump the collection into a newly
     * allocated array of <tt>String</tt>:
     * <p/>
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this collection are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this collection
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this collection
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public final <E> E[] toArray(@Nonnull final E[] a) {
        return parameters.toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns <tt>true</tt> if this collection changed as a
     * result of the call.  (Returns <tt>false</tt> if this collection does
     * not permit duplicates and already contains the specified element.)<p>
     * <p/>
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     * <p/>
     * If a collection refuses to add a particular element for any reason
     * other than that it already contains the element, it <i>must</i> throw
     * an exception (rather than returning <tt>false</tt>).  This preserves
     * the invariant that a collection always contains the specified element
     * after this call returns.
     *
     * @param s element whose presence in this collection is to be ensured
     * @return <tt>true</tt> if this collection changed as a result of the
     * call
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this collection
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     * @throws IllegalArgumentException      if some property of the element
     *                                       prevents it from being added to this collection
     * @throws IllegalStateException         if the element cannot be added at this
     *                                       time due to insertion restrictions
     */
    @Override
    public final boolean add(final String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this collection contains one or more such elements.  Returns
     * <tt>true</tt> if this collection contained the specified element (or
     * equivalently, if this collection changed as a result of the call).
     *
     * @param o element to be removed from this collection, if present
     * @return <tt>true</tt> if an element was removed as a result of this call
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this collection
     */
    @Override
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements
     * in the specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this collection does not permit null
     *                              elements
     *                              (<a href="#optional-restrictions">optional</a>),
     *                              or if the specified collection is null.
     * @see #contains(Object)
     */
    @Override
    public final boolean containsAll(@Nonnull final Collection<?> c) {
        return parameters.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param c collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    @Override
    public final boolean addAll(@Nonnull final Collection<? extends String> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection (optional operation).  After this call returns,
     * this collection will contain no elements in common with the specified
     * collection.
     *
     * @param c collection containing elements to be removed from this collection
     * @return <tt>true</tt> if this collection changed as a result of the
     * call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not support
     *                                       null elements
     *                                       (<a href="#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public final boolean removeAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes from
     * this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not permit null
     *                                       elements
     *                                       (<a href="#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public final boolean retainAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the <tt>release</tt> operation
     *                                       is not supported by this collection
     */
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }
}
