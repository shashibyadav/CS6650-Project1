Author :- Shashi Bhushan Yadav
Course :- CS6650

This zip contains all the files for the project including Executable jars in folder named "Executables". There are two executables Server.jar and Client.jar for the project.

How to run Server Executable :-
	1.	For TCP protocol only :- 
		-> java -jar Server.jar TCP <server-host> <TCP-port>
	2.	For UDP protocol only :-
		-> java -jar Server.jar UDP <server-host> <UDP-port>
	3.	For TCP and UDP protocols concurrently :- 
		-> java -jar Server.jar All <server-host> <TCP-port> <UDP-port>
		
		Ex. java -jar Server.jar ALL localhost 40000 40001

How to run Client Executable :- 
	1.	For TCP protocol :- 
		-> java -jar Client.jar TCP <server-host> <TCP-server-port>
	2.	For UDP protocol :-
		-> java -jar Client.jar UDP <server-host> <UDP-server-port>

		Ex. java -jar Client.jar TCP localhost 40000 

--------------------------------------------------------------------
Entry point Java files are located in src/main/java/com/project/one

How to run Server using ServerDebug.java :-
	->	javac ServerDebug.java

	1.	For TCP protocol only :- 
		-> java ServerDebug TCP <server-host> <TCP-port>
	2.	For UDP protocol only :-
		-> java ServerDebug UDP <server-host> <UDP-port>
	3.	For TCP and UDP protocols concurrently :- 
		-> java ServerDebug All <server-hosts> <TCP-port> <UDP-port>

How to run Client using ClientDebug.java :- 
	->	javac ClientDebug.java

	1.	For TCP protocol :- 
		-> java ClientDebug TCP <server-host> <TCP-server-port>
	2.	For UDP protocol :-
		-> java ClientDebug UDP <server-host> <UDP-server-port>
--------------------------------------------------------------------

Screenshots of the terminal Output are present in folder named Screenshots

--------------------------------------------------------------------

Summary of working :- 

After starting the server first using any of the start command start the client corresponding to the protocol. 
Client is written to test dummy data when it starts it will put 5 key-value pairs then fetch them. 
After printing those five fetched values client will ask for deletion of these five keys and fetch them from server which will result in values shown as null as client didn't receive any data for corresponding keys from Server.
After this client becomes interactive and user can perform PUT, GET, DELETE operations.
User can also exit the client or can ask server and client both to exit depending upon the selection.
Also, Server creates a log file starting with the name "Server_log_*" that can be used to asses server health or status.

---------------------------------------------------------------------

Executive Summary :- 

By working of this project I understood about the workings of TCP and UDP protocols and how to build a server in multi-threaded environment. 
My implementation of Server spawns at most 6 threads one for cleanup and one main thread, other than that 2 threads are assigned for each TCP and UDP protocols for request processing. 
I understood how client and server communicate and what kind errors or issues can arise in multiple clients (Ex. two different clients accessing common store concurrently). 
There is a lot of improvement that can be performed in my implementation but I guess for now it satisfies the requirements of the project. 
I enjoyed working on it and it got me chance to get a refresher on Java based Client-Server programming. 