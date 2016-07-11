# Binding #1 Binding - Author #1

This is the binding number one

## Supported Things

## Things
<table>
<thead>
<th>Thing Type Id</th>
<th>Channels</th>
<th>Channel Groups</th>
<th>Config</th>
<th>Description</th>
</thead>
<tbody>
<tr>
<td><a name="thing-id-thermostat"></a>thermostat</td>
<td>  <a href="#channel-id-roomSetpoint">roomSetpoint</a>,    <a href="#channel-id-heatingMode">heatingMode</a>,    <a href="#channel-id-currentRoomTemperature">currentRoomTemperature</a>,    <a href="#channel-id-schedule">schedule</a>,    <a href="#channel-id-scheduleState">scheduleState</a>,    <a href="#channel-id-activePreset">activePreset</a>,    <a href="#channel-id-gatewayEnvironment">gatewayEnvironment</a>,    <a href="#channel-id-savingsResultUnit">savingsResultUnit</a>,    <a href="#channel-id-savingsResultValue">savingsResultValue</a>,    <a href="#channel-id-savingsResultFormUrl">savingsResultFormUrl</a>,    <a href="#channel-id-heatReport">heatReport</a>,    <a href="#channel-id-presetHome">presetHome</a>,    <a href="#channel-id-presetAsleep">presetAsleep</a>,    <a href="#channel-id-presetAway">presetAway</a>,    <a href="#channel-id-presetVacation">presetVacation</a>,    <a href="#channel-id-presetNoFrost">presetNoFrost</a> </td>
<td></td>
<td></td>
<td>An thermostat.</td>
</tr>
<tr>
<td><a name="thing-id-boiler"></a>boiler</td>
<td>  <a href="#channel-id-boilerState">boilerState</a>,    <a href="#channel-id-temperature">temperature</a> </td>
<td></td>
<td></td>
<td>A central heating boiler</td>
</tr>
</tbody>
</table>

## Discovery

## Binding Configuration




## Thing Configuration

## Thing Config
### thermostat
|Name|Type|Properties|Context|Description|
|---|---|---|---|---|
|enableHeatDemandCalculation | BOOLEAN | required=true, readOnly=false |  | Turn on/off the heat demand calculation for incoming heat reports. |
|thermostatId | TEXT | required=false, readOnly=false |  | ID of the Thermostat. |
|applianceId | TEXT | required=false, readOnly=false |  | ID of the &gt;Appliance. |




## Bridges
|Bridge Type Id|Channel Groups|Channels|Description|
|---|---|---|---|
|<a name="bridge-id-bridge"></a>bridge |  |  | The bridge number one.


## Bridge Config

### bridge
|Name|Type|Properties|Context|Description|
|---|---|---|---|---|
|id | TEXT | required=true, readOnly=false |  | &#10;                    The parameter number one.&#10;                 |
|pollingInterval | INTEGER | required=false, readOnly=false, max=300, min=15 |  | &#10;                    The refresh interval for all values.&#10;                 |




## Channels

## Channel types
|Channel Type Id|Item Type|ReadOnly|Options|Description|
|---|---|---|---|---|
<a name="channel-id-roomSetpoint"></a>roomSetpoint | Number |   No   |    | The room temperature setpoint.
<a name="channel-id-heatingMode"></a>heatingMode | String |   No   |  Comfort, Eco, Super eco  | The current operation mode.
<a name="channel-id-currentRoomTemperature"></a>currentRoomTemperature | Number |  Yes    |    | The current measured room temperature.
<a name="channel-id-schedule"></a>schedule | String |   No   |    | The current weekly schedule in JSON format.
<a name="channel-id-scheduleState"></a>scheduleState | Switch |   No   |    | Turn on/off the weekly schedule.
<a name="channel-id-activePreset"></a>activePreset | String |   No   |  Vacation, Home, Away, Night, Frost protection  | The current active preset.
<a name="channel-id-presetHome"></a>presetHome | Number |   No   |    | The temperature setpoint for preset.
<a name="channel-id-presetAsleep"></a>presetAsleep | Number |   No   |    | The temperature setpoint for preset.
<a name="channel-id-presetAway"></a>presetAway | Number |   No   |    | The temperature setpoint for preset.
<a name="channel-id-presetVacation"></a>presetVacation | Number |   No   |    | The temperature setpoint for preset.
<a name="channel-id-presetNoFrost"></a>presetNoFrost | Number |   No   |    | The temperature setpoint for preset.
<a name="channel-id-gatewayEnvironment"></a>gatewayEnvironment | String |   No   |    | The current gateway environment in JSON format.
<a name="channel-id-savingsResultValue"></a>savingsResultValue | Number |  Yes    |    | The saved energy.
<a name="channel-id-savingsResultUnit"></a>savingsResultUnit | String |  Yes    |    | The unit of the saved energy.
<a name="channel-id-savingsResultFormUrl"></a>savingsResultFormUrl | String |  Yes    |    | The url to the savings result input form.
<a name="channel-id-heatReport"></a>heatReport | String |   No   |    | Contains a message with data that gets pushed for further decentral heat demand calculations. The data is written by the SmartLife Plugin into this channel.&#10;        
<a name="channel-id-boilerState"></a>boilerState | Switch |  Yes    |    | Represents the state on/off of the boiler.
<a name="channel-id-temperature"></a>temperature | Number |  Yes    |    | The temperature of boiler.


