/**
 *  Zigbee trv for hubitat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * 
 * TO CONNECT: short press home (turn on), long press home (enter settings), go to setting 5 (wifi logo), press home (only wifi now showing), long press home (wifi now blinking) 
 *
 * KNOWN ERRORS
 * 1 - groovy.lang.GroovyRuntimeException: Cannot read write-only property: schedule on line 544 (refresh)        
 *     schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 1/3 * * ? *", 'checkPresence')
 *
 * UNKNOWN MESSAGES
 * 0104 000A 01 01 0040 00 962D 00 00 0000 00 00 0700 <--?
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 24 01 0074 <--after installing
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19056602000400000005
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19066702000400000016
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190702020004000000C
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19080302000400000131
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19090302000400000131
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190A0404000102
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190B0404000102
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190C0701000100
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190D0D05000100
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190E2C0200040000005A
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 190F6800000301060A
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 1910690200040000012C
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19116A04000100
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19126B02000400000014
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19136C0200040000000F
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19146D02000400000000
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19156E01000100
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19166F04000102
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19177000001286001608000F0B1E0F0C1E0F141E14160014
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 19187100001206001408000F0B1E0F0C1E0F0F1E1416000F
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 1919720200040000000F
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 191A7301000100
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 191B7401000101
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 01 01 191C7502000400000001
 * 0104 EF00 01 01 0040 00 962D 01 00 0000 11 01 007680
 *
 * 0104 EF00 01 01 0040 00 B06F 01 00 0000 24 01 004E <--after configure
 * 0104 EF00 01 01 0040 00 B06F 01 00 0000 24 01 0000
 * 0104 EF00 01 01 0040 00 9D33 01 00 0000 01 01 1B3C0701000101 <-- tuya command 0701
 *
 * The following seems temperature reports. Cluster 0203?
 * 0104 EF00 01 01 0040 00 B06F 01 00 0000 01 01 174C720200040000000F <-- after thermostat mode setingz
 * 0104 EF00 01 01 0040 00 B06F 01 00 0000 01 01 18C00302000400000122
 *
 * https://github.com/pipiche38/Domoticz-Zigate/blob/4276f21030850b7f48893864fbe0c9ecfabb2ca8/Modules/tuya.py
**/

import hubitat.zigbee.zcl.DataType
import hubitat.helper.HexUtils

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.security.MessageDigest


def getVersionNum() { return "0.0.2" }
private def getVersionLabel() { return "Tuya TRV TS0601" }
private String getDriverVersion() {
    comment = getVersionLabel()
    if(comment != "") state.comment = comment
    String version = getVersionNum()
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}

metadata {
    definition (name: "My Tuya TRV", namespace: "r.brunialti", author: "r.brunialti") {
        
        capability "Battery"
        capability "Configuration"
        capability "TemperatureMeasurement"
        capability "Thermostat"
        capability "ThermostatHeatingSetpoint"
        capability "ThermostatCoolingSetpoint"
        capability "ThermostatSetpoint"
        capability "Refresh"
        
        attribute "valve", "String"
        attribute "WindowOpenDetection","String"
        attribute "autolock","String"
        attribute "childLock","String"
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "number"
        attribute "notPresentCounter", "number"
        attribute "restoredCounter", "number"
        attribute "batteryLastReplaced", "String"

        command "resetRestoredCounter"
        command "resetBatteryReplacedDate"
        command "configure"
        command "refresh"

        fingerprint model:"TS0601", manufacturer:"_TZE200_ckud7u2l", profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,0006,EF00", outClusters:"0019,000A", application:"53"
       }
    preferences {
        //Logging Message Config
        input(name: "presenceEnable", type: "bool", title: styling_addTitleDiv("Enable Presence"), description: styling_addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
        input(name: "presenceWarningEnable", type: "bool", title: styling_addTitleDiv("Enable Presence Warning"), description: styling_addDescriptionDiv("Enables Presence Warnings in the Logs (default: true)"), defaultValue: true)
        input(name: "lastCheckinEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Date"), description: styling_addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
        input(name: "lastCheckinEpochEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Epoch"), description: styling_addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)      
		
        //Battery Voltage Range
 		input name: "voltsmin", title: "Min Volts (0% battery = ___ volts, range 2.0 to 2.9). Default = 2.5 Volts", description: "", type: "decimal", range: "2..2.7"
 		input name: "voltsmax", title: "Max Volts (100% battery = ___ volts, range 2.8 to 3.5). Default = 3.0 Volts", description: "Default = 3.0 Volts", type: "decimal", range: "2.8..3.4"
 		
        //Logging Message Config
        input(name: "infoLogging", type: "bool", title: "Enable info message logging", description: "",defaultValue: true)
		input(name: "debugLogging", type: "bool", title: "Enable debug message logging", description: "",defaultValue: true)   
    }
}

