package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class MultipleFailedDeploymentsNotificationType extends DeploymentNotificationTypeImpl
{
    private static final Logger log = Logger.getLogger(MultipleFailedDeploymentsNotificationType.class);
    private static final String MinFailuresInput = "minFailures";

    private int minFailures = 3;
    private DeploymentResultService deploymentResultService;
    private TemplateRenderer templateRenderer;
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

    @Override
    public void setConfigurationData(@Nullable String data)
    {
        if (data == null)
            return;

        try
        {
            minFailures = Integer.parseInt(data);
        }
        catch (NumberFormatException e)
        {
            log.warn("Unable to parse " + data, e);
        }
    }

    @Override
    @NotNull
    public String getConfigurationData()
    {
        return String.valueOf(minFailures);
    }

    @Override
    public void populate(@NotNull Map<String, String[]> config)
    {
        minFailures = Integer.parseInt(config.get(MinFailuresInput)[0]);
    }

    @Override
    @NotNull
    public ErrorCollection validate(@NotNull Map<String, String[]> config)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        String minFailuresText = config.get(MinFailuresInput)[0];

        int minFailuresValue = minFailures;
        try
        {
            minFailuresValue = Integer.parseInt(minFailuresText);
        }
        catch (NumberFormatException e)
        {
            errors.addError(MinFailuresInput, "Please enter a number");
        }

        if (minFailuresValue < 1)
        {
            errors.addError(MinFailuresInput, "Please enter a number greater than 0");
        }

        return errors;
    }

    @Override
    @NotNull
    public String getEditHtml()
    {
        String editTemplateLocation = notificationTypeModuleDescriptor.getEditTemplate();

        Map<String, Object> context = Maps.newHashMap();
        context.put(MinFailuresInput, minFailures);

        return templateRenderer.render(editTemplateLocation, context);
    }

    @Override
    @NotNull
    public String getViewHtml()
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

    public void setTemplateRenderer(TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
    }
}

