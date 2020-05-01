/**
 * © Copyright IBM Corporation 2014, 2016.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 * @author cooperc
 */
import com.urbancode.air.AirPluginTool;
import com.urbancode.air.CommandHelper;

import groovy.json.JsonBuilder

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HostConfiguration
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity

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
final def proxyHost = props['proxyhost'];
final def proxyPort = props['proxyport'];

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

// HTTP POST to Slack
try{
	def requestEntity = new StringRequestEntity(
			json.toString(),
			"application/json",
			"UTF-8"
	);
	def http = new HttpClient();

	// Set proxy if set
	if(proxyHost != null && proxyHost.length() > 1) {

		try {
			def HostConfiguration hostConfiguration = http.getHostConfiguration();
			hostConfiguration.setProxy(proxyHost, proxyPort.toInteger());
			http.setHostConfiguration(hostConfiguration);
		} catch(Exception e) {
			println "[Error] Unable to set proxy: ${e.message}"
    		println "[Possible Solution] Verify the proxyHost and proxyPort parameters. host=${proxyHost}, port=${proxyPort}"
		}
		
	}

	def post = new PostMethod(webhook);
	post.setRequestEntity(requestEntity);

	def status = http.executeMethod(post);

	if (status == 200){
		println "Success: ${status}";
		System.exit(0);;
	} else {
		println "Failure: ${status}"
		System.exit(3);
	}
} catch (Exception e) {
	println "[Error] Unable to set path: ${e.message}"
    println "[Possible Solution] Confirm the properties by running the Webhook with its associated JSON body in a REST Client."
	System.exit(2)
}
