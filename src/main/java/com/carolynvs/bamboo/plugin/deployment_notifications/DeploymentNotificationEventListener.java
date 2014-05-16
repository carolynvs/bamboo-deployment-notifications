package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.execution.events.DeploymentFinishedEvent;
import com.atlassian.bamboo.deployments.notification.*;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.*;
import com.atlassian.bamboo.util.Narrow;
import com.atlassian.event.api.EventListener;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;

public class DeploymentNotificationEventListener
{
    private static final Logger log = Logger.getLogger(DeploymentNotificationEventListener.class);

    private final DeploymentResultService deploymentResultService;
    private final EnvironmentService environmentService;
    private final NotificationDispatcher notificationDispatcher;

    public DeploymentNotificationEventListener(DeploymentResultService deploymentResultService, EnvironmentService environmentService, NotificationDispatcher notificationDispatcher)
    {
        this.deploymentResultService = deploymentResultService;
        this.environmentService = environmentService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @EventListener
    public void onDeploymentFinished(@NotNull DeploymentFinishedEvent event)
    {
        try
        {
            log.debug("DeploymentFinishedEvent received");

            long deploymentResultId = event.getDeploymentResultId();
            final DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentResultId);

            long environmentId = deploymentResult.getEnvironment().getId();
            NotificationSet notificationSet = environmentService.getNotificationSet(environmentId);
            if (notificationSet == null)
                return;

            EvaluateNotificationRules(event, deploymentResult, notificationSet);
        }
        catch(Exception ex)
        {
            log.error("Error in DeploymentNotificationEventListener.onDeploymentFinished");
            log.error(ex);
        }
    }

    private void EvaluateNotificationRules(DeploymentFinishedEvent event, DeploymentResult deploymentResult, NotificationSet notificationSet)
    {
        HashMap<Class, Notification> notifications = new HashMap<Class, Notification>();

        Iterable<NotificationRule> notificationRules = getNotifications(notificationSet, event, deploymentResult);
        for (NotificationRule rule : notificationRules)
        {
            NotificationRecipient recipient = rule.getNotificationRecipient();
            if (recipient == null)
                continue;

            Notification notification = getOrCreateNotification(rule, notifications);
            addNotificationRecipient(notification, recipient, deploymentResult);
        }

        dispatchNotifications(notifications);
    }

    private void addNotificationRecipient(Notification notification, NotificationRecipient recipient, DeploymentResult deploymentResult)
    {
        DeploymentResultAwareNotificationRecipient deploymentAwareRecipient = Narrow.downTo(recipient, DeploymentResultAwareNotificationRecipient.class);
        if (deploymentAwareRecipient != null)
        {
            deploymentAwareRecipient.setDeploymentResult(deploymentResult);
        }
        notification.addRecipient(recipient);
    }

    private Notification getOrCreateNotification(NotificationRule rule, HashMap<Class, Notification> notifications)
    {
        Notification notification;

        DeploymentNotificationType notificationType = (DeploymentNotificationType)rule.getNotificationType();
        Class notificationClass = notificationType.getNotificationClass();

        if(notifications.containsKey(notificationClass))
        {
            notification = notifications.get(notificationClass);
        }
        else
        {
            notification = notificationType.buildNotification();
            notifications.put(notificationClass, notification);
        }

        return notification;
    }

    private void dispatchNotifications(HashMap<Class, Notification> notifications)
    {
        for(Notification notification : notifications.values())
        {
            notificationDispatcher.dispatchNotifications(notification);
        }
    }

    private Iterable<NotificationRule> getNotifications(NotificationSet notificationSet, final DeploymentFinishedEvent event, final DeploymentResult result)
    {
        return Iterables.filter(notificationSet.getNotificationRules(), new Predicate<NotificationRule>() {
            @Override
            public boolean apply(@Nullable NotificationRule input)
            {
                NotificationType notification = input.getNotificationType();
                DeploymentNotificationType deploymentNotification = Narrow.downTo(notification, DeploymentNotificationType.class);
                if(deploymentNotification == null)
                    return false;

                deploymentNotification.setDeploymentEvent(event);
                deploymentNotification.setDeploymentResult(result);

                return deploymentNotification.isNotificationRequired();
            }
        });
    }
}

