package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import org.jetbrains.annotations.NotNull;

public class FailedDeploymentNotificationType extends DeploymentNotificationTypeImpl
{
    private final DeploymentResultService deploymentResultService;

    public FailedDeploymentNotificationType(DeploymentResultService deploymentResultService)
    {

        this.deploymentResultService = deploymentResultService;
    }

    @Override
    public boolean isNotificationRequired(@NotNull DeploymentEvent event)
    {
        long deploymentResultId = event.getDeploymentResultId();
        final DeploymentResult deploymentResult = deploymentResultService.getDeploymentResult(deploymentResultId);

        BuildState deploymentState = deploymentResult.getDeploymentState();
        return deploymentState == BuildState.FAILED;
    }
}
