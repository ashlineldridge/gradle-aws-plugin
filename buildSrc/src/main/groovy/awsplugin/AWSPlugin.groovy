package awsplugin

import awsplugin.cloudformation.tasks.AbstractWaitUntilStackTask
import awsplugin.cloudformation.tasks.CreateOrUpdateStackTask
import awsplugin.cloudformation.tasks.CreateStackTask
import awsplugin.cloudformation.tasks.DeleteStackTask
import awsplugin.cloudformation.tasks.ResolveStackPropertiesTask
import awsplugin.cloudformation.Stack
import awsplugin.cloudformation.tasks.WaitUntilStackCreatedOrUpdatedTask
import awsplugin.cloudformation.tasks.WaitUntilStackCreatedTask
import awsplugin.cloudformation.tasks.WaitUntilStackDeletedTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AWSPlugin implements Plugin<Project> {

    void apply(Project project) {
        def stacks = project.container(Stack)
        project.extensions.create('aws', AWSPluginOptions)
        project.extensions.add('stacks', stacks)
        project.task('createStack', type: CreateStackTask, dependsOn: 'resolveStackProperties')
        project.task('createOrUpdateStack', type: CreateOrUpdateStackTask, dependsOn: 'resolveStackProperties')
        project.task('deleteStack', type: DeleteStackTask)
        project.task('deleteStacks', type: DeleteStackTask)
        project.task('resolveStackProperties', type: ResolveStackPropertiesTask)
        project.task('waitUntilStackCreated', type: WaitUntilStackCreatedTask, dependsOn: 'createStack')
        project.task('waitUntilStackCreatedOrUpdated', type: WaitUntilStackCreatedOrUpdatedTask, dependsOn: 'createOrUpdateStack')
        project.task('waitUntilStackDeleted', type: WaitUntilStackDeletedTask, dependsOn: 'deleteStack')
    }
}

class AWSPluginOptions {
    String region = 'ap-southeast-2'
}

