package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.notification.DeploymentFinishedNotification;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.spring.container.ContainerManager;
import org.jetbrains.annotations.NotNull;

public class FailedDeploymentNotificationType extends DeploymentNotificationTypeImpl
{
    private DeploymentResult result;
    private DeploymentEvent event;
    private DeploymentResultService deploymentResultService;

    @Override
    public boolean isNotificationRequired()
    {
        // Notify when failed
        BuildState deploymentState = result.getDeploymentState();
        if(deploymentState != BuildState.SUCCESS)
        {
            return true;
        }

        // Notify on first success
        DeploymentResult previousResult = deploymentResultService.getLastResultBefore(result);
        return previousResult.getDeploymentState() != BuildState.SUCCESS;
    }

    @NotNull
    @Override
    public Class getNotificationClass()
    {
        return DeploymentFinishedNotification.class;
    }

    @NotNull
    @Override
    public Notification buildNotification()
    {
        DeploymentFinishedNotification notification = (DeploymentFinishedNotification)super.buildNotification();
        notification.setDeploymentResult(result);

        return notification;
    }

    @Override
    public void setDeploymentEvent(@NotNull DeploymentEvent event)
    {
        this.event = event;
    }

    @Override
    public void setDeploymentResult(@NotNull DeploymentResult result)
    {
        this.result = result;
    }

    public void setDeploymentResultService(DeploymentResultService deploymentResultService)
    {
        this.deploymentResultService = deploymentResultService;
    }
}
