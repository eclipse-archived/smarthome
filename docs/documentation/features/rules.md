---
layout: documentation
---

{% include base.html %}

# Rules

Eclipse SmartHome provides a modular rule engine than can be easily extended.

## Concept

*Please describe the general concept/ideas*

In general this rule engine aims to support rules defined with syntax similar to:
```
ON item_id state changed IF item_id.state == desired_value THEN item_id2.state = desired_value2 
```

Each rule can have some basic information like name,tags,description and three module sections (**on,if,then**)
The **'on'** sections is the trigger (eventing) part. 
The **'then'** section contains the actions which specify what should be executed when the event is received.
The **'if'** section lists the conditions which act as a filter for the events - actions of the rule will be executed only if the conditions evaluating the event data are satisfied and return 'true'.

One rule can invoke one and the same operation upon receiving each trigger event, or the operation can be dynamic using input parameters from the event itself or from the system objects

Main building blocks of the rules are modules and each rule consists of one or more instances of each of the following modules:

    trigger - which specifies on which event to execute the rule;
    condition - which acts like a filter for the trigger event and can evaluate the event properties or the state of the system;
    action - which specifies the operation of the rule which will be executed if the condition is statisfied.

Each module is created from a template called "module type" and can specify configuration parameters for the template, like "itemID" for the "ItemTrigger" or "operator" for the "ComparatorCondition"
There are system module types which are provided by the system and there could be added custom module types which are based on the system module types with predefined configurations like "ItemTrigger:MotionTrigger" which is based on the ItemTrigger but specifies in its configuration that it is triggered only on item's motion events

**Module type** has the following elements:

    ID
    label - localizable text
    description - localizable text
    configuration properties
    input variables
    output variables

**Configuration property** has the following metadata

    ID
    type - one of the following "text", "integer", "decimal", "boolean"
    label - localizable text
    description - localizable text
    required - boolean flag indicating if this configuration property can be optional and thus it can be ommited in the rule, by default required is false
    defaultValue - default value for the configuration property when not specified in the rule

**Input property** has the following metadata

    ID
    type - fully qualified name of Java class ("java.lang.Integer")
    label - localizable text
    description - localizable text
    defaultValue - default value for the configuration property when not specified in the rule
    tags - shows how to be considered a given value. For example, as a Temperature


**Output property** has the following metadata

    ID
    type - fully qualified name of Java class ("java.lang.Integer")
    label - localizable text
    description - localizable text
    defaultValue - default value for the configuration property when not specified in the rule
    reference - which means the property value can be specified as a reference to configuration parameter or input parameter
    tags - shows how to be considered a given value. For example, as a Temperature

**Supported Java Types**
The java types supported in the **input/output** objects are:

- Integer
- Short
- Long
- Byte
- Float
- Double
- Boolean
- Character
- String
- StringBuffer
- Array of the above types
- List of the above types
- HashMap (keys are String)

The types in the **Configuration** object are restricted to the following:

- TEXT - The data type for a UTF8 text value.
- INTEGER - The data type for a signed integer value in the range of Integer#MIN_VALUE, Integer#MAX_VALUE
- DECIMAL - The data type for a signed floating point value (IEEE 754) in the range of Float#MIN_VALUE, Float#MAX_VALUE
- BOOLEAN - The data type for a boolean: true or false


## Defining Rules via JSON

*Please describe how do define rules with JSON in a bundle. Also showing the JSON syntax (incl. reference to the JSON schema)*

**JSON schemas for:**

 * [module types](../../../bundles/automation/org.eclipse.smarthome.automation.json/src/main/resources/ModuleTypeDefinitions_JSONschema.json)
 * [rule templates](../../../bundles/automation/org.eclipse.smarthome.automation.json/src/main/resources/TemplateDefinitions_JSONschema.json)
 * [rule instances](../../../bundles/automation/org.eclipse.smarthome.automation.json/src/main/resources/RuleDefinitions_JSONschema.json)


### Sample Rules

*Show some sample rules*
    

 * **Sample rule instance referencing module types:**
```
{  
    "uid": "sample.rule1",
    "name": "SampleRule",
    "tags": [  
      "sample",
      "rule"
    ],
    "description": "Sample Rule definition.",
    "on": [  
      {  
        "id": "SampleTriggerID",
        "type": "SampleTrigger"
      }
    ],
    "if": [
      {
        "id": "SampleConditionID",
        "type": "SampleCondition",
        "config": {
          "operator": "=",
          "constraint": "dtag"
        },
        "input": {
          "conditionInput": "SampleTriggerID.triggerOutput"
        }
      }
    ],
    "then": [
      {  
        "id": "SampleActionID",
        "type": "SampleAction",
        "config": {  
          "message": ">>> Hello World!!!"
        }
      }
    ]
  }
```

 * **Sample module types:**

