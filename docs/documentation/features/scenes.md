---
layout: documentation
---

{% include base.html %}

# Scenes

A scene is a defined set of states which several items should have when the scene is active.
It can only be activated, either manually or by a common trigger, e.g., time based.

## Concept


A scene can be handled as a set of actions based on the rule engine. The user can create a rule and define the expected states of the items in the action section. This rule should have a tag like "scene" to express its purpose.
With the Rest Service `PUT /rest/rules/{ruleUID}/runnow` the scene can be activated manually via any UI. Or it can be activated automatically by defining a second rule with triggers and conditions and using the action section of the rule to trigger the scene rule with a specific actionhandler.
For this case, an action module to execute rules directly, similar to **runnow**, is needed. This would only execute the actions of the rule without evaluation of the conditions. And all actions will be executed even if an action before returns an error.

Asynchronous execution of actions is maybe needed to fulfil further requirements:

* For now most of the actions are just sending events, which then is kind of asynchronous because the eventHandlers are executed asynchronously.
* For long-running script actions, e.g., the handler should be aware of asynchronous execution.
* Best solution for asynchronous action execution is that the ruleEngine itself handles the asynchronous execution. There should be a configuration parameter for actions like "wait", "asynchronous", etc. in general which can be evaluated by the ruleEngine. Then the RuleEngine itself should have a thread-pool to also handle stopping/killing of the actions and to set the status of the rule.
But that should be a general topic of the rule engine.


## Defining Scene via JSON
A scene can be created by making a POST request to the following endpoint provided by the rule-engine:

`POST /rest/rules`

### Sample Scene

 * **Sample scene instance created using rule-engine:**

```

{  
   "uid":"sample.scene1",
   "name":"SampleScene",
   "tags":[  
      "sample",
      "scene"
   ],
   "description":"A Sample for a Scene.",
   "triggers":[  

   ],
   "conditions":[  

   ],
   "actions":[  
      {  
         "id":"SampleActionID",
         "type":"SampleAction",
         "configuration":{  
            "message":">>> Hello World!!!"
         }
      },
      {  
         "id":"ItemPostCommandActionID",
         "type":"core.ItemCommandAction",
         "configuration":{  
            "itemName":"myLampItem",
            "command":"ON"
         }
      }
   ]
}

```

 * **This scene either can be activated by sending a PUT request to the following endpoint:**
 `/rest/rules/sample.scene1/runnow`

* **Or by defining an action within another rule as shown in the code block below:**

```
[  
  {  
    "name": "SceneActivationSampleRule",
    "uid": "SceneActivationSampleRule_1",
    "tags": [  
      "sample",
      "item",
      "rule"
    ],
    "configuration": {},
    "description": "Sample Rule for activating scenes.",
    "triggers": [  
      {  
        "id": "ItemStateChangeTriggerID",
        "type": "core.GenericEventTrigger",
        "configuration":{
            "eventSource":"myMotionItem",
            "eventTopic":"smarthome/items/*",
            "eventTypes":"ItemStateEvent"
        }
      }
    ],
    "actions": [
      {  
        "id": "enableSceneAction",
        "type": "core.RunRuleAction",
        "configuration": {  
          "ruleUIDs": "sample.scene1"
		 }
      }
    ]
  }
]
```

## REST API
You will need the rule rest endpoints provided by the rule-engine to work with scenes. Some important endpoints are given below, the complete list can be found [here](rules.md#rest-api).

 - GET /rest/rules - returns all registered rule instances.
 - POST /rest/rules - adds new rule instance to the rule registry.
 - DELETE /rest/rules/{ruleUID} - deletes the specified rule instance.
 - PUT /rest/rules/{ruleUID} - updates the specified rule instance.
 - PUT /rest/rules/{ruleUID}/runnow - executes actions of specified rule instance.
 - GET /rest/rules/{ruleUID}/actions - returns the actions defined for the specified rule instance.

 
