package awsplugin.cloudformation.tasks

import static awsplugin.cloudformation.StackStatus.*

class WaitUntilStackDeletedTask extends AbstractWaitUntilStackTask {

    WaitUntilStackDeletedTask() {
        super([NON_EXISTENT, DELETE_COMPLETE], [DELETE_FAILED])
    }

    @Override
    String usage() {
        '''Usage: gradle waitUntilStackDeleted
        -Pstack.name=<value>'''
    }
}