```
  "triggers": {
    "SampleTrigger": {
      "label": "SampleTrigger label",
      "description": "Sample Trigger description.",
      "output": {
        "triggerOutput": {
          "type": "java.lang.String",
          "label": "TriggerOutput label",
          "description": "Text from user input or default message.",
          "reference": "consoleInput",
          "defaultValue": "dtag"
        }
      }
	}, 
    "SampleTrigger:CustomTrigger": {
      "label": "CustomTrigger label",
      "description": "Custom Trigger description.",
      "output": {
        "customTriggerOutput": {
          "type": "java.lang.String",
          "label": "CustomTriggerOutput label",
          "description": "Text from user input or default message.",
          "reference": "$triggerOutput"
        }
      }
    }
  }
```

```
  "conditions": {
    "SampleCondition": {
      "label": "SampleCondition label",
      "description": "Sample Condition description",
      "config": {
        "operator": {
          "type": "TEXT",
          "description": "Valid operators are =,>,<,!=",
          "required": true
        },
        "constraint": {
          "type": "TEXT",
          "description": "Right operand which is compared with the input.",
          "required": true
        }
      },
      "input": {
        "conditionInput": {
          "type": "java.lang.String",
          "label": "ConditionInput label",
          "description": "Left operand which will be evaluated.",
          "required": true
        }
      }
    }
  }
```

```
  "actions": {
  	"SampleAction": {
      "label": "SampleAction label",
      "description": "Sample Action description.",
      "config": {  
        "message": {  
          "type": "TEXT",
          "label": "message label",
          "description": "Defines the message description.",
          "defaultValue": "Default message",
          "required": false
        }
      }
    },
    "SampleAction:CustomAction": {
      "label": "CustomAction label",
      "description": "Custom Action description.",
      "config": {  
        "customMessage": {
          "type": "TEXT",
          "label": "custom message label",
          "description": "Defines the custom message description.",
          "context": "(nameRef=$message, valueRef=$customActionInput)",
          "defaultValue": ">>> Default Custom Message",
          "required": false
        }
      },
      "input": {  
        "customActionInput": {
          "type": "java.lang.String",
          "label": "ActionInput label",
          "description": "Text that will be printed.",
          "reference": "$actionInput",
          "required": true
        }
      }
    }
  }
```


## Working with Rules

*listing the ways for interaction (bundle resources, console, Java API, etc.)*

There are several ways to add new rules:

  * using **JAVA API** from package: **org.eclipse.smarthome.automation.api**
  * using **text console commands: smarthome automation**
  * using **resource bundles** that provide moduletypes, rules and rule templates stored in **.json** files
  * using **REST API** - TODO

## JAVA API
`org.eclipse.smarthome.automation.RuleRegistry` - provides main functionality to manage rules in the Rule Engine. It can add rules, get existing ones and remove them from the Rule Engine.

`org.eclipse.smarthome.automation.type.ModuleTypeRegistry` - provides functionality to get module types from the Rule Engine.

`org.eclipse.smarthome.automation.template.TemplateRegistry` - provides functionality to get templates from the Rule Engine.


## Text console commands
`automation listModuleTypes [-st] <filter> ` - lists all Module Types. If filter is present, lists only matching Module Types

`automation listTemplates [-st] <filter> ` - lists all Templates. If filter is present, lists only matching Templates

`automation listRules [-st] <filter> `- lists all Rules. If filter is present, lists only matching Rules

`automation removeModuleTypes [-st] <url> ` - Removes the Module Types, loaded from the given url

`automation removeTemplates [-st] <url ` - Removes the Templates, loaded from the given url

`automation removeRule [-st] <uid> ` - Removes the rule, specified by given UID

`automation removeRules [-st] <filter> `- Removes the rules. If filter is present, removes only matching Rules

`automation importModuleTypes [-p] <parserType> [-st] <url> ` - Imports Module Types from given url. If parser type missing, "json" parser will be set as default

`automation importTemplates [-p] <parserType> [-st] <url> ` - Imports Templates from given url. If parser type missing, "json" parser will be set as default

`automation importRules [-p] <parserType> [-st] <url> ` - Imports Rules from given url. If parser type missing, "json" parser will be set as default

`automation exportModuleTypes [-p] <parserType> [-st] <file> ` - Exports Module Types in a file. If parser type missing, "json" parser will be set as default

`automation exportTemplates [-p] <parserType> [-st] <file> ` - Exports Templates in a file. If parser type missing, "json" parser will be set as default

`automation exportRules [-p] <parserType> [-st] <file> ` - Exports Rules in a file. If parser type missing, "json" parser will be set as default

