package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationType;
import org.jetbrains.annotations.NotNull;

public interface DeploymentNotificationType extends NotificationType
{
    /**
     * Should the notification be fired
     * Note: This supersedes NotificationType.isNotificationRequired
     */
    public boolean isNotificationRequired();

    /**
     * Returns the type of Notification that should be published
     */
    @NotNull
    Class getNotificationClass();

    /**
     * Builds the Notification to be dispatched
     */
    @NotNull
    Notification buildNotification();

    void setDeploymentEvent(@NotNull DeploymentEvent event);

    void setDeploymentResult(@NotNull DeploymentResult result);
}

