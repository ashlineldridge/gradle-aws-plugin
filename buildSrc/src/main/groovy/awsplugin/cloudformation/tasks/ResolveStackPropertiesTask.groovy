package awsplugin.cloudformation.tasks

import awsplugin.cloudformation.PropertyKey
import awsplugin.cloudformation.PropertyValue
import awsplugin.cloudformation.ReferencePropertyValue
import awsplugin.cloudformation.SimplePropertyValue
import awsplugin.cloudformation.Stack
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException

class ResolveStackPropertiesTask extends AbstractStackTask {

    @Override
    def run() {
        def s = stack()
        applyPropertyOverrides(s)
        def unresolved = s.props
        def resolved = [:]

        unresolved.each { k, v ->
            def vv = resolve(v)
            resolved.put(k, vv)
        }
        s.props = resolved
    }

    @Override
    String usage() {
        '''Usage: gradle resolveStackProperties
        -Pstack.name=<value>
        [-Pstack.environment=<value>]
        [-Pstack.<name>.<propertyKey>=<value> ...]
        [-Pstack.<name>.<environment>.<propertyKey>=<value> ...]'''
    }

    PropertyValue resolve(SimplePropertyValue value) {
        return value
    }

    PropertyValue resolve(ReferencePropertyValue value) {
        def (stackName, region) = getStackValues(value)
        client.setRegion(region)
        try {
            def req = new DescribeStacksRequest().withStackName(stackName)
            def res = client.describeStacks(req)
            def outputKey = value.refs.get(2).capitalize()
            def output = res.stacks.head().outputs.find { o -> o.outputKey == outputKey }
            if (output == null)
                throw new InvalidUserDataException("Output key '${outputKey}' does not exist for stack '${stackName}'")
            return new SimplePropertyValue(output.outputValue)
        } catch (AmazonServiceException e) {
            throw new GradleException("Stack output referenced by property value '${value}' could not be retrieved on stack '${stackName}'", e)
        }
    }

    private Tuple2<String, Region> getStackValues(ReferencePropertyValue value) {
        if (value.refs.size() != 3 || value.refs.get(1) != 'output')
            throw new InvalidUserDataException("Property '${value}' is not a valid stack output reference")
        def referencedStackName = value.refs.head()
        def s = project.stacks.find { s -> s.name == referencedStackName || s.cloudFormationName == referencedStackName }
        def stackName = s != null ? s.cloudFormationName : referencedStackName
        def region = s != null && s.region != null ? s.region : project.aws.region
        new Tuple2(stackName, Region.getRegion(Regions.fromName(region)))
    }

    private void applyPropertyOverrides(Stack stack) {
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