//
def parse(String description) {
    Map map = [:]
    // lastCheckin can be used with webCoRE
    sendEvent(name: "lastCheckin", value: new Date().format("dd/mm/yyyy hh:mm:ss a", location.timeZone))
    
    // BEGIN:getGenericZigbeeParseHeader(loglevel=0)
    //logging("PARSE START---------------------", 0)
    //logging("Parsing: '${description}'", 0)
    ArrayList<String> cmd = []
    Map msgMap = null
    if(description.indexOf('encoding: 4C') >= 0) {
    
      msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 4C', 'encoding: F2'))
    
      msgMap = unpackStructInMap(msgMap)
    
    } else if(description.indexOf('attrId: FF01, encoding: 42') >= 0) {
      msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: F2'))
      msgMap["encoding"] = "41"
      msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false, hasLength=true)
    } else {
      if(description.indexOf('encoding: 42') >= 0) {
        List values = description.split("value: ")[1].split("(?<=\\G..)")
        String fullValue = values.join()
        Integer zeroIndex = values.indexOf("01")
        if(zeroIndex > -1) {
    
          //logging("zeroIndex: $zeroIndex, fullValue: $fullValue, string: ${values.take(zeroIndex).join()}", 0)
          msgMap = zigbee.parseDescriptionAsMap(description.replace(fullValue, values.take(zeroIndex).join()))
    
          values = values.drop(zeroIndex + 3)
          msgMap["additionalAttrs"] = [
              ["encoding": "41",
              "value": parseXiaomiStruct(values.join(), isFCC0=false, hasLength=true)]
          ]
        } else {
          msgMap = zigbee.parseDescriptionAsMap(description)
        }
      } else {
        msgMap = zigbee.parseDescriptionAsMap(description)
      }
    
      if(msgMap.containsKey("encoding") && msgMap.containsKey("value") && msgMap["encoding"] != "41" && msgMap["encoding"] != "42") {
        msgMap["valueParsed"] = zigbee_generic_decodeZigbeeData(msgMap["value"], msgMap["encoding"])
      }
      if(msgMap == [:] && description.indexOf("zone") == 0) {
    
        msgMap["type"] = "zone"
        java.util.regex.Matcher zoneMatcher = description =~ /.*zone.*status.*0x(?<status>([0-9a-fA-F][0-9a-fA-F])+).*extended.*status.*0x(?<statusExtended>([0-9a-fA-F][0-9a-fA-F])+).*/
        if(zoneMatcher.matches()) {
          msgMap["parsed"] = true
          msgMap["status"] = zoneMatcher.group("status")
          msgMap["statusInt"] = Integer.parseInt(msgMap["status"], 16)
          msgMap["statusExtended"] = zoneMatcher.group("statusExtended")
          msgMap["statusExtendedInt"] = Integer.parseInt(msgMap["statusExtended"], 16)
        } else {
          msgMap["parsed"] = false
        }
      }
    }
    //logging("msgMap: ${msgMap}", 0)
    // END:  getGenericZigbeeParseHeader(loglevel=0)

    
    // def msgMap = zigbee.parseDescriptionAsMap(description)
    def cluster = msgMap["clusterId"]

    //displayDebugLog("Parsed message: ${msgMap}")      

    if (description.startsWith('catchall:')) { 
        if (cluster == "0013"){
            displayInfoLog("MULTISTATE CLUSTER EVENT")
        }
        else if (cluster == "8001"){
            displayInfoLog("GENERAL CLUSTER EVENT")
        }
        else if (cluster == "8004"){
            displayInfoLog("Simple Descriptor Information Received - description:${description} | parseMap:${msgMap}")
            updateDataFromSimpleDescriptorData(msgMap["data"])
        }
        else if (cluster == "8021"){
            if(msgMap["data"] != []) {
                displayInfoLog("BIND CONFIRMATION: 0x${msgMap["data"][0]}")
            }
        }
        else if (cluster == "8031"){
            displayInfoLog("Link Quality Cluster Event - description:${description} | parseMap:${msgMap}")
        }
        else if (cluster == "8032"){
            displayInfoLog("Routing Table Cluster Event - description:${description} | parseMap:${msgMap}")
        }
        else if (cluster == "8038"){
            displayInfoLog("GENERAL CATCHALL (0x${msgMap["clusterId"]}")
        }
        else if (cluster == "EF00"){
            def data = msgMap["data"]
            //FILTER CASE MSG LEN>4
            if (data.size()<6){
                displayDebugLog("[1] Cluster ${cluster} - cmd too short - ${msgMap}")
                }
            else{
                def tdata = tuyaMap(msgMap["data"])
                displayDebugLog("TUYAMAP: ${tdata}")
				switch(tdata["dp"]){
				
                    case "0202": //target set point temp
						String SetPoint = HexUtils.hexStringToInt(tdata["data"].join()) / 10
						displayInfoLog("CURENT TEMP SETPOINT: ${SetPoint}")
						sendEvent(name: "heatingSetpoint", value: SetPoint, unit: "C")
						sendEvent(name: "thermostatSetpoint", value: SetPoint, unit: "C")
                        if (device.currentValue("thermostatMode") != "off" && SetPoint.toFloat() > device.currentValue("temperature").toFloat()) { 
							sendEvent(name: "thermostatOperatingState", value: "heating")}
						else { sendEvent(name: "thermostatOperatingState", value: "idle")}
					break

					case '0303': //@?
					case '0302': //@Set Temperature aknowledge?
                    case '0203': //@Room Temperature report?
						String temperature = HexUtils.hexStringToInt(tdata["data"].join())/ 10
                        displayInfoLog("CURRENT ROOM TEMPERATURE: ${temperature}")
						sendEvent(name: "temperature", value: temperature, unit: "C" )
						if (device.currentValue("thermostatMode") != "off" && temperature.toFloat() < device.currentValue("thermostatSetpoint").toFloat()) {
							sendEvent(name: "thermostatOperatingState", value: "heating")}
						else { sendEvent(name: "thermostatOperatingState", value: "idle")}
					break

					case '0404': // Mode
						String mode = HexUtils.hexStringToInt(tdata["data"].join())
					    def smode="?"
                        switch (mode){
							case '0':
								sendEvent(name: "thermostatMode", value: "off" )
                                smode="off"
                            break
							case '1':
								sendEvent(name: "thermostatMode", value: "auto" , descriptionText:"internal programming of device")
                                smode="auto"
							break
							case '2':
                                smode="heat"
							break
						}
                        if (smode!="?"){
					        sendEvent(name: "thermostatMode", value: smode )
				            displayInfoLog("NEW THERMOSTAT MODE:${smode}")
                        }
                        else
				            displayInfoLog("UNKNOWN THERMOSTAT MODE:${smode}")
					break

                    case '0215': // battery
						String volt = HexUtils.hexStringToInt(tdata["data"].join())/ 10
                        displayDebugLog("BATTERY VOLTS (raw value):${volt}")
                        parseAndSendBatteryStatus(Integer.parseInt(volt, 16) / 10.0)
					break

					case '6D02': // Valve position
						String valve = HexUtils.hexStringToInt(tdata["data"].join())
						displayInfoLog("VALVE POSITION:${valve}")
						sendEvent(name: "valve", value: valve, unit: "%", descriptionText: "Valve open ${valve}%")
					break

                    case '2C02': //Temperature correction reporting
						String temperatureCorr = HexUtils.hexStringToInt(tdata["data"].join())/ 10
						displayInfoLog("REPORTING TEMP CORRECTION:${temperatureCorr}")
					break

					case '6800': //window open detection
						String WinTemp = HexUtils.hexStringToInt(tdata["data"][7])
						String WinMin  = HexUtils.hexStringToInt(tdata["data"][8])
						displayDebugLog("TEMP DECREASE OF ${WinTemp}C° in ${WinMin}' WILL TRIGGER OFF MODE")
						sendEvent(name: "WindowOpenDetection", value: "${WinTemp}C° in ${WinMin} minutes")
					break
								
					case '6902': //boost -- Dev
						String values = data.collect{c -> HexUtils.hexStringToInt(c)}
						displayDebugLog("${device.displayName} boost ${values}")
					break

					case '7000': // schedule setting aka Auto mode -- Dev
						displayDebugLog("${device.displayName} schedual P1 ${data[6]}:${data[7]} = ${data[8]}deg , ${data[9]}:${data[10]} = ${data[11]}deg ,more ${data} ")
					break

					case '7001': // schedule setting aka Auto mode -- Dev
						displayDebugLog("${device.displayName} schedual P2 ${data[6]}:${data[7]} = ${data[8]}deg , ${data[9]}:${data[10]} = ${data[11]}deg ,more ${data} ")
					break

                    case '0701': // Child lock
						String locked = HexUtils.hexStringToInt(data[6])
						displayDebugLog("${device.displayName} child lock ${commandType}, raw=${msgMap} data=${data} ,dec=${mode}")
						switch (locked){
							case '0':
								sendEvent(name: "childLock", value: "off" )
							break
							case '1':
								sendEvent(name: "childLock", value: "on")
							break
						}
					break

					case '7401': // auto lock setting A3
						String autolock = HexUtils.hexStringToInt(data[6])
						switch (autolock){
							case '0':
								displayDebugLog("${device.displayName} Auto lock A3 Off")
								sendEvent(name: "autolock", value: "off")
							break
							case '1':
								displayDebugLog("${device.displayName} Auto lock A3 On ather 10min")
								sendEvent(name: "autolock", value: "on")
							break
						}
					break

					default:
						String values = data.collect{c -> HexUtils.hexStringToInt(c)}
                        displayDebugLog("[2] Cluster ${cluster} - Unknown attribute - ${msgMap}")      
					break
				}
            }
        }
    }
    else if (cluster == "0000"){
        switch (msgMap["attrId"]) {
            case "0001":
                displayDebugLog("Application ID Received")
                if(msgMap['value']) {
                    updateDataValue("application", msgMap['value'])
                }
                break
            case "0004":
                displayDebugLog("Manufacturer Name Received ${msgMap['value']}")
                if(msgMap['value']) {
                    updateDataValue("manufacturer", msgMap['value'])
                }
                break
            case "0005":
                displayDebugLog("Model Name Received")
                if(msgMap['value']) {
                    updateDataValue('model', msgMap['value'])
                }    
                break
            default:
                displayDebugLog("[3] Cluster ${cluster} - Unknown attribute - ${msgMap}")      
                break
        }
    }
    else if (cluster == "0001"){
    	// battery report
        switch (msgMap["attrId"]) {
            case "0020":
                displayDebugLog("0001 -BATTERY REPORT - VOLTS: ${msgMap}")      
            case "0021":
                displayDebugLog("0001 - BATTERY REPORT - PERCENTAGE: ${msgMap}")      
        }
    }
    else if (cluster == "0004"){
    	// ?
    }
    else if (cluster == "0005"){
        // ?
    }
    else if (cluster == "0006"){
        // ?
    }
    else {
        displayDebugLog("[4] Cluster ${msgMap.cluster} - Unknown cluster - ${msgMap}")
    }
}

