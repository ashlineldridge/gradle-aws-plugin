package awsplugin

import org.gradle.api.GradleException

class Retry {

    static def retry(long intervalSeconds, long maxWaitTimeSeconds, Closure<Boolean> operation) {
        def start = System.currentTimeSeconds()
        while (System.currentTimeSeconds() - start < maxWaitTimeSeconds) {
            if (operation.call()) {
                return
            }
            sleep(intervalSeconds * 1000)
        }
        throw new GradleException("Operation timed out")
    }
}
