package awsplugin.sqs.tasks

import awsplugin.cloudformation.tasks.AbstractWaitUntilStackTask

import static awsplugin.cloudformation.StackStatus.*

class WaitUntilSQSQueueEmptyTask extends AbstractWaitUntilStackTask {

    WaitUntilSQSQueueEmptyTask() {
        super([NON_EXISTENT, DELETE_COMPLETE], [DELETE_FAILED])
    }

    @Override
    String usage() {
        '''Usage: gradle waitUntilStackDeleted
        -Pstack.name=<value>'''
    }
}