// 
private tuyaMap(data){
    def m=[:]
    m["status"]=data[0]
    m["transid"]=data[1]
    m["dp"]=data[3]+data[2]
    m["fn"]=data[4]
    m["data"]=data[6..6+data[5].toInteger()-1]
    return m
}

//================================================
/*
HE raw zigbee frame (for the command)
List cmds = ["he raw 0x${device.deviceNetworkId} 1 1 0x0501 {09 01 00 04}"]
	
he raw                      command
0x${device.deviceNetworkId} 16 bit hex address 
1							source endpoint, always one				 
1 							destination endpoint, device dependent
0x0501 						zigbee cluster id
{09 						frame control
	01 						sequence, always 01
		00 					command
			04}				command parameter(s)
*/

//
private sendTuyaCommand(dp, fn, data) { 
	zigbee.command(CLUSTER_TUYA, SETDATA, "00" + zigbee.convertToHexString(rand(256), 2) + dp + fn + data)
}

private getCLUSTER_TUYA() { 0xEF00 }
private getSETDATA() { 0x00 }

//
private rand(n) {
	return (new Random().nextInt(n))
} 

//
def off() {
    def dp = "0404"
    def fn = "0001"
    def data = "00" // off
	displayDebugLog("Off command, this is away setting heat point ${dp}${fn}${data}")
    sendTuyaCommand(dp,fn,data)
}

