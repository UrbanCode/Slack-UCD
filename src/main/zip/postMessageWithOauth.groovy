/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Deploy
 * (c) Copyright IBM Corporation 2011, 2014. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */
 /* This is an example step groovy to show the proper use of APTool
  * In order to use import these utilities, you have to use the "pluginutilscripts" jar
  * that comes bundled with this plugin example.
  */
 import com.urbancode.air.AirPluginTool
 
 def apTool = new AirPluginTool(this.args[0], this.args[1])
 def props = apTool.getStepProperties()
 
 def token = props['token']
 def channel = props['channel']
 def text = props['text']
 def username = props['username']
 def as_user = props['as_user']
 def helper = new SlackRestHelper()
 
 try {
     println("Executing post message...")
     helper.executePostMethod(token, channel, text, username, as_user)
 }
 catch (Exception ee) {
     throw new Exception("Command failed with message: " + ee.message)
 }