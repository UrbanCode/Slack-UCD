# IBM UrbanCode Deploy Slack Plugin [![Build Status](https://travis-ci.org/IBM-UrbanCode/Slack-UCD.svg?branch=master)](https://travis-ci.org/IBM-UrbanCode/Slack-UCD)
---
Note: This is not the plugin distributable! This is the source code. To find the installable plugin, go into the 'Releases' tab, and download a stable version.

### License
This plugin is protected under the [Eclipse Public 1.0 License](http://www.eclipse.org/legal/epl-v10.html)

### Compatibility
	The IBM UrbanCode Deploy automation plugin uses Slack's Web API.
	This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy.

### Installation
	The packaged zip is located in the releases folder. No special steps are required for installation.
	See Installing plug-ins in UrbanCode Deploy. Download this zip file if you wish to skip the
	manual build step. Otherwise, download the entire Slack-UCD and
	run the "gradlew build" command in the top level folder. This should compile the code and create
	a new distributable zip within the build/distributions folder. Use this command if you wish to make
	your own changes to the plugin.

### History
    Version 5
        - Change from Ant/Ivy to Gradle build tool.
        - Reorganized folder structure.
        - Fix z/OS IBM-1047 encoding error.
        - Removed unnecessary jars.
    Version 4
        - Send Slack Attachment message to multiple channels.
        - Changed the Post Custom Notification to Slack step name to Post Attachment Notification to Slack.
        - The Attachment Payload must now follow the exact JSON paylod for normal Attachment messages.
        - Various exception handling.
    Version 3
        - New Custom Slack Step
        - Added timestamp
        - Removed Command Helper
        - Fixed issues with the JSON that was built
        - Major build.xml revisions
        - Upgrade groovy-plugin-utils to v1.2

    Version 2.1
        - Description Updates
        - Added invalid Exception Variable

    Version 2
        - Community GitHub Release

### How to build the plugin from Eclipse client:

1. Expand the Groovy project that you checked-out from example template.
2. Open the `build.gradle` file and execute it as a Gradle Build operation (Run As -> Gradle Build)
3. The built plugin is located at `build/distributions/Slack-UCD-vdev.zip`

### How to build the plugin from command line:

1. Navigate to the base folder of the project through the command line.
2. Make sure that the `build.gradle` file is there, then execute `gradlew build`
    * To create a release version, append `-PpluginVersion=VERSION` where `VERSION` is the version you want (e.g. `5.0`)
3. The built plugin is located at `build/distributions/Slack-UCD-vdev.zip`
    * If you added the `PpluginVersion` flag, the file will have your version at the end (e.g. `Slack-UCD-v5.0.zip`)