//
def heat() {
    updateDataValue("lastRunningMode", "heat")
    def dp = "0404"
    def fn = "0001"
    def data = "02" // on
	displayDebugLog("On/heat/manual command ${zigbee.convertToHexString(rand(256), 2)}${dp}${fn}${data}")
    sendTuyaCommand(dp,fn,data)
}

//
def auto() {
    updateDataValue("lastRunningMode", "heat")
    def dp = "0404"
    def fn = "0001"
    def data = "01" // auto mode, internal schedule
	displayDebugLog("auto mode, internal schedule command ${zigbee.convertToHexString(rand(256), 2)}${dp}${fn}${data}")
	sendTuyaCommand(dp,fn,data)
}

//
def setHeatingSetpoint(preciseDegrees) {
    if (preciseDegrees != null) {
        def dp = "0202"
        def fn = "00"
        def SP = preciseDegrees *10
        def data = "04000000" + zigbee.convertToHexString(SP.intValue(), 2)
	    displayDebugLog("heating ${dp}${fn}${data}")
	    sendTuyaCommand(dp,fn,data)
	}
}

//
def setThermostatMode(String value) {
    switch (value) {
        case "heat":
        case "emergency heat":
            return heat()
        case "eco":
        case "cool":
            return eco()
        case "auto":
            return auto()
        default:
            return off()
    }
}

