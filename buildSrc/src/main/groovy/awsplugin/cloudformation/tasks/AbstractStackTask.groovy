package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.Stack
import awsplugin.cloudformation.StackStatus
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.Capability
import com.amazonaws.services.cloudformation.model.CreateStackRequest
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import com.amazonaws.services.cloudformation.model.Parameter
import com.amazonaws.services.cloudformation.model.Tag
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

abstract class AbstractStackTask extends DefaultTask {

    final boolean requireTargetStack
    final AmazonCloudFormationClient client = new AmazonCloudFormationClient()

    abstract def run()

    abstract String usage()

    AbstractStackTask() {
        this(true)
    }

    AbstractStackTask(boolean requireTargetStack) {
        this.requireTargetStack = requireTargetStack
    }

    @TaskAction
    def exec() {
        def region = project.aws.region
        if (requireTargetStack) {
            if (!stackName()) userDataError()
            if (!stack()) userDataError("Stack '${stackName()}' is not valid")
            if (stack().region) region = stack().region
        }
        client.setRegion(Region.getRegion(Regions.fromName(region)))
        run()
    }

    def stackName() {
        return project.properties.get('stack.name')
    }

    def environment() {
        return project.properties.get('stack.environment') ?: ''
    }

    def Stack stack() {
        findStack(stackName())
    }

    def Stack findStack(String stackName) {
        project.stacks.find { s -> s.name == stackName || s.cloudFormationName == stackName }
    }

    def Collection<Parameter> stackParameters() {
        def params = []
        stack().props(environment()).each { k, v ->
            params.add(new Parameter().withParameterKey(k.name.capitalize()).withParameterValue(v.value()))
        }
        params
    }

    def Collection<Tag> stackTags() {
        def tags = []
        stack().tags(environment()).each { k, v ->
            tags.add(new Tag().withKey(k.name.capitalize()).withValue(v))
        }
        tags
    }

    def userDataError(String msg = null) {
        throw new InvalidUserDataException(msg != null ? "Error: ${msg}\n${usage()}" : usage())
    }

    StackStatus stackStatus(String stackName = null) {
        stackName = stackName ?: stack().cloudFormationName
        try {
            def res = client.describeStacks(new DescribeStacksRequest().withStackName(stackName))
            if (!res.stacks.isEmpty()) {
                return StackStatus.valueOf(res.stacks.head().stackStatus)
            }
        } catch (AmazonServiceException e) {
            if (e.errorMessage.contains('does not exist')) {
                return StackStatus.NON_EXISTENT
            }
            throw e
        }
    }

    Boolean exists(String stackName = null) {
        def status = stackStatus(stackName)
        return status != StackStatus.NON_EXISTENT && status != StackStatus.DELETE_COMPLETE
    }
}
