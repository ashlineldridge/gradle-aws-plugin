package awsplugin.cloudformation.tasks

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.cloudformation.model.Capability
import com.amazonaws.services.cloudformation.model.UpdateStackRequest

class CreateOrUpdateStackTask extends CreateStackTask {

    @Override
    def run() {
        def s = stack()
        if (exists()) {
            def req = new UpdateStackRequest()
                    .withStackName(s.cloudFormationName)
                    .withTemplateBody(s.template.text)
                    .withCapabilities(Capability.CAPABILITY_IAM)
                    .withParameters(stackParameters())
            try {
                def res = client.updateStack(req)
                logger.lifecycle("Updated CloudFormation stack '${s.cloudFormationName}' with ID '${res.stackId}'")
            } catch (AmazonServiceException e) {
                if (e.errorMessage.contains('No updates are to be performed')) {
                    logger.lifecycle("CloudFormation stack '${s.cloudFormationName}' is up to date")
                } else {
                    throw e
                }
            }
        } else {
            super.run()
        }
    }

    @Override
    String usage() {
        '''Usage: gradle createOrUpdateStack
        -Pstack.name=<value>
        [-Pstack.environment=<value>]
        [-Pstack.<name>.<propertyKey>=<value> ...]
        [-Pstack.<name>.<environment>.<propertyKey>=<value> ...]'''
    }
}
