package awsplugin.cloudformation

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

interface PropertyValue {
    String resolve(Project project)
}

class SimplePropertyValue implements PropertyValue {
    final Object value

    SimplePropertyValue(Object value) {
        this.value = value
    }

    def String toString() {
        "SimplePropertyValue: (${value.toString()})"
    }

    @Override
    String resolve(Project project) {
        value.toString()
    }
}

class ReferencePropertyValue implements PropertyValue {
    final AmazonCloudFormationClient client = new AmazonCloudFormationClient()
    final String originEnvironment
    final List<String> refs = []

    ReferencePropertyValue(String originEnvironment, String ref) {
        this.originEnvironment = originEnvironment
        refs.add(ref)
    }

    def propertyMissing(String p) {
        refs.add(p)
        this
    }

    def String toString() {
        refs.join('.')
    }

    @Override
    String resolve(Project project) {
        def referenced = referencedStackName(project)
        def req = new DescribeStacksRequest().withStackName(referenced)
        def res = client.describeStacks(req)
        if (res.stacks.isEmpty())
            throw new InvalidUserDataException("Referenced stack '${referenced}' is unknown to CloudFormation")
        def outputKey = refs.get(2)
        def output = res.stacks.head().outputs.find { o -> o.outputKey == outputKey }
        if (output == null)
            throw new InvalidUserDataException("Output key '${outputKey}' does not exist for stack '${referenced}'")
        output.outputValue
    }

    private Stack referencedStackName(Project project) {
        if (refs.size() != 3 || refs.get(1) != 'output')
            throw new InvalidUserDataException("Property '${toString()}' is not a valid stack output reference")
        def stackName = refs.head()
        def s = project.stacks.find { s -> s.name == stackName || s.cloudFormationName == stackName }
        s != null ? s.cloudFormationName : stackName
    }
}
