package awsplugin

import awsplugin.cloudformation.tasks.CreateStackTask
import awsplugin.cloudformation.tasks.DeleteStackTask
import awsplugin.cloudformation.tasks.ResolveStackPropertiesTask
import awsplugin.cloudformation.Stack
import org.gradle.api.Plugin
import org.gradle.api.Project

class AWSPlugin implements Plugin<Project> {

    void apply(Project project) {
        def stacks = project.container(Stack)
        project.extensions.add('stacks', stacks)
        project.task('createStack', type: CreateStackTask, dependsOn: 'resolveStackProperties')
        project.task('deleteStack', type: DeleteStackTask, dependsOn: 'resolveStackProperties')
        project.task('resolveStackProperties', type: ResolveStackPropertiesTask)
    }
}

