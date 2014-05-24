package com.carolynvs.bamboo.plugin.deployment_notifications;

import com.atlassian.bamboo.deployments.notification.DeploymentFinishedNotification;
import com.atlassian.bamboo.template.TemplateRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MultipleFailedDeploymentsNotification extends DeploymentFinishedNotification
{
    private class Templates
    {
        public static final String InstantMessageText = "templates/MultipleFailedDeployments/InstantMessageText.ftl";
        public static final String InstantMessageHtml = "templates/MultipleFailedDeployments/InstantMessageHtml.ftl";
        private static final String EmailSubject = "templates/MultipleFailedDeployments/EmailSubject.ftl";
    }

    private int numberOfFailures = 0;
    private TemplateRenderer templateRenderer;

    @NotNull
    @Override
    public String getDescription()
    {
        return "Multiple Deployments Failed Notification";
    }

    @Override
    public String getIMContent()
    {
        return templateRenderer.render(Templates.InstantMessageText, getContext());
    }

    @Override
    public String getHtmlImContent()
    {
        return templateRenderer.render(Templates.InstantMessageHtml, getContext());
    }

    @Override
    public String getEmailSubject()
            throws Exception
    {
        return templateRenderer.render(Templates.EmailSubject, getContext());
    }

    @Override
    protected Map<String, Object> getContext()
    {
        Map<String,Object> context = super.getContext();
        context.put("numFailures", numberOfFailures);
        return context;
    }

    public void setNumberOfFailures(int failures)
    {
        this.numberOfFailures = failures;
    }

    @Override
    public void setTemplateRenderer(TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
        super.setTemplateRenderer(templateRenderer);
    }
}