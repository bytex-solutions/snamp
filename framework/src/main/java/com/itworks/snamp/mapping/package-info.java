/**
 * Represents a data mapping layer between resource-specified data types and resource adapters.
 * <p>
 *      Data mapping architecture consists of three layer:
 *      adapter &lt;-- SNAMP type system --&gt; connector
 * <p>
 *     Each resource connector should provide and each resource adapter should understand
 *     the following data types:
 *     <ul>
 *         <li>{@link java.lang.Byte}</li>
 *         <li>{@link java.lang.Short}</li>
 *         <li>{@link java.lang.Integer}</li>
 *         <li>{@link java.lang.Long}</li>
 *         <li>{@link java.math.BigInteger}</li>
 *         <li>{@link java.math.BigDecimal}</li>
 *         <li>{@link java.lang.String}</li>
 *         <li>{@link java.lang.Boolean}</li>
 *         <li>{@link java.lang.Character}</li>
 *         <li>{@link java.util.Date}</li>
 *         <li>{@link com.itworks.snamp.mapping.RecordSet}&lt;{@link java.lang.String}, ?&gt; as map view</li>
 *         <li>{@link com.itworks.snamp.mapping.RowSet}&lt;?&gt; as table view</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see com.itworks.snamp.mapping.TypeConverter
 * @see com.itworks.snamp.mapping.TypeConverterProvider
 * @see com.itworks.snamp.mapping.RecordSet
 * @see com.itworks.snamp.mapping.RecordReader
 * @see com.itworks.snamp.mapping.RecordSetUtils
 */
package com.itworks.snamp.mapping;