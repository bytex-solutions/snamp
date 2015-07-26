/**
 * Represents a bridge between JSR-223 and OSGi.
 * <p>
 *     If you want to load script engine in OSGi environment then you should
 *     replace {@code engine = new ScriptEngineManager().getEngineByName("javascript")}
 *     with {@code engine = new OSGiScriptEngineManager(this.getClass()).getEngineByName("javascript")}
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see com.bytex.snamp.scripting.OSGiScriptEngineManager
 */
package com.bytex.snamp.scripting;