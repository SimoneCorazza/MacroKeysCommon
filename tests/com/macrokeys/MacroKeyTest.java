package com.macrokeys;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MacroKeyTest {

    public class SetIdTest {

        @Test
        public void errorIfIdNegative() {
            MacroKey m = new MacroKey();
            assertThrows(IllegalArgumentException.class, () -> m.setId(-1));
        }

        @Test
        public void shouldSetId() {
            MacroKey m = new MacroKey();
            m.setId(-1);
            assertEquals(0, m.getId());
        }
    }

    @Test
    public void shouldSetColorFill() {
        MacroKey m = new MacroKey();
        m.setColorFill(2234234);
        assertEquals(2234234, m.getColorFill());
    }
}
