package awsplugin.cloudformation

interface PropertyValue {
    String value()
}

class SimplePropertyValue implements PropertyValue {
    final Object value

    SimplePropertyValue(Object value) {
        this.value = value
    }

    @Override
    String value() {
        value.toString()
    }

    @Override
    String toString() {
        value()
    }
}

class ReferencePropertyValue implements PropertyValue {
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

    @Override
    String value() {
        refs.join('.')
    }

    @Override
    String toString() {
        value()
    }
}
