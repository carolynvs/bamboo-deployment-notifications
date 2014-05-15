package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;
import com.atlassian.bamboo.deployments.execution.events.DeploymentFinishedEvent;
import com.atlassian.bamboo.deployments.notification.*;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.*;
import com.atlassian.bamboo.util.Narrow;
import com.atlassian.event.api.EventListener;
import com.atlassian.spring.container.ContainerContext;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
        log.debug("DeploymentFinishedEvent received");

        long deploymentResultId = event.getDeploymentResultId();
        final DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentResultId);

        long environmentId = deploymentResult.getEnvironment().getId();
        NotificationSet notificationSet = environmentService.getNotificationSet(environmentId);
        if (notificationSet == null)
            return;

        LazyReference<DeploymentFinishedNotification> notification = new LazyReference<DeploymentFinishedNotification>() {
            @Override
            protected DeploymentFinishedNotification create()
            {
            return createNotification(deploymentResult);
            }
        };

        Iterable<NotificationRule> notificationRules = getNotifications(notificationSet, event);
        for (NotificationRule rule : notificationRules)
        {
            NotificationRecipient recipient = rule.getNotificationRecipient();
            if (recipient == null)
                continue;

            DeploymentResultAwareNotificationRecipient deploymentAwareRecipient = Narrow.downTo(recipient, DeploymentResultAwareNotificationRecipient.class);
            if (deploymentAwareRecipient != null)
            {
                deploymentAwareRecipient.setDeploymentResult(deploymentResult);
            }

            notification.get().addRecipient(recipient);
        }
        notificationDispatcher.dispatchNotifications(notification.get());
    }

    private DeploymentFinishedNotification createNotification(DeploymentResult deploymentResult)
    {
        ContainerContext containerContext = ContainerManager.getInstance().getContainerContext();
        DeploymentFinishedNotification deploymentFinishedNotification = (DeploymentFinishedNotification) containerContext.createCompleteComponent(DeploymentFinishedNotification.class);

        deploymentFinishedNotification.setDeploymentResult(deploymentResult);

        return deploymentFinishedNotification;
    }

    private Iterable<NotificationRule> getNotifications(NotificationSet notificationSet, final DeploymentFinishedEvent event)
    {
        return Iterables.filter(notificationSet.getNotificationRules(), new Predicate<NotificationRule>() {
            @Override
            public boolean apply(@Nullable NotificationRule input)
            {
                NotificationType notification = input.getNotificationType();
                DeploymentNotificationType deploymentNotification = Narrow.downTo(notification, DeploymentNotificationType.class);
                return deploymentNotification != null && deploymentNotification.isNotificationRequired(event);
            }
        });
    }
}

