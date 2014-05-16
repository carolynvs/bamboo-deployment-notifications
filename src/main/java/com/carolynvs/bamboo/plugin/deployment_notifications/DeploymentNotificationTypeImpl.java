package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.notification.AbstractNotificationType;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.event.Event;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import org.jetbrains.annotations.NotNull;

public abstract class DeploymentNotificationTypeImpl extends AbstractNotificationType implements DeploymentNotificationType
{
    @Override
    public boolean isNotificationRequired(@NotNull Event event)
    {
        // not used
        return true;
    }

    @NotNull
    @Override
    public Notification buildNotification()
    {
        Class notificationClass = getNotificationClass();
        ContainerContext container = ContainerManager.getInstance().getContainerContext();
        return (Notification)container.createComponent(notificationClass);
    }
}
