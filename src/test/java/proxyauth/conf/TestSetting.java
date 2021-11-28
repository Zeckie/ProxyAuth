/*
 * This file is part of ProxyAuth - https://github.com/Zeckie/ProxyAuth
 * ProxyAuth is Copyright (c) 2021 Zeckie
 *
 * ProxyAuth is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * ProxyAuth is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with ProxyAuth. If you have the source code, this is in a file called
 * LICENSE. If you have the built jar file, the licence can be viewed by
 * running "java -jar ProxyAuth-<version>.jar licence".
 * Otherwise, see <https://www.gnu.org/licenses/>.
 */

package proxyauth.conf;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Zeckie
 */
public class TestSetting {
    @Test
    public void testSettingInt() {
        Setting<Integer> setting = new Setting<>(5, Converter.INTEGER, false, "Test number", null, 0, 100);
        Assertions.assertEquals((int) setting.currentValue, 5);
        setting.setString("3");
        Assertions.assertEquals((int) setting.currentValue, 3);
        Assertions.assertEquals("3", setting.toUserString());
    }

    @Test
    public void testInvalidInt() {
        assertThrows(InvalidSettingException.class,  () -> {
            Setting<Integer> setting = new Setting<>(5, Converter.INTEGER, false, "Test number", null, 0, 100);
            setting.setString("Foo");
        });
    }

    @Test
    public void testInvalidYesNo() {
        assertThrows(InvalidSettingException.class,  () -> {
            Setting<Boolean> setting = new Setting<>(null, Converter.YES_NO, false, "Test bool", null, null, null);
            setting.setString("Foo");
        });
    }

}