//
def eco() {
    updateDataValue("lastRunningMode", "cool")
    displayDebugLog("eco mode is not available for this device. => Defaulting to off mode instead.")
    off()
}

//
def cool() {
    displayDebugLog("cool mode is not available for this device. => Defaulting to eco mode instead.")
    eco()
}

//
def emergencyHeat() {
    displayDebugLog("emergencyHeat mode is not available for this device. => Defaulting to heat mode instead.")
	heat()
}

//
def setCoolingSetpoint(degrees) {
    displayDebugLog("setCoolingSetpoint is not available for this device")
}

//
def fanAuto() {
    displayDebugLog("fanAuto mode is not available for this device")
}

//
def fanCirculate(){
    displayDebugLog("fanCirculate mode is not available for this device")
}

//
def fanOn(){
    displayDebugLog("fanOn mode is not available for this device")
}

//
def setSchedule(JSON_OBJECT){
    displayDebugLog("setSchedule is not available for this device")
}

//
def setThermostatFanMode(fanmode){
    displayDebugLog("setThermostatFanMode is not available for this device")
}

//
void parseAndSendBatteryStatus(BigDecimal vCurrent) {
    BigDecimal vMin = vMinSetting == null ? 2.5 : vMinSetting
    BigDecimal vMax = vMaxSetting == null ? 3.0 : vMaxSetting
    
    BigDecimal bat = 0
    if(vMax - vMin > 0) {
        bat = ((vCurrent - vMin) / (vMax - vMin)) * 100.0
    } else {
        bat = 100
    }
    bat = bat.setScale(0, BigDecimal.ROUND_HALF_UP)
    bat = bat > 100 ? 100 : bat
    
    vCurrent = vCurrent.setScale(3, BigDecimal.ROUND_HALF_UP)

    logging("Battery event: $bat% (V = $vCurrent)", 1)
    sendEvent(name:"battery", value: bat, unit: "%", isStateChange: false)
}

