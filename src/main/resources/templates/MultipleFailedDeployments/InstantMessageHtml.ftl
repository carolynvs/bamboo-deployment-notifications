[#-- @ftlvariable name="deploymentResult" type="com.atlassian.bamboo.deployments.results.DeploymentResult" --]
[#-- @ftlvariable name="deploymentProject" type="com.atlassian.bamboo.deployments.projects.DeploymentProject" --]
[#-- @ftlvariable name="triggerReasonShortDescriptionHtml" type="String" --]
[#-- @ftlvariable name="numFailures" type="Integer" --]

[#assign deploymentLink]
<a href="${baseUrl}/deploy/viewDeploymentProjectEnvironments.action?id=${deploymentProject.id}">${deploymentProject.name?html}</a>
[/#assign]

[#assign releaseLink]
<a href="${baseUrl}/deploy/viewDeploymentVersion.action?versionId=${deploymentResult.deploymentVersion.id}">${deploymentResult.deploymentVersion.name?html}</a>
[/#assign]

[#assign environmentLink]
<a href="${baseUrl}/deploy/viewEnvironment.action?id=${deploymentResult.environment.id}">${deploymentResult.environment.name?html}</a>
[/#assign]

[#assign icon="icon-build-unknown.png"/]
[#if deploymentResult.deploymentState == "Successful"]
    [#assign icon="icon-build-successful.png"/]
[#elseif deploymentResult.deploymentState == "Failed"]
    [#assign icon="icon-build-failed.png"/]
[/#if]

<img src='${baseUrl}/images/iconsv4/${icon}'/>&nbsp;[#t]
${deploymentLink} ${releaseLink} [#t]
[#if deploymentResult.deploymentState == "Successful"] was successfully deployed to ${environmentLink} after ${numFailures} [#if numFailures = 1]failure[#else]failures[/#if].[#rt]
[#elseif deploymentResult.deploymentState == "Failed"] failed deploying to ${environmentLink} (${numFailures} [#if numFailures = 1]times[#else]times[/#if]).[#rt]
[#else] stopped deploying to ${environmentLink} after ${numFailures} [#if numFailures = 1]failure[#else]failures[/#if][#rt]
[/#if]
&nbsp;<a href="${baseUrl}/deploy/viewDeploymentResult.action?deploymentResultId=${deploymentResult.id}">See details</a>.