`automation enableRule [-st] <uid> <enable> ` - Enables the Rule, specified by given UID. If enable parameter is missing, the result of the command will be visualization of enabled/disabled state of the rule, if its value is "true" or "false", the result of the command will be to set enable/disable on the Rule.

 
## Resource bundles
Bundles that provide rules in json format should have the following folder structure 


`ESH-INF\automation\moduletypes` - folder for .json files with module types

`ESH-INF\automation\rules` - folder for .json files with rule instances

`ESH-INF\automation\templates` - folder for .json files with rule templates


## Rule Templates

*Please describe rule template approach*

Rule templates can simplify the definition of rules with similar behavior, by providing additional configuration properties. Then rule instance definition only refers the rule template and provides the values of the configuration properties.

 * **Sample rule instance referencing rule template:**
```
  {  
    "uid": "sample.rulebytemplate",
    "name": "RuleByTemplate",
    "template.uid": "SampleRuleTemplate",
    "tags": [  
      "rule",
      "template"
    ],
    "config": {  
      "condition_operator": "!=",
      "condition_constraint": "template"
    }
  }
```

 * **Sample rule template:**
```
  {  
    "uid":"SampleRuleTemplate",
    "description":"Sample Rule Template.",
    "tags":[  
      "sample",
      "rule",
      "template"
    ],
    "config":{  
        "condition_operator": {
          "type": "TEXT",
          "description": "Valid operators are =,>,<,!=",
          "required": true
        },
        "condition_constraint": {
          "type": "TEXT",
          "description": "Right operand which is compared with the input.",
          "required": true
        }
    },
    "on": [  
      {  
        "id": "CustomSampleTriggerTemplateID",
        "type": "SampleTrigger:CustomTrigger"
      }
    ],
    "if": [
      {
        "id": "SampleConditionTemplateID",
        "type": "SampleCondition",
        "config": {
          "operator": "$condition_operator",
          "constraint": "$condition_constraint"
        },
        "input": {
          "conditionInput": "CustomSampleTriggerTemplateID.customTriggerOutput"
        }
      }
    ],
    "then": [
      {  
        "id": "CustomActionTemplateID",
        "type": "SampleAction:CustomAction",
        "input": {  
          "customActionInput": "CustomSampleTriggerTemplateID.customTriggerOutput"
        }
      }
    ]
  } 
```

The above example uses two rule configuration properties: "condition_operator" and "condition_constraint" that update the configuration of the "SampleCondition"


## System Module Types

*Please describe the list of system module types*

TODO: describe the sample module types

TODO: mention the Item triggers and actions

## Providing new Module Types

*Please describe how to provide new module types (which interfaces to implement and so on)*

The rule engine is pluggable - any OSGi bundle can provide implementation for triggers,actions and condition module types and their corresponding metatype definition in JSON format. 

The extension bundle should register the service ModuleHandlerFactory (org.eclipse.smarthome.automation.handler.ModuleHandlerFactory) 
and implement the needed methods to create instances of the supported module handlers:

- org.eclipse.smarthome.automation.handler.TriggerHandler
- org.eclipse.smarthome.automation.handler.ConditionHandler
- org.eclipse.smarthome.automation.handler.ActionHandler


## Custom module types
Another way to extend the supported module types is by defining custom module types as extension of the system module types. All module types which have in its type the symbol ':' are extensions of the system module types and they can redefine the configuration properties and the input objects of the parent type

```
    "SampleAction:CustomAction": {
      "label": "CustomAction label",
      "description": "Custom Action description.",
      "config": {  
        "customMessage": {
          "type": "TEXT",
          "label": "custom message label",
          "description": "Defines the custom message description.",
          "context": "(nameRef=$message, valueRef=$customActionInput)",
          "defaultValue": ">>> Default Custom Message",
          "required": false
        }
      },
      "input": {  
        "customActionInput": {
          "type": "java.lang.String",
          "label": "ActionInput label",
          "description": "Text that will be printed.",
          "reference": "$actionInput",
          "required": true
        }
      }
    }
  }
```
This example demonstrates extending the system action "SampleAction", which has configuration property "message" with another action "SampleAction:CustomAction", which defines input object "customActionInput". This input object references the configuration property "message" via the "context" attribute of the auxiliary property "customMessage". 

```
"context": "(nameRef=$message, valueRef=$customActionInput)",
```

- `nameRef` specifies which property to be updated (in this case it is the property "message")
- `valueRef` specifies that the value of the property will be provided by the input "customActionInput"


Then the rule or rule template will use the CustomAction input object instead of the configuration property:

```
    "then": [
      {  
        "id": "CustomActionTemplateID",
        "type": "SampleAction:CustomAction",
        "input": {  
          "customActionInput": "CustomSampleTriggerTemplateID.customTriggerOutput"
        }
      }
    ]
```
