package awsplugin.cloudformation.tasks

import com.amazonaws.services.cloudformation.model.Capability
import com.amazonaws.services.cloudformation.model.CreateStackRequest
import com.amazonaws.services.cloudformation.model.DeleteStackRequest

class DeleteStackTask extends AbstractStackTask {

    @Override
    def run() {
        def s = stack()
        def req = new DeleteStackRequest().withStackName(s.cloudFormationName)
        client.deleteStack(req)
        logger.lifecycle("Deleted CloudFormation stack '${s.cloudFormationName}'")
    }

    @Override
    String usage() {
        '''Usage: gradle deleteStack
        -Pstack.name=<value>'''
    }
}
