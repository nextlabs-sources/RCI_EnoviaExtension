####################################################################################
#		Nextlabs Enovia Extensions - RCI Custom
####################################################################################

Requirements:
- NextLabs Control Center 6.0
- Enovia V62009
- NextLabs Enovia EM V1.0


Install:
1. Copy nextlabs-enovia-em-extension.jar from <RCI-Enovia-Extensions> folder to <Enovia>/server/java/custom folder.
2. Copy classification_conn.properties from <RCI-Enovia-Extensions>/conf folder to <Enovia>/server/java/custom/nextlabs/conf folder.
3. Copy log4j_rciext.properties from <RCI-Enovia-Extensions>/conf folder to <Enovia>/server/java/custom/nextlabs/conf folder.
4. You may modify the classification_conn.properties to manage the connection to repository.
5. You may modify the log4j_rciext.properties to manage the message logging.
6. runtimeconfig.xml in <RCI-Enovia-Extensions>/conf demonstrates how to configure extension for the specific object type.


Folder structure of Nextlabs Enovia Extensions - RCI Custom:
RCI-Enovia-Extensions
	|- conf
		|- classification_conn.properties
		|- log4j_rciext.properties
		|- runtimeconfig.xml
	|- nextlabs-enovia-em-extension.jar
	|- readme.txt