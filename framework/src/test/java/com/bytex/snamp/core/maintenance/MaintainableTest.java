package com.bytex.snamp.core.maintenance;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.management.AbstractMaintainable;
import com.bytex.snamp.management.Maintainable;
import com.bytex.snamp.management.MaintenanceActionInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MaintainableTest extends Assert {
    private enum MaintenanceActions implements MaintenanceActionInfo {
        @SpecialUse
        INC("inc"),
        @SpecialUse
        DEC("dec");

        private final String name;

        MaintenanceActions(final String name){
            this.name = name;
        }

        /**
         * Gets system name of this action,
         *
         * @return The system name of this action.
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Gets description of this action.
         *
         * @param loc The locale of the description.
         * @return The description of this action.
         */
        @Override
        public String getDescription(final Locale loc) {
            return String.format("%s description", name);
        }
    }

    public static final class MaintainableImpl extends AbstractMaintainable<MaintenanceActions>{
        private MaintainableImpl(){
            super(MaintenanceActions.class);
        }

        /**
         * Parses arguments string into an array of method invocation arguments.
         *
         * @param action    Action descriptor.
         * @param arguments The string to parse.
         * @param loc       The localization of the action arguments.
         * @return An array of parsed arguments.
         */
        @Override
        protected Object[] parseArguments(final MaintenanceActions action, final String arguments, final Locale loc) {
            return new Object[]{Integer.parseInt(arguments)};
        }

        @Action
        @SpecialUse
        public String inc(final int value){
            return Integer.toString(value + 1);
        }

        @Action
        @SpecialUse
        public String dec(final int value){
            return Integer.toString(value - 1);
        }

        /**
         * Gets logger associated with this service.
         *
         * @return The logger associated with this service.
         */
        @Override
        @Aggregation
        @SpecialUse
        public Logger getLogger() {
            return Logger.getAnonymousLogger();
        }
    }

    @Test
    public final void maintainableTest() throws ExecutionException, InterruptedException {
        final Maintainable m = new MaintainableImpl();
        assertEquals(2, m.getActions().size());
        assertTrue(m.getActions().contains("inc"));
        assertTrue(m.getActions().contains("dec"));
        assertEquals("inc description", m.getActionDescription("inc", null));
        Future<String> value = m.doAction("inc", "10", null);
        assertNotNull(value);
        assertEquals("11", value.get());
        value = m.doAction("dec", "20", null);
        assertNotNull(value);
        assertEquals("19", value.get());
    }
}
