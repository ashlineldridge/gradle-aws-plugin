Gradle AWS Plugin
=================

 cmdline: gradle create-stack -Pstack.name=application -Pstack.environment=sandbox -Pstack.property1=cmdLineOverride -Pstack.teamCityProperty=foobarbaz
 cmdline: gradle update-stack ...
 cmdline: gradle create-or-update-stack ...
 Define utitily tasks such as:
	1. Instances in the stack being healthy
	2. An SQS queue having no messages, etc
 Then the writer of build.gradle is essentially defining a DSL for their infra
 They can define the tasks and the dependencies
 Example tasks:
	- Attach ASG to ELB
	- Detach ASG from ELB
	- Create/update stack
	- Delete stack
	- Wait until a queue is empty
	- Get instance details for instances in an ASG
	- Copy messages from one queue to another
	- Upload local files to S3
	- Wait until a stack is created/updated
	- Wait until instances in an ASG are healthy

 Make as little depend on CloudFormation as possible so teams could use some of the functionality even if not using CFN
 E.g. Could obtain ASG names from CFN output ('stack.scaffolding('output1')') or by specifying directly
