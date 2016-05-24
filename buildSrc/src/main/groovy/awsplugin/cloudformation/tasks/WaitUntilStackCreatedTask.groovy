package awsplugin.cloudformation.tasks

import static awsplugin.cloudformation.StackStatus.*

class WaitUntilStackCreatedTask extends AbstractWaitUntilStackTask {

    WaitUntilStackCreatedTask() {
        super([CREATE_COMPLETE], [CREATE_FAILED, ROLLBACK_COMPLETE, ROLLBACK_FAILED, ROLLBACK_IN_PROGRESS])
    }

    @Override
    String usage() {
        '''Usage: gradle waitUntilStackCreated
        -Pstack.name=<value>
        [-Pstack.environment=<value>]
        [-Pstack.<name>.<propertyKey>=<value> ...]
        [-Pstack.<name>.<environment>.<propertyKey>=<value> ...]'''
    }
}
