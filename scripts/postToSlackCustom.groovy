/**
 * © Copyright IBM Corporation 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 * @author cooperc
 */
import com.urbancode.air.AirPluginTool;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

final def workDir = new File('.').canonicalFile
final def pluginTool = new AirPluginTool(args[0], args[1])
final def props = pluginTool.stepProperties

final def webhook = props['webhook'];
final def slackChannels = props['channels'].split(",|\n")*.trim() - "";
final def slackUsername = props['username'];
final def emoji = props['emoji'];
final def slackAttachment = props['attachment'];
final def slackProxyHost = props['slackProxyHost'];
final def slackProxyPort = props['slackProxyPort'];

slackChannels.each { slackChannel ->
  slackChannel = URLDecoder.decode(slackChannel, "UTF-8" );
  println "slackChannel: " + slackChannel
  if (!slackChannel.startsWith("@") && !slackChannel.startsWith("#")) {
    throw new RuntimeException("ERROR:: Invalid slack channel format passed: '${slackChannel}'. Must start with either # or @.")
  }
}

slackChannels.each { slackChannel ->
  def slurped = new JsonSlurper().parseText(slackAttachment)
  def attachmentJson = new JsonBuilder(slurped)

  println "attachmentJson.content: " + attachmentJson.content

  attachmentJson.content[0].ts = "" + System.currentTimeMillis()/1000;
  
  def slackChannelDecoded = URLDecoder.decode(slackChannel, "UTF-8" );

  def json = new JsonBuilder();
  try {
    json {
        channel slackChannelDecoded
        username slackUsername
        icon_emoji emoji
        attachments attachmentJson.content
    }
    println "DEBUG:: JSON Payload"
    println json.toPrettyString();
  } 
  catch (Exception exception) {
    println "ERROR:: setting path: ${e.message}"
    System.exit(1)
  }

  println "DEBUG:: POST to Slack: " + webhook
  def http = new HTTPBuilder( webhook )
  http.ignoreSSLIssues()
  if (slackProxyHost != null && !slackProxyHost.isEmpty()) {
  	http.setProxy(slackProxyHost, Integer.valueOf(slackProxyPort), 'http')                     
  }
  http.request (POST, JSON) { req ->
    body = json.toPrettyString()

    response.success = { resp ->
        println "SUCCESS: Message posted to Slack successfully: ${resp.statusLine}"
    }
    response.failure = {  resp ->
        println "ERROR: Failed to post message to Slack: ${resp.statusLine}"
        System.exit(1)
    }
  }
}