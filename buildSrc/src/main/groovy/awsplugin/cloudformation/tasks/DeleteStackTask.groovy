package awsplugin.cloudformation.tasks

import com.amazonaws.services.cloudformation.model.DeleteStackRequest
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest

class DeleteStackTask extends AbstractStackTask {

    DeleteStackTask() {
        super(false)
    }

    @Override
    def run() {
        if (stackName()) {
            def s = stack()
            def stackName = s != null ? s.cloudFormationName : stackName()
            if (exists(stackName)) {
                deleteStack(stackName)
            } else {
                logger.lifecycle("CloudFormation stack '${stackName}' does not exist")
            }
        } else {
            deleteMatchedStacks()
        }
    }

    def deleteStack(String stackName) {
        def req = new DeleteStackRequest().withStackName(stackName)
        client.deleteStack(req)
        logger.lifecycle("Deleted CloudFormation stack '${stackName}'")
    }

    def deleteMatchedStacks() {
        Closure c = shouldDelete()
        def allStacksResult = client.describeStacks(new DescribeStacksRequest())
        def counter = 0
        allStacksResult.stacks.each { com.amazonaws.services.cloudformation.model.Stack s ->
            if (c.call(s.stackName)) {
                deleteStack(s.stackName)
                counter++
            }
        }
        if (!counter) {
            logger.lifecycle("No CloudFormation stacks were found that matched '${prefix() ?: regex()}'")
        }
    }

    @Override
    String usage() {
        '''Usage: gradle deleteStack
        -Pstack.name=<value> OR -Pstack.name.prefix=<value> OR -Pstack.name.regex=<value>'''
    }

    Closure shouldDelete() {
        def prefix = prefix()
        def regex = regex()
        if (!prefix && !regex) userDataError('Error: Either stack.name, stack.name.prefix, or stack.name.refex must be specified')
        return { String stackName ->
            if (prefix) stackName.startsWith(prefix)
            else stackName.matches(regex)
        }
    }

    def prefix() {
        project.properties.get('stack.name.prefix')
    }

    def regex() {
        project.properties.get('stack.name.regex')
    }
}
