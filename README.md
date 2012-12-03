openas2
=======

AS/2 Client and Server, forked from the open source Mendelson AS/2 project.

objectives
=======

Mendelson created a great open source AS/2 solution [(here)](http://as2.mendelson-e-c.com/ "Mendelson AS2") under GPL.  This project aims to build upon that solution with the following objectives:

* Projectize the open source solution.  The Java source is kept in a CVS repo on SourceForge, but the accompanying resources like third party libs, SQL, and web assets are only available by downloading the install binary from another site.  Further, there is no project or build file pulling it all together.  So the first goal is to sync source and dependencies, create a project build, and centralize the entire project on github so that new contributors can easily get up and running with the project.
* Separate client and server.  Enable as2 to run as a server only, without starting up a Swing UI.  The server will still need to provide configuration and reporting functionality.
* Extend client capabilities so that as2.jar can be included as a third party lib in other projects to send messages.
* Further extend the client so that it can be called directly from script, with no UI or configuration prerequisite.
* Abstract the database layer so that users can plugin other datastores such as Oracle, Mongo, and file.

features
=======

* Logging- and configuration GUI (SWING)
* AS2 1.2 protocol standard
* Async & sync MDN
* Signatures
* SSL
* Web interface
* Encryption
* Pluggable into any servlet container
* Data compression
* Message post processing (scripting on receipt, sent)
* Email event notification
* Optional AS2 profile MA (Multiple Attachments)
* Optional AS2 profile FN (Filename Preservation)