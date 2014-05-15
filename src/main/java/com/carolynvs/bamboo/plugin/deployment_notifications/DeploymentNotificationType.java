package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public interface DeploymentNotificationType extends NotificationType
{
    /**
     * Should the notification be fired for the specified event.
     * Note: Since DeploymentFinishedEvent doesn't inherit from Event, this supersedes NotificationType.isNotificationRequired
     */
    public boolean isNotificationRequired(@NotNull DeploymentEvent event);
}

