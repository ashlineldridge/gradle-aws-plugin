package awsplugin.cloudformation.tasks

import com.amazonaws.services.cloudformation.model.DeleteStackRequest

class DeleteStackTask extends AbstractStackTask {

    @Override
    def run() {
        def s = stack()
        if (exists()) {
            def req = new DeleteStackRequest().withStackName(s.cloudFormationName)
            client.deleteStack(req)
            logger.lifecycle("Deleted CloudFormation stack '${s.cloudFormationName}'")
        } else {
            logger.lifecycle("CloudFormation stack '${s.cloudFormationName}' does not exist")
        }
    }

    @Override
    String usage() {
        '''Usage: gradle deleteStack
        -Pstack.name=<value>'''
    }
}
