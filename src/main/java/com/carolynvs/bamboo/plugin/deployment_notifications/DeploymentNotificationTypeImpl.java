package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.notification.AbstractNotificationType;
import com.atlassian.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class DeploymentNotificationTypeImpl extends AbstractNotificationType implements DeploymentNotificationType
{
    @Override
    public boolean isNotificationRequired(@NotNull Event event)
    {
        // not used
        return true;
    }
}
