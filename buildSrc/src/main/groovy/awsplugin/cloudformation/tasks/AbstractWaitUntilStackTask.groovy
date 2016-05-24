package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.StackStatus
import org.gradle.api.GradleException

abstract class AbstractWaitUntilStackTask extends AbstractStackTask {

    static class Options {
        long pollIntervalSeconds = 5
        long maxWaitTimeSeconds = 600
    }

    private Collection<StackStatus> desiredStates
    private Collection<StackStatus> undesiredStates

    AbstractWaitUntilStackTask(Collection<StackStatus> desiredStates, Collection<StackStatus> undesiredStates) {
        this.desiredStates = desiredStates
        this.undesiredStates = undesiredStates
    }

    @Override
    def run() {
        def start = System.currentTimeSeconds()
        while (System.currentTimeSeconds() - start < options().maxWaitTimeSeconds) {
            def status = stackStatus()
            if (undesiredStates.contains(status))
                throw new GradleException("Operation on CloudFormation stack '${stack().cloudFormationName}' failed: ${status}")
            if (desiredStates.contains(status)) {
                logger.lifecycle("Operation on CloudFormation stack '${stack().cloudFormationName}' complete: ${status}")
                return
            }
            sleep(options().pollIntervalSeconds * 1000)
        }
        throw new GradleException("Operation on CloudFormation stack '${stack().cloudFormationName}' timed out")
    }

    private Options options() {
        project[getName()]
    }
}
