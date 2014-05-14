package ut.com.carolynvs.bamboo.plugin.deployment_notifications;

import org.junit.Test;
import com.carolynvs.bamboo.plugin.deployment_notifications.MyPluginComponent;
import com.carolynvs.bamboo.plugin.deployment_notifications.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}