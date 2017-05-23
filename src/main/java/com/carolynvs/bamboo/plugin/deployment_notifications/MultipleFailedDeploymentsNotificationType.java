package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.execution.events.DeploymentEvent;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.deployments.results.service.DeploymentResultService;
import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.persistence3.PluginHibernateSessionFactory;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;

public class MultipleFailedDeploymentsNotificationType extends DeploymentNotificationTypeImpl
{
    private static final Logger log = Logger.getLogger(MultipleFailedDeploymentsNotificationType.class);
    private static final String MinFailuresInput = "minFailures";

    private int minFailures = 3;
    private int numberOfFailures;
    private DeploymentResultService deploymentResultService;
    private TemplateRenderer templateRenderer;
    private DeploymentResult result;
    private PluginHibernateSessionFactory sessionFactory;

    private static final String countNumberOfFailures =
"select count(*) " +
"from com.atlassian.bamboo.deployments.results.persistence.MutableDeploymentResultImpl r\n" +
"where r.environment.id = :environmentId and r.id > :lastSuccessfulResultId and r.deploymentState != 'Successful'";

    @Override
    public boolean isNotificationRequired()
    {
        return IsFirstSuccess() || HasReachedMinimumFailures();
    }

    private boolean HasReachedMinimumFailures()
    {
        if (result.getDeploymentState() == BuildState.SUCCESS)
            return false;

        try
        {
            loadNumberOfFailures();
            return numberOfFailures >= minFailures;
        }
        catch (HibernateException e)
        {
            log.error(e);
            return false;
        }
    }

    private boolean IsFirstSuccess()
    {
        if (result.getDeploymentState() != BuildState.SUCCESS)
            return false;

        DeploymentResult previousResult = deploymentResultService.getLastResultBefore(result);
        if(previousResult != null && previousResult.getDeploymentState() == BuildState.SUCCESS)
        {
            return false;
        }

        try
        {
            loadNumberOfFailures();
            return true;
        }
        catch (HibernateException e)
        {
            log.error(e);
            return false;
        }
    }

    private void loadNumberOfFailures()
            throws HibernateException
    {
        DeploymentResult lastSuccess = deploymentResultService.getLastResultInStatesBefore(result, EnumSet.of(BuildState.SUCCESS));
        long lastSuccessfulResultId = lastSuccess != null ? lastSuccess.getId() : 0;
        long environmentId = result.getEnvironmentId();

        // the session returned from getSession is always closed, this allows us to manage our own session
        Session session = sessionFactory.getSession().getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        numberOfFailures = (Integer)session.createQuery(countNumberOfFailures)
                    .setParameter("environmentId", environmentId)
                    .setParameter("lastSuccessfulResultId", lastSuccessfulResultId)
                    .uniqueResult();

        transaction.commit();
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
        notification.setNumberOfFailures(numberOfFailures);

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

    public void setPluginHibernateSessionFactory(PluginHibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

