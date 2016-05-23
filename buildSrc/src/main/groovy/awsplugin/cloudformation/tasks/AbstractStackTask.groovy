package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.Stack
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.Capability
import com.amazonaws.services.cloudformation.model.CreateStackRequest
import com.amazonaws.services.cloudformation.model.Parameter
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

abstract class AbstractStackTask extends DefaultTask {

    final AmazonCloudFormationClient client = new AmazonCloudFormationClient()

    abstract def run()

    abstract String usage()

    @TaskAction
    def exec() {
        if (stackName() == null) userDataError()
        if (stack() == null) userDataError("Stack '${stackName()}' is not valid")
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
            params.add(new Parameter().withParameterKey(k.name.capitalize()).withParameterValue(v.resolve(project)))
        }
        params
    }

    private def userDataError(String msg = null) {
        throw new InvalidUserDataException(msg != null ? "Error: ${msg}\n${usage()}" : usage())
    }
}
