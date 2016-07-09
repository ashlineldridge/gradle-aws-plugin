package awsplugin.cloudformation

class Stack {
    final String name
    String cloudFormationName
    File template
    Map<PropertyKey, String> tags = [:]
    String region

    Map<PropertyKey, PropertyValue> props = [:]
    String currentEnvironment = ''

    Stack(String name) {
        this.name = this.cloudFormationName = name
    }

    def name(String name) {
        this.cloudFormationName = name
    }

    def template(File template) {
        this.template = template
    }

    def tags(Map<String, String> tags) {
        tags.each { k, v -> this.tags.put(new PropertyKey(currentEnvironment, k), v) }
    }

    def setTags(Map<String, String> tags) {
        this.tags.clear()
        this.tags(tags)
    }

    def region(String region) {
        this.region = region
    }

    Map<PropertyKey, PropertyValue> props(String environment) {
        byEnvironment(environment, props)
    }

    Map<PropertyKey, String> tags(String environment) {
        byEnvironment(environment, tags)
    }

	def methodMissing(String m, args) {
		if (args.length > 0 && args[0] instanceof Closure) {
			currentEnvironment = m
			Closure c = args[0]
			c()
			currentEnvironment = ''
		} else {
			throw new MissingMethodException(m, getClass(), (Object[]) args)
		}
	}

	def propertyMissing(String p) {
		new ReferencePropertyValue(currentEnvironment, p)
	}

	def propertyMissing(String p, v) {
		def pk = new PropertyKey(currentEnvironment, p)
		def pv = v instanceof PropertyValue ? v : new SimplePropertyValue(v)
		props.put(pk, pv)
	}

    private <A> Map<PropertyKey, A> byEnvironment(String environment, Map<PropertyKey, A> m) {
        m.findAll { k, _ ->
            if (environment == '') {
                return k.environment == ''
            } else {
                if (k.environment == '') {
                    return props.find { kk, __ -> kk.name == k.name && kk.environment == environment } == null
                }
                return k.environment == environment
            }
        }
    }
}

class PropertyKey {
    final String environment
    final String name

    PropertyKey(String name) {
        this('', name)
    }

    PropertyKey(String environment, String name) {
        this.environment = environment
        this.name = name
    }

    @Override
    def int hashCode() {
        "$environment.$name".hashCode()
    }

    @Override
    def boolean equals(Object o) {
        if (o instanceof PropertyKey) {
            def other = (PropertyKey) o
            return other.hashCode() == hashCode()
        }
        false
    }

    def String toString() {
        def e = environment.isEmpty() ? 'default' : environment
        "PropertyKey: ($e, $name)"
    }
}