//Reset the batteryLastReplaced date to current date
def resetBatteryReplacedDate(paired) {
	displayInfoLog("SETTING BATTERY LAST REPLACE DATE")
	sendEvent(name: "batteryLastReplaced", value: new Date().format("dd/mm/yyyy hh:mm:ss a", location.timeZone))
    }

// Reverses order of bytes in hex string
def reverseHexString(hexString) {
	def reversed = ""
	for (int i = hexString.length(); i > 0; i -= 2) {
		reversed += hexString.substring(i - 2, i )
	}
	return reversed
}

//
private def displayInfoLog(message) {
    if(infoLogging)
        log.info "${device.displayName}: ${message}"
}

//
private def displayDebugLog(message) {
	if (debugLogging) 
        log.debug "${device.displayName}: ${message}"
}

//============================
// MARKUS
//============================

//
ArrayList<String> refresh() {    
    getDriverVersion()
    configurePresence()
    //startCheckEventInterval()
    resetBatteryReplacedDate(forced=false)
    setLogsOffTask(noLogWarning=true)
    setCleanModelName(newModelToSet=null, acceptedModels=["TS0601"])
    ArrayList<String> cmd = []
    return cmd
}

/*
//
void refresh(String cmd) {
    deviceCommand(cmd)
}
*/
                      
//
def initialize() {
    logging("initialize()", 100)
    unschedule()
    refresh()
    configureDevice()
}

//
void installed() {
    logging("installed()", 100)
    refresh()
    configureDevice()
}

//
void updated() {
    logging("updated()", 100)
    refresh()
    configureDevice()
}

//
void prepareCounters() {
    if(device.currentValue('restoredCounter') == null) sendEvent(name: "restoredCounter", value: 0, descriptionText: "Initialized to 0" )
    if(device.currentValue('notPresentCounter') == null) sendEvent(name: "notPresentCounter", value: 0, descriptionText: "Initialized to 0" )
    if(device.currentValue('presence') == null) sendEvent(name: "presence", value: "unknown", descriptionText: "Initialized as Unknown" )
}

//
Integer getMINUTES_BETWEEN_EVENTS() {
    return 140
}

//
void configurePresence() {
    prepareCounters()
    if(presenceEnable == null || presenceEnable == true) {
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 1/3 * * ? *", 'checkPresence')
        checkPresence(false)
    } else {
        sendEvent(name: "presence", value: "not present", descriptionText: "Presence Checking Disabled" )
        unschedule('checkPresence')
    }
}

