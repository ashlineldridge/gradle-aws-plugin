package awsplugin.cloudformation.tasks

import com.amazonaws.services.cloudformation.model.Capability
import com.amazonaws.services.cloudformation.model.CreateStackRequest

class CreateStackTask extends AbstractStackTask {

    @Override
    def run() {
        def s = stack()
        def req = new CreateStackRequest()
                        .withStackName(s.cloudFormationName)
                        .withTemplateBody(s.template.text)
                        .withCapabilities(Capability.CAPABILITY_IAM)
                        .withParameters(stackParameters())
                        .withTags(stackTags())
        def res = client.createStack(req)
        logger.lifecycle("Created CloudFormation stack '${s.cloudFormationName}' with ID '${res.stackId}'")
    }

    @Override
    String usage() {
        '''Usage: gradle createStack
        -Pstack.name=<value>
        [-Pstack.environment=<value>]
        [-Pstack.<name>.<propertyKey>=<value> ...]
        [-Pstack.<name>.<environment>.<propertyKey>=<value> ...]'''
    }
}
