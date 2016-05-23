package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.PropertyKey
import awsplugin.cloudformation.PropertyValue
import awsplugin.cloudformation.ReferencePropertyValue
import awsplugin.cloudformation.SimplePropertyValue
import awsplugin.cloudformation.Stack
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

class ResolveStackPropertiesTask extends DefaultTask {

    @TaskAction
    def run() {
        applyPropertyOverrides()
        project.stacks.each { Stack stack ->
            def unresolved = stack.props
            def resolved = [:]

            unresolved.each { k, v ->
                def vv = v instanceof SimplePropertyValue ? v : resolve(k, v)
                resolved.put(k, vv)
            }
            stack.props = resolved
        }
    }

    def PropertyValue resolve(PropertyKey key, ReferencePropertyValue value) {
        // Don't support properties referencing other properties for now
        return value
    }

    def applyPropertyOverrides() {
        project.stacks.each { Stack stack ->
            project.properties.findAll { k, _ -> k.startsWith("stack.${stack.name}") } each { k, v ->
                def kk = k.split('\\.')
                def pk
                if (kk.length == 3) {
                    pk = new PropertyKey(kk[2])
                } else if (kk.length == 4) {
                    pk = new PropertyKey(kk[2], kk[3])
                } else {
                    throw new InvalidUserDataException("Invalid stack property '$k'. Stack properties must be specified as '-Pstack.stackName[.environment].propertyName=propertyValue'")
                }
                stack.props.put(pk, new SimplePropertyValue(v))
            }
        }
    }

}
