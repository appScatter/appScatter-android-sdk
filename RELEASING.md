###After making changes to any module release all of the modules to keep consistent versioning:

1. Increase the version number
2. Go to AppScatterSDKPlugin.groovy class and update the sdk-aspects dependency and sdk dependency according
to the new version number
3. Release all of the modules:
    * to Nexus
        **./gradlew clean build publish -PmavenUser={username} -PmavenPassword={password}**

    * to your Local Maven
        **./gradlew clean build publishToMavenLocal**
    
    * to jCenter
        **./gradlew clean build bintrayUpload -PbintrayApiKey={bintrayApiKey} -PbintrayGPGPassphrase={bintrayGpgPassphrase}**


