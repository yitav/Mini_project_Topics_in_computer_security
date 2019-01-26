# Mini_project_Topics_in_computer_security

This mini project is the implementation of a cryptographically secure file download and upload client and server. 


Readme File - client

The client application is written in Java language and was written and tested on
linux x64 Ubuntu 16.04 . In order for it to run on a desktop java version 8 should be installed
on the desktop – i.e. jdk 8 or jre 8 should be installed.


Readme File - server

language of server is: node js version 9.8.0
operating system which it was developed on linux x64 Ubuntu 16.04

Server resources and how to use them:
each of the following resources except the ones used for validation and except
'/isLastUploadCompleted' returns "success" on success or "failed" on failure.
'/isLastUploadCompleted' has in addition a third state of response which is "pending"
which symbolizes an ongoing upload.
another special case with responses is '/ viewAvailableFiles' which return a json string –
details below.
furthermore every client is assigned a root directory for his own use.
a more detailed description :
The first two routes of the next ones presented are used for validation purposes which should be done
at the client side.
1) Check connection through get request :
http://<ip>:<port>/isConnection
on success returns "success" otherwise unknown
2) Check what is the server's socket port through get request :
http://<ip>:<port>/getSocketPort
on success returns <socket-port-NUMBER> otherwise unknown
The next routes:* Register through get request
http://<ip>:<port>/register?username=<username>&password=<password>
* login through get request :
http://<ip>:<port>/login?username=<username>&password=<password>
* logout through get request :
http://<ip>:<port>/logout
* upload through get request :
http://<ip>:<port>/upload?filenamePath=<filenamePath>&size=<size>
notes :
1) upload file will be stored in server under root's client directory unless change directory was
called and then it will be stored in the changed directory
2) after the server returns "success" then the client can connect to the server's socket to upload
the file.
3)The client needs to send via socket first 0 in one byte size if it is an upload ,second the size
of the name of the file sent in 4 bytes length ,Endianess is Big Endian ,
and afterwards the name of the file in bytes encoded in utf8.
After sending the size of the name and the name , the sending of the file itself should occur
in bytes of course.
* Check if an upload completed successfully through get request :
http://<ip>:<port>/isLastUploadCompleted?filenamePath=<filenamePath>
* change directory through get request :
http://<ip>:<port>/changeDirPath?path=<path>
note : both start and end of path should be / i.e. path:=/string
* download through get request
http://<ip>:<port>/download?filename=<filename>
notes :
1) before each download the client needs to change to its wanted directory allocated to it on
the server , which on there the requested file is on
2) after the client makes request which result in "success" he should open a socket and
connect to the server .
3) after a connection is made in resemblance to upload the client needs the send via socket
first 1 in one byte size if it is a download ,second the size of the name of the file sent in 4
bytes length, Endianess is Big Endian ,
and afterwards the name of the file in bytes encoded in utf8.
After that the client needs to read from the socket the requested file.
* view available files through get request :
http://<ip>:<port>/viewAvailableFiles
notes :
1) as stated above answer is in the form of a json string ,
its structure is : {response:<status>, data:<files-tree>},
where status = "success" or "failed" ,
and filetree structure is {path:<path> , name:<name>,children : [filetree,...,filetree]}
where the empty array symbolizes no children.* Rename file through get request :
http://<ip>:<port>/renameFile?oldFilePath=<oldFilePath>&newFilePath=<newFilePath>
* Delete file through get request :
http://<ip>:<port>/deleteFile?filePath=<filePath>
* Delete directory through get request :
http://<ip>:<port>/deleteDir?dirPath=<directoryPath>
note: necessary condition for deleting directory is it will not contain any other file or directory
* Create directory through get request :
http://<ip>:<port>/createDir?dirPath=<directoryPath>






