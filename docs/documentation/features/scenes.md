---
layout: documentation
---

{% include base.html %}

# Scenes

A scene is a defined set of states which several items should have when the scene is active.
It can be activated either manually or by a simple trigger, e.g., a time based rule.

## Concept

A scene is merely a set of actions inside a rule that is executed by the rule engine. Each action yields an item into a target state once the scene is activated. It is advisable to have a tag like "scene" on a rule to express its purpose.

The scene can be activated either manually or automatically. To manually activate the scene you will need to make a call to rest endpoint `PUT /rest/rules/{ruleUID}/runnow`. To automate the scene activation, a secondary rule must be created. The action section of this secondary rule is responsible for executing the scene with the `core.RunRuleAction` actionhandler. 

The action module, used for running the scene, must support the direct execution of rules without requiring to evaluate the conditions. This way all actions will be executed even if an action before returns an error.



Asynchronous execution of actions is needed to fulfil further requirements:

* Most of the actions just send events. And as the eventHandlers for these events are executed asynchronously the resulting action execution is also asynchronous.
* For long-running script actions, e.g., the handler should be aware of asynchronous execution.


## Defining Scene via JSON
A scene can be created by making a POST request to the following endpoint provided by the rule-engine:

`POST /rest/rules`


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

 
