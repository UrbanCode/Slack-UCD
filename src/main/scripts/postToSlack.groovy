/**
 * © Copyright IBM Corporation 2014, 2016.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 * @author cooperc
 */
import com.urbancode.air.AirPluginTool;
import com.urbancode.air.CommandHelper;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

final def workDir = new File('.').canonicalFile;
final def props = new Properties();
final def inputPropsFile = new File(args[0]);
try {
    inputPropsStream = new FileInputStream(inputPropsFile);
    props.load(inputPropsStream);
}
catch (IOException e) {
    throw new RuntimeException(e);
}

// properties
final def webhook = props['webhook'];
final def slackChannel = props['channel'];
final def colour = props['colour'];
final def emoji = props['emoji'];
final def environment = props['environment'];
final def component = props['component'];
final def version = props['version'];

def commandHelper = new CommandHelper(workDir);

// Setup path
try {
	def curPath = System.getenv("PATH");
	def pluginHome = new File(System.getenv("PLUGIN_HOME"))
	println "Setup of path using plugin home: " + pluginHome;
	def binDir = new File(pluginHome, "bin")
	def newPath = curPath+":"+binDir.absolutePath;
	commandHelper.addEnvironmentVariable("PATH", newPath);
} catch(Exception e){
	println "ERROR setting path: ${e.message}";
	System.exit(1);
}

// JSON message composition
def json = new JsonBuilder();
try {
	def jsonField = new JsonBuilder();
	jsonField {
		'title' 'Environment'
		value environment
		'short' 'true'
	}
	def jsonValue = new JsonBuilder();
	jsonValue {
		'title' 'Version'
		value  version
		'short' 'true'
	}
	json {
		channel slackChannel
		text component
		color colour
		fields  jsonField.content,
				jsonValue.content
		icon_emoji emoji
		username 'UrbanCode Deploy'
	}
	println json.toPrettyString();
} catch (Exception exception) {
	println "ERROR setting path: ${e.message}"
	System.exit(1)
}


println "DEBUG:: POST to Slack: " + webhook
def http = new HTTPBuilder( webhook )
http.ignoreSSLIssues()
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
