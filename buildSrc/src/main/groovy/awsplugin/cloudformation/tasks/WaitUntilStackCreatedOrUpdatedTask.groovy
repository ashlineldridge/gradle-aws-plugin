package awsplugin.cloudformation.tasks

import static awsplugin.cloudformation.StackStatus.*

class WaitUntilStackCreatedOrUpdatedTask extends AbstractWaitUntilStackTask {

    WaitUntilStackCreatedOrUpdatedTask() {
        super([CREATE_COMPLETE, UPDATE_COMPLETE],
              [CREATE_FAILED, ROLLBACK_COMPLETE, ROLLBACK_FAILED, ROLLBACK_IN_PROGRESS, UPDATE_ROLLBACK_COMPLETE,
               UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS, UPDATE_ROLLBACK_FAILED, UPDATE_ROLLBACK_IN_PROGRESS])
    }

    @Override
    String usage() {
        '''Usage: gradle waitUntilStackCreatedOrUpdated
        -Pstack.name=<value>
        [-Pstack.environment=<value>]
        [-Pstack.<name>.<propertyKey>=<value> ...]
        [-Pstack.<name>.<environment>.<propertyKey>=<value> ...]'''
    }
}
