package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
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
import java.lang.annotation.Annotation;

public class FailedDeploymentNotificationEventListener
{
    private static final Logger log = Logger.getLogger(DeploymentNotificationEventListener.class);

    private final DeploymentResultService deploymentResultService;
    private final EnvironmentService environmentService;
    private final NotificationDispatcher notificationDispatcher;

    public FailedDeploymentNotificationEventListener(DeploymentResultService deploymentResultService, EnvironmentService environmentService, NotificationDispatcher notificationDispatcher)
    {
        this.deploymentResultService = deploymentResultService;
        this.environmentService = environmentService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @EventListener
    public void onDeploymentFinished(@NotNull DeploymentFinishedEvent event)
    {
        log.info("FailedDeploymentNotificationEventListener.onDeploymentFinished");
        long deploymentResultId = event.getDeploymentResultId();
        final DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentResultId);

        BuildState deploymentState = deploymentResult.getDeploymentState();
        if(deploymentState != BuildState.FAILED)
            return;

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

        Iterable<NotificationRule> notifications = getApplicableFilters(notificationSet);
        for (NotificationRule rule : notifications)
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

    private Iterable<NotificationRule> getApplicableFilters(NotificationSet notificationSet)
    {
        return Iterables.filter(notificationSet.getNotificationRules(), new Predicate<NotificationRule>() {
            @Override
            public boolean apply(@Nullable NotificationRule input) {
                NotificationType notificationType = input.getNotificationType();
                return notificationType instanceof FailedDeploymentNotificationType;
            }
        });
    }
}

