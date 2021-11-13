package proxyauth.conf;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Zeckie
 * Copyright and licence details in Main.java
 */
public class TestSetting {
    @Test
    public void testSettingInt() {
        Setting<Integer> setting = new Setting<>(5, Converter.INTEGER, false, "Test number", null, 0, 100);
        Assert.assertEquals((int) setting.currentValue, 5);
        setting.setString("3");
        Assert.assertEquals((int) setting.currentValue, 3);
        Assert.assertEquals("3", setting.toUserString());
    }

    @Test(expected = InvalidSettingException.class)
    public void testInvalidInt() {
        Setting<Integer> setting = new Setting<>(5, Converter.INTEGER, false, "Test number", null, 0, 100);
        setting.setString("Foo");
    }

    @Test(expected = InvalidSettingException.class)
    public void testInvalidYesNo() {
        Setting<Boolean> setting = new Setting<>(null, Converter.YES_NO, false, "Test bool", null, null, null);
        setting.setString("Foo");
    }

}
