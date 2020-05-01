/**
 * ? Copyright IBM Corporation 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
import com.urbancode.air.AirPluginTool;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HostConfiguration
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity

final def airTool = new AirPluginTool(args[0], args[1])

/* Here we call getStepProperties() to get a Properties object that contains the step properties
* provided by the user.
*/
final def props = airTool.getStepProperties()

// properties
final def webhook = props['webhook'];
final def slackChannels = props['channels'].split(",|\n")*.trim() - "";
final def slackUsername = props['username'];
final def emoji = props['emoji'];
final def slackAttachment = props['attachment'];
final def proxyHost = props['proxyhost'];
final def proxyPort = props['proxyport'];

slackChannels.each { slackChannel ->
    slackChannel = URLDecoder.decode(slackChannel, "UTF-8" );
    if (!slackChannel.startsWith("@") && !slackChannel.startsWith("#")) {
        throw new RuntimeException("ERROR:: Invalid slack channel format passed: '${slackChannel}'. Must start with either # or @.")
    }
}

//Convert attachment input to be ArrayList for JSONBuilder
def attachmentJson = {}
try {
    attachmentJson = new JsonSlurper().parseText(slackAttachment)
}
catch (Exception e) {
    printSampleAttachmentPayload()
    throw new RuntimeException("ERROR:: Unable to parse the Attachment Payload as JSON. Follow the above sample JSON payload.\n${e.message}")
}

if (!attachmentJson.attachments) {
    printSampleAttachmentPayload()
    throw new RuntimeException("ERROR:: Unable to identify an 'attachments' ID. Follow the above sample JSON payload.")
}

String currentTime = System.currentTimeMillis()/1000
attachmentJson.attachments.each { attachment ->
    if (!attachment.ts) {
        attachment.ts = currentTime
    }
}

int countFails = 0
slackChannels.each { slackChannel ->
    // JSON message composition
    def json = new JsonBuilder();
    try {
        json {
            channel slackChannel
            username slackUsername
            icon_emoji emoji
            attachments attachmentJson.attachments
        }
        println "DEBUG:: JSON Payload"
        println json.toPrettyString();
    } catch (Exception exception) {
        throw new Exception("ERROR:: setting path: ${exception.message}")
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
            println "${status} Success at '${slackChannel}'";
        } else {
            println "${status} Failure at '${slackChannel}'"
            countFails++
        }
    } catch (Exception e) {
        println "ERROR:: Unable to set path: ${e.message}"
        println "[Possible Solution] Confirm the properties by running the Webhook with its associated JSON body in a REST Client."
        System.exit(2)
    }
}
if (countFails > 0) {
    println "ERROR:: One of the messages failed to send. View the above logs to determine the source."
    System.exit(1)
}
void printSampleAttachmentPayload() {
    println "==== Sample Attachment JSON ===="
    println "{"
    println "   \"attachments\": ["
    println "       {"
    println "           \"title\": \"IBM UrbanCode Developer Community\","
    println "           \"title_link\": \"https://developer.ibm.com/urbancode/plugins/development-community/\","
    println "           \"text\": \"Learn about developing community plugins!\","
    println "           \"color\": \"#36a64f\","
    println "           \"footer\": \"Slack API\""
    println "       }"
    println "   ]"
    println "}"
    println "================================"
}
