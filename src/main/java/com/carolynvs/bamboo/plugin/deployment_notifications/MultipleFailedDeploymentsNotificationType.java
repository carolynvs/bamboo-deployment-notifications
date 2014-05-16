package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultipleFailedDeploymentsNotificationType extends DeploymentNotificationTypeImpl
{
    private int minFailures = 3;
    private DeploymentResultService deploymentResultService;
    private DeploymentResult result;

    @Override
    public boolean isNotificationRequired()
    {
        List<DeploymentResult> recentResults = deploymentResultService.getDeploymentResultsForEnvironment(result.getEnvironmentId(), 1, minFailures - 1);
        for (DeploymentResult recentResult : recentResults)
        {
            if(recentResult.getDeploymentState() != BuildState.FAILED)
            {
                return false;
            }
        }

        return true;
    }

    @NotNull
    @Override
    public String getName()
    {
        return String.format("Notify After %s Consecutive Failures", minFailures);
    }

    @Override
    @NotNull
    public Class getNotificationClass()
    {
        return MultipleFailedDeploymentsNotification.class;
    }

    @Override
    @NotNull
    public Notification buildNotification()
    {
        MultipleFailedDeploymentsNotification notification = (MultipleFailedDeploymentsNotification)super.buildNotification();
        notification.setDeploymentResult(result);
        notification.setNumberOfFailures(minFailures);
        return notification;
    }

    @Override
    public void setDeploymentEvent(@NotNull DeploymentEvent event)
    { }

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