//
boolean checkPresence(boolean displayWarnings=true) {
    boolean isPresent = false
    Long lastCheckinTime = null
    String lastCheckinVal = device.currentValue('lastCheckin')
    //if ((lastCheckinEnable == true || lastCheckinEnable == null) && isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == true) {
    if ((lastCheckinEnable == true || lastCheckinEnable == null) && isValidDate(lastCheckinVal) == true) {
        lastCheckinTime = Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime()
    } else if (lastCheckinEpochEnable == true && device.currentValue('lastCheckinEpoch') != null) {
        lastCheckinTime = device.currentValue('lastCheckinEpoch').toLong()
    }
    if(lastCheckinTime != null && lastCheckinTime >= now() - (3 * 60 * 60 * 1000)) {
        setAsPresent()
        isPresent = true
    } else {
        sendEvent(name: "presence", value: "not present")
        if(displayWarnings == true) {
            Integer numNotPresent = device.currentValue('notPresentCounter')
            numNotPresent = numNotPresent == null ? 1 : numNotPresent + 1
            sendEvent(name: "notPresentCounter", value: numNotPresent )
            if(presenceWarningEnable == null || presenceWarningEnable == true) {
                log.warn("No event seen from the device for over 3 hours! Something is not right... (consecutive events: $numNotPresent)")
            }
        }
    }
    return isPresent
}

void checkEventInterval(boolean displayWarnings=true) {
    logging("recoveryMode: $recoveryMode", 1)
    if(recoveryMode == "Disabled") {
        unschedule('checkEventInterval')
    } else {
        prepareCounters()
        Integer mbe = getMaximumMinutesBetweenEvents()
        try {
            if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe) == false) {
                recoveryMode = recoveryMode == null ? "Normal" : recoveryMode
                if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("Event interval INCORRECT, recovery mode ($recoveryMode) ACTIVE! If this is shown every hour for the same device and doesn't go away after three times, the device has probably fallen off and require a quick press of the reset button or possibly even re-pairing. It MAY also return within 24 hours, so patience MIGHT pay off.")
                scheduleRecoveryEvent()
            }
        } catch(Exception e) {
            disableRecoveryDueToBug()
        }
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
}
    
//
void startCheckEventInterval() {
    if(recoveryMode != "Disabled") {
        logging("Recovery feature ENABLED", 100)
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)}/59 * * * ? *", 'checkEventInterval')
        checkEventInterval(displayWarnings=true)
    } else {
        logging("Recovery feature DISABLED", 100)
        unschedule('checkEventInterval')
        unschedule('recoveryEvent')
        unschedule('reconnectEvent')
    }
}

//
Long secondsSinceLastCheckinEvent() {
    Long r = null
    if (lastCheckinEnable == true || lastCheckinEnable == null) {
        String lastCheckinVal = device.currentValue('lastCheckin')
        if(lastCheckinVal == null || isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == false) {
            logging("No VALID lastCheckin event available! This should be resolved by itself within 1 or 2 hours and is perfectly NORMAL as long as the same device don't get this multiple times per day...", 100)
            r = -1
        } else {
            r = (now() - Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime()) / 1000
        }
	}
    if (lastCheckinEpochEnable == true) {
		if(device.currentValue('lastCheckinEpoch') == null) {
		    logging("No VALID lastCheckin event available! This should be resolved by itself within 1 or 2 hours and is perfectly NORMAL as long as the same device don't get this multiple times per day...", 100)
            r = r == null ? -1 : r
        } else {
            r = (now() - device.currentValue('lastCheckinEpoch').toLong()) / 1000
        }
	}
    return r
}

//
boolean hasCorrectCheckinEvents(Integer maximumMinutesBetweenEvents=90, boolean displayWarnings=true) {
    Long secondsSinceLastCheckin = secondsSinceLastCheckinEvent()
    if(secondsSinceLastCheckin != null && secondsSinceLastCheckin > maximumMinutesBetweenEvents * 60) {
        if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("One or several EXPECTED checkin events have been missed! Something MIGHT be wrong with the mesh for this device. Minutes since last checkin: ${Math.round(secondsSinceLastCheckin / 60)} (maximum expected $maximumMinutesBetweenEvents)")
        return false
    }
    return true
}

