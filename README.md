# EduroamCAT- Android App
A tool to allow users to configure their device for eduroam wireless networks. This tool requires a configuration file from your home institution in order acquire the configuration settings needed. These can also be discovered by the tool, if the institution has CAT configured.
Due to limitations in the Android OS, the application needs to set up a screen lock if none is already set. 
The configuration file is in a standardised file format and can be obtained from eduroam Configuration Assistant Tool deployments (such as https://cat.eduroam.org and others). 
The tool also provides some status information on the eduroam connection.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=uk.ac.swansea.eduroamcat)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/uk.ac.swansea.eduroamcat/)

# Permissions needed by the app

The application requests only the bare minimum of permissions needed to fulfill its purpose: to configure the Wi-Fi interface of Android devices, and to verify that the connection has been established correctly. The following permissions are needed:

1. Location - Access Approximate Location (network-based)

   eduroam configuration details vary by participating organisation. The application needs to know which organisation a user belongs to to install the correct ones. One of the means to find out is by comparing the device location to the location of participating organisations, and then suggesting nearby organisations on top of a list.
   
2. Photos/Media/Files - Read the Contents of your SD Card

    The application reads the user selected config file from external storage.

3. Photos/Media/Files - Modify or Delete the Contents of your SD Card

   The application saves the organisation that was selected by the user and the associated configuration details. It also retrieves those from file storage on startup.
   
4. Other - Connect and Disconnect from Wi-Fi

   It is the core purpose of the application to do just that.
   
5. Other - Have Full Network Access

   The application performs basic diagnosis on the connection while it is being established. For example, it finds out whether or not a IP address has been handed out after a successful authentication. Also, in the course of organisation discovery, it needs to retrieve the list of organisations and the pertinent configuration details from a server on the internet.
