package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.notification.AbstractNotificationType;
import com.atlassian.event.Event;
import org.jetbrains.annotations.NotNull;

public class FailedDeploymentNotificationTypeImpl extends AbstractNotificationType implements FailedDeploymentNotificationType
{
    @Override
    public boolean isNotificationRequired(@NotNull Event event)
    {
        return true;
    }
}