//
void setAsPresent() {
    if(device.currentValue('presence') == "not present") {
        Integer numRestored = device.currentValue('restoredCounter')
        numRestored = numRestored == null ? 1 : numRestored + 1
        sendEvent(name: "restoredCounter", value: numRestored )
        sendEvent(name: "notPresentCounter", value: 0 )
    }
    sendEvent(name: "presence", value: "present")
}

void resetNotPresentCounter() {
    logging("resetNotPresentCounter()", 100)
    sendEvent(name: "notPresentCounter", value: 0, descriptionText: "Reset notPresentCounter to 0" )
}

void resetRestoredCounter() {
    logging("resetRestoredCounter()", 100)
    sendEvent(name: "restoredCounter", value: 0, descriptionText: "Reset restoredCounter to 0" )
}

String styling_addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

String styling_addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

String styling_makeTextBold(s) {
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

String styling_makeTextItalic(s) {
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

String styling_getDefaultCSS(boolean includeTags=true) {
    String defaultCSS = '''
    /* This is part of the CSS for replacing a Command Title */
    div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell p::after {
        visibility: visible;
        position: absolute;
        left: 50%;
        transform: translate(-50%, 0%);
        width: calc(100% - 20px);
        padding-left: 5px;
        padding-right: 5px;
        margin-top: 0px;
    }
    /* This is general CSS Styling for the Driver page */
    h3, h4, .property-label {
        font-weight: bold;
    }
    .preference-title {
        font-weight: bold;
    }
    .preference-description {
        font-style: italic;
    }
    
    '''
    if(includeTags == true) {
        return "<style>$defaultCSS </style>"
    } else {
        return defaultCSS
    }
}

//
private boolean logging(message, level) {
    boolean didLogging = false
     
    Integer logLevelLocal = 0
    if (infoLogging == null || infoLogging == true) {
        logLevelLocal = 100
    }
    if (debugLogging == true) {
        logLevelLocal = 1
    }
     
    if (logLevelLocal != 0){
        switch (logLevelLocal) {
        case 1:  
            if (level >= 1 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case 100:  
            if (level == 100 ) {
                log.info "$message"
                didLogging = true
            }
        break
        }
    }
    return didLogging
}

//============================
//============================

//
void configure() {
    Integer endpointId = 1
    ArrayList<String> cmds = []

    displayDebugLog("Configuration starting")

    sendEvent(name: "supportedThermostatModes", value: ["off", "heat", "auto"] )
    
    cmds=[
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0000 {${device.zigbeeId}} {}", 	// ?
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0001 {${device.zigbeeId}} {}", 	// POWER CONFIGURATION
        "zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0004 {${device.zigbeeId}} {}", 	// ?
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0005 {${device.zigbeeId}} {}", 	// ?
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0x0006 {${device.zigbeeId}} {}", 	// ?
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 0x01 0xefOO {${device.zigbeeId}} {}" 	// TUYA CLUSTER FOR PROPRIETARY COMMANDS
   ]
    
    //CONFIGURE REPORTS
    cmds += zigbee.configureReporting(0x0001, 0x0020, 0x20, 3509, 3600, null, [:], 100) //BatteryVoltage
    cmds += zigbee.configureReporting(0x0001, 0x0021, 0x20, 3509, 3600, null, [:], 100) //BatteryPercentageRemaining
    //READ ATTRIBUTE

    sendZigbeeCommands(cmds)
}

void sendZigbeeCommand(String cmd) {
    logging("sendZigbeeCommand($cmd)", 1)
    sendZigbeeCommands([cmd])
}

void sendZigbeeCommands(ArrayList<String> cmd) {
    displayDebugLog("sendZigbeeCommands($cmd)")
    hubitat.device.HubMultiAction allActions = new hubitat.device.HubMultiAction()
    cmd.each {
            allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
    }
    sendHubCommand(allActions)
}
