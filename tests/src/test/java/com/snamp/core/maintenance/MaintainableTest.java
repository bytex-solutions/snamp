package com.snamp.core.maintenance;

import com.snamp.SnampClassTestSet;
import org.junit.Test;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MaintainableTest extends SnampClassTestSet<AbstractMaintainable<?>> {
    private static enum MaintenanceActions implements MaintenanceActionInfo{
        INC("inc"),
        DEC("dec");

        private final String name;

        private MaintenanceActions(final String name){
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

    private static final class MaintainableImpl extends AbstractMaintainable<MaintenanceActions>{
        public MaintainableImpl(){
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
            return new Object[]{Integer.valueOf(arguments)};
        }

        @Action
        public String inc(final int value){
            return Integer.toString(value + 1);
        }

        @Action
        public String dec(final int value){
            return Integer.toString(value - 1);
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
