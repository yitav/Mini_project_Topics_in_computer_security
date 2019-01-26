var express = require('express');
var app = express();
app.set('port', process.env.PORT || 3000);
var portSocket = process.env.PORTSOCKET || 5000;

var session = require('express-session');
var net = require('net');
var ip = require('ip');
var fs = require('fs');
const dirTree = require('directory-tree');
const crypto = require('crypto');
const sqlite3 = require('sqlite3').verbose();

let db = new sqlite3.Database('./users.db',sqlite3.OPEN_READWRITE, (err) => {
  if (err) {
    console.error(err.message);
    process.exit(1); //if process is not able to connect to db then it can not give service
  }
  console.log('Connected to the users database.');
});
//CREATE TABLE USERS(USERNAME CHAR(150) PRIMARY KEY    NOT NULL , SALT CHAR(150)    NOT NULL , HPASSWORD CHAR(150) NOT NULL);
process.on('exit', function() {
	db.close();
});

app.use(session({//session is used for login purposes(Express Middleware)
  secret: 'keyboard cat'+Math.floor((Math.random()*1000000)), // This is the secret used to sign the session ID cookie
  resave: false,//when set to true - Forces the session to be saved back to the session store, even if the session was never modified during the request
  saveUninitialized: false,//when true - Forces a session that is "uninitialized" to be saved to the store. A session is uninitialized when it is new but not modified
  //cookie: { secure: true } //when true - compliant clients will not send the cookie back to the server in the future if the browser does not have an HTTPS connection
  cookie: { maxAge : 24*60*60*1000 }

}))

app.get('/isConnection',function(req,res){
	res.send("success");
});

app.get('/getSocketPort',function(req,res){
	res.send(""+portSocket);
});


//registering a handler for this http register route
app.get('/register',function(req,res){
	//console.log(req.query)
	console.log(req.query.username);
	console.log(req.query.password);

	var request_username = req.query.username;
	var request_password = req.query.password;

	salt = Math.floor((Math.random()*1000000000));

	var getRegisterPromise = ()=>{
		return new Promise((resolve , reject)=>{

		const tobehashed = ""+salt+request_password; //salting
		const hash = crypto.createHash('sha256');

        hash.update(tobehashed); //hashing of the salted password
                   
		// insert one row into the langs table
		db.run(`INSERT INTO USERS(USERNAME,SALT,HPASSWORD) VALUES(? , ? , ?)`,
		 [request_username,salt,hash.digest('hex')],
		 function(err) {
		    if (err) {
		    	res.send("failed");
				reject("ERROR - writing to db failed : "+err.message);
		    }else{
		    	var dir = "./"+request_username;

		    	fs.stat(dir, function(err, stat) { //checking if there exist already directory with this username string name
				    if(err == null) {//if null then there is such directory and service should not be given to this username's client
				        //console.log('File exists');
				        res.send('failed');
					 	reject("ERROR - directory with username already exists | username : "+request_username);
				    } else if(err.code == 'ENOENT') {
				        // file does not exist
				        fs.mkdir(dir, function(err) { //Creating a directory with this username string for future requests
					        if (err) {
					            res.send('failed');
					            reject("ERROR - could not create directory with username : "+request_username);
					        } else {
					        	console.log("created directory successfully : "+dir);
					        	res.send('success'); // successfully created folder
					    		resolve(`user Registered - A row has been inserted for user : `+request_username);
					    	}
					    }); 
				    } else {
				        console.log('Some other error: ', err.code);
				    }
				});
		    	
			}
		});
		} );
	
	};
	getRegisterPromise().then((msg)=>{console.log(msg);},(errmsg)=>{console.log(errmsg);});
	
	
});

//registering a handler for this http register route
app.get('/login',function(req,res){
	//console.log(req.query)
	username = req.query.username;
	console.log(username);
	console.log(req.query.password);
	console.log("login")

	var getLoginPromise = ()=>{
		return new Promise((resolve , reject)=>{

			let sql = `SELECT USERNAME username,
			                  SALT salt,
			                  HPASSWORD hpassword
			           FROM USERS
			           WHERE USERNAME  = ?`;
			let usernameId = username;
			 
			// querying the db with the this request's username
			db.get(sql, [usernameId], (err, row) => {
			  if (err) {
			  	res.send("failed");
			    reject( "Login query Failed : "+err.message);
			  }else{
				  if(row){//there exists such username in db
				  	req.session.username = username; //saving this username in this session
				  	const tobehashed = ""+row.salt+req.query.password; //salt and password from this row from db
					const hash = crypto.createHash('sha256');

                   	hash.update(tobehashed); //hashing
                   	if(hash.digest('hex') ===row.hpassword){
                   		req.session.cwd = "./"+username+"/"; //saving in this session the client's root directory
				  		res.send("success");
				  		resolve("Login success - salt :"+row.salt+" | hpassword : "+ row.hpassword);
				  	}else{
				  		res.send("failed");
				   		reject('Authentication Failed - Hashed Passwords NOT Match');
				  	}
				  }else{
				  	res.send("failed");
				    reject('No username found with the usernameId');
				}
			 }
			});


		});
	}
	getLoginPromise().then((msg)=>{console.log(msg);},(errmsg)=>{console.log(errmsg);});
	

});

//registering a handler for this http logout route
app.get('/logout',function(req,res){
	if(req.session.username){ //checking if the requesting client has done login
		var username = req.session.username;
		req.session.destroy(function(err) {
	  		// cannot access session here
	  		if(err){
	  			console.log("error in logout for user : "+username);
	  			res.send("failed");
	  		}else{
	  			console.log("logout made by user : "+username);
	  			res.send("success");
	  		}
		});
	}else{
		res.send("success");
	}
});


//this http handler is for validation by the client for completing upload to the server
var isUploadsSucceded = {};
app.get('/isUploadCompleted',function(req,res){ //registering a handler for this http register route
	console.log("isUploadCompleted")
	if(req.session.username){ //checking if the requesting client has done login
		//req.session.username = username; // Do not know why it was needed probably copy paste error
		
		var filenamePath = req.query.filenamePath;
		if(isUploadsSucceded[""+req.ip+filenamePath]===true){
			res.send('success');
		}else if (isUploadsSucceded[""+req.ip+filenamePath]==="pending"){
			res.send('pending');
		}else{
			res.send("failed");
		}
	}else{
		res.send('failed');
	}
	
});
var autorizedIps = [];
//the resolve reject acossiative array used because the upload does not work in a request response mechanism - meaning the server socket listens to all clients and not a specific one
var resolveRejectAcosiArr = {};//key ip value {'resolve' : closure, 'reject' : closure ,'username':username ,'filename': filename } 

app.get('/upload',function(req,res){

	var getUploadPromise = ()=>{
		return new Promise((resolve, reject)=>{
			
		
			var filenamePath = "./"+req.session.username+"/"+req.query.filenamePath;
			var size = Number(req.query.size); //needs error checking if there is time
			console.log(filenamePath);
			console.log(size);
			

			if(req.session.username){//checking if the requesting client has done login
				console.log('upload requested by user : '+req.session.username);
				if( (filenamePath.indexOf("..") === -1)){
					//Server uses ip to check if the requster of the upload is legit
					//but what will happen if there is ip spoofing ? - not to worry we are authenticating the files!
					//so unauthorized uploader will be detected when authenticating 
					autorizedIps.push([req.ip,req.query.filenamePath]);
					resolve({'filenamePath': filenamePath, "rfp":req.query.filenamePath,'size':size,'ip':req.ip , "cwd":req.session.cwd , 'username':req.session.username, /*'closure':function(msg){res.send(msg);}*/});
				}else{
					res.send('failed');
					reject('illegal filename for upload');
				}
				
			}else{
				res.send('failed');
				reject('user is not logged');
			}

	} )};

	getUploadPromise().then((getParams)=>{
		
		
		return new Promise( (resolve , reject)=>{
			
			console.log("promise succeeded - now need to listen for upload");
			console.log("filenamePath: "+getParams['filenamePath']+" | size : "+getParams['size']+" | ip: "+ getParams['ip'] +" | cwd : "+getParams['cwd'] );

			var stream = fs.createWriteStream(""+getParams['filenamePath']); //stream for file writing
			getParams['stream'] = function(){ //wrapping the stream and fs so we could transfer them as params to the next promise for use(as long as the reference is referenced it will live in memory)
				fs;
				return stream;}
			stream.once('open', function(fd) {
				resolve(getParams);
			});
			stream.on('error', ()=>{reject(getParams);});
		});

	},(errormsg)=>{
		console.log("error occured : "+errormsg);
	}).then((getParams)=>{
		return new Promise( (resolve , reject)=>{
			
			resolveRejectAcosiArr[""+getParams['ip']+getParams['rfp']] = //saving a reference for the resolve and reject and calling them later on error or success
				{'streamFunc':getParams['stream'],'resolve' : resolve, 'reject' : reject ,'username':getParams['username'] ,'filenamePath': getParams['filenamePath'] , 'size': getParams['size']};
			
			isUploadsSucceded[""+getParams['ip']+getParams['rfp']]="pending";//now the upload is in a pending state
			res.send('success'); //now the client is permitted to upload its file through sockets
		}
		);

	},(getParams)=>{
		res.send('failed');
		console.log("error occured in stream");
	}).then((msg)=>{console.log(msg)},
		(errmsg)=>{console.log(errmsg);});
});

app.get('/download',function(req,res){//registering a handler for this http download route
	var getDownloadPromise = ()=>{
	return new Promise((resolve,reject)=>{
		console.log("download request");
		filename = req.query.filename;
		//size = Number(req.query.size); //**********************needs error checking if there is time***********************
		console.log(filename);
		//console.log(size);
		
		if(req.session.username){//checking if the requesting client has done login
			console.log('download requested by user : '+req.session.username);
			if(filename.indexOf("..") === -1){
				filenamePath = ""+req.session.cwd+filename;
				var fileStream = fs.createReadStream(filenamePath); //opening a stream for reading the requested file to be downloaded 
			    fileStream.on('error', function(err){
			        console.log(err);			  
			        let err1 = new Error();
 	 				err1.name = "error on file stream";
			        reject(err1);
			    })
			    fileStream.on('open',function() {
			        console.log('file for download is now open');
			        //fileStream.pipe(client);		        
			        //resolve({'filename':filename , 'size':size,'ip':req.ip , "cwd":"" , 'username':req.session.username,'fileStream':fileStream});
			        resolveRejectAcosiArr[""+req.ip+filename] = //saving a reference for the resolve and reject and calling them later on error or success
							{'fileStream':fileStream,
							'resolve' : resolve, 
							'reject' : reject ,
							'username':req.session.username ,
							'filename': filename //, 
							//'size': size
						};
					autorizedIps.push([req.ip,filename]);
			        res.send("success");
			    });
			}else{
				let err = new Error();
 				err.name = 'illegal path';
		        reject(err);
		        res.send("failed");
			}		
		}else{
			let err = new Error();
			err.name = 'user is not logged';
	        reject(err);
	        res.send("failed");
		}
	} ) ;
	};
	getDownloadPromise().then(
		(msg)=>{console.log(msg);},
		(err)=>{console.log(err.name);}
	);
});

app.get('/changeDirPath',function(req,res){//registering a handler for this http change current directory route
	console.log("changeDirPath")
	if(req.session.username){//checking if the requesting client has done login
		var path = req.query.path;
		if( (path.indexOf("..") === -1) &&(path.charAt(0)==="/")&& (path.charAt(path.length-1)==="/") ){//validating the path //
			cwd = "./"+req.session.username+path;
			
			fs.stat(cwd, function(err, stat) {//check if the directory path given by the client actually exist
			    if(err == null) {
			        //console.log('File exists');
			        req.session.cwd = cwd;
				 	res.send('success');
			    } else if(err.code == 'ENOENT') {
			        // file does not exist
			        res.send('failed');
			    } else {
			        //console.log('Some other error: ', err.code);
			        res.send('failed');
			    }
			});
				
		}else{
			res.send('failed');
		}
		
		
	}else{
		res.send('failed');
	}
	
});

app.get('/createDir',function(req,res){//registering a handler for this http create directory route
	console.log("createDir")
	if(req.session.username){//checking if the requesting client has done login
		var dirPath = req.query.dirPath;
		if( (dirPath.indexOf("..") === -1)&&(dirPath.charAt(0)==="/") ){ //simple validation
			dir = "./"+req.session.username+dirPath;
			

			fs.stat(dir, function(err, stat) {//check if the directory path given by the client already exist
			    if(err == null) {
			        //console.log('File exists');
			        res.send('failed');
			    } else if(err.code == 'ENOENT') {
			        // file does not exist
			        fs.mkdir(dir, function(err) { //creating the directory
				        if (err) {
				            res.send('failed');
				        } else {
				        	console.log("created directory successfully : "+dir);
				        	res.send('success'); // successfully created folder
				    	
				    	}
				    }); 
			    } else {
			        //console.log('Some other error: ', err.code);
			        res.send('failed');
			    }
			});

		}else{
			res.send('failed');
		}
		
	}else{
		res.send('failed');
	}
	
});

app.get('/deleteDir',function(req,res){//registering a handler for this http delete directory route
	console.log("deleteDir")
	if(req.session.username){//checking if the requesting client has done login
		var dirPath = req.query.dirPath;
		if( (dirPath.indexOf("..") === -1)&&(dirPath.charAt(0)==="/") ){//simple validation
			dir = ""+req.session.username+dirPath;
			

			fs.stat(dir, function(err, stat) {//check if the directory exists
			    if(err == null) {
			        //console.log('File exists');
			        fs.readdir(dir, function(err, files) {//getting info if there are files in directory ,if there are we won't delete it
					    if (err) {
					       res.send('failed');
					    } else {
					       if (!files.length) {
					           // directory appears to be empty
					           	fs.rmdir(dir, function(err) {
							    	if(err){
						    			console.log("delete directory Failed : "+err);
						    			res.send('failed');
							    	}else{
							    		console.log("delete directory succeded");
							    		res.send('success');
							    	}
								});
					       }
					    }
					});
			    } else if(err.code == 'ENOENT') {
			        // file does not exist
			        res.send('failed');
			    } else {
			        //console.log('Some other error: ', err.code);
			        res.send('failed');
			    }
			});

		}else{
			res.send('failed');
		}
		
		
	}else{
		res.send('failed');
	}
	
});

app.get('/viewAvailableFiles',function(req,res){//registering a handler for this http view Available Files route
	console.log("viewAvailableFiles");
	if(req.session.username){//checking if the requesting client has done login
		//loop for checking and testing purposes
		//var keys = Object.keys(resolveRejectAcosiArr);
		//while(keys.length > 0){
		//	keys = Object.keys(resolveRejectAcosiArr);
		//}
		//console.log("after while");

		var treeToRuturn = {};
		var children = [];
		var walkPath = './';

		var walk = function (dir, done) {
	    	fs.readdir(dir, function (error, list) {

	    		treeToRuturn["path"] = "./";
            	//var name = file.substr(path.lastIndexOf('/')+1 , path.length-1);
            	treeToRuturn["name"] = ".";
            	
            	treeToRuturn["size"] = 0;
            	treeToRuturn["type"] = "directory";

	        	if (error) {
	        	    return done(error);
	        	}
	        	var i = 0;
	        	(function next () {
	            	var file = list[i++];
	            	if (!file) {
	               		return done(null);
	            	}	
	           		file = dir + '/' + file;
	            	fs.stat(file, function (error, stat) {
	                	if (stat && stat.isDirectory()) {


	                		const tree = dirTree(file);
	                		children.push(tree);
	                		next();
	                    	//walk(file, function (error) {

	                        //	next();
	                    	//});
	                	} else {
	                    	// do stuff to file here
	                    	console.log(file);
	                    	var tree ={};
	                    	tree["path"] = file;
	                    	var name = file.substr(file.lastIndexOf('/')+1 , file.length-1);
	                    	if(name==="snapshot"){

	                    	}else{
		                    	tree["name"] = name;
		                    	tree["children"] = [];
		                    	tree["size"] = stat.size;
		                    	tree["type"] = "file";
		                    	children.push(tree);
		                    }
	                    	next();
	                	}
	            	});
	        	})();
	    	});
		};
		
		var cwd = process.cwd();
		console.log('Starting directory: ' + process.cwd());
		try {
		  process.chdir('./'+req.session.username);
		  console.log('New directory: ' + process.cwd());
		}
		catch (err) {
		  console.log('chdir: ' + err);
		}
		walk('.', function(error) {
	    	if (error) {
	        	throw error;
	    	} else {
	    		cwd = process.cwd();
				console.log('Starting directory: ' + process.cwd());
				try {
				  process.chdir('../');
				  console.log('New directory: ' + process.cwd());
				}
				catch (err) {
				  console.log('chdir: ' + err);
				}
	        	console.log('-------------------------------------------------------------');
	        	console.log('finished.');
	        	console.log('-------------------------------------------------------------');
	        	treeToRuturn["children"] = children;
				res.setHeader('Content-Type', 'application/json');
    			res.send(JSON.stringify({'response':"success" ,'data':treeToRuturn}));//tree
	    	}
	    	
		});
		

//		const tree = dirTree('./'+req.session.username);//using directory-tree library to return a json tree object string for all files
//		res.setHeader('Content-Type', 'application/json');
//    	res.send(JSON.stringify({'response':"success" ,'data':tree}));

	}else{
		res.send(JSON.stringify({'response':"failed"}));
	}
	
});

app.get('/renameFile',function(req,res){//registering a handler for this http File Rename route
	console.log("renameFile");
	if(req.session.username){//checking if the requesting client has done login

		var oldFilePath = "./"+req.session.username+req.query.oldFilePath;
		var newFilePath = "./"+req.session.username+req.query.newFilePath;
		if( (oldFilePath.indexOf("..") === -1) && (newFilePath.indexOf("..") === -1) ){//simple validation
			fs.rename(oldFilePath, newFilePath, (err) => {//the renaming
				if(err){
					console.log("Error in rename file : "+oldFilePath);
					res.send("failed");
			  	}else{
			  		console.log('Rename complete! from : '+req.query.oldFilePath+" |to :"+req.query.newFilePath);
			  		res.send("success");
			  	}
			});
		}else{
			res.send("failed");
		}
		
	}else{
		res.send("failed");
	}
	
});

app.get('/deleteFile',function(req,res){//registering a handler for this http File Delete route
	console.log("deleteFile");
	if(req.session.username){//checking if the requesting client has done login
		var filePath = "./"+req.session.username+req.query.filePath;
		
		if(req.query.filePath.indexOf("..") === -1){//simple validation
			
			fs.unlink(filePath, (err) => {//the delete
				if (err) {
					console.log("Error in delete file : "+req.query.filePath);
					res.send("failed");
				}else{
					console.log('file was deleted  :'+ req.query.filePath);
					res.send("success")
				}
			});

		}else{
			console.log("simple validation failed");
			res.send("failed");
		}

	}else{
		console.log("no session for user delete file failed");
		res.send("failed");
	}
	
});

//app.listen(3000,()=>{console.log("server up on port 3000");});
app.listen(app.get('port'),()=>{console.log("server up on port "+app.get('port'));});


//var clients = [];

var server = net.createServer(function (socket) { //server creation for listening purpose via sockets for the upload operation

	// Identify this client
	socket.name = socket.remoteAddress + ":" + socket.remotePort 
	var isIn = false;
	var ipClient;
	autorizedIps.some((item,index,_autorizedIps)=>{ //checking if the sending ip is authorized meaning it had already made a request for download and was authorized
		//Array.prototype.some is pretty much the same as forEach but it break when the callback returns true.
		if( ip.isEqual(socket.remoteAddress , item[0]) ){//item=[ip,filename]
			isIn = true;
			ipClient = item[0];
			return true;
		}
	});
		
	if(!isIn){
		console.log('error - illegal user tried to connect - aborting callback');
		return;
	}
	
	var stream; 

	// Put this new client in the list
	//clients.push(socket);
	var fileSentFromClientSize = 0;
	var fileSentFromClientSizeLastIteration = 0;
	// var filenameSizeData = [];
	var filenameSize=0;
	var filenameData = [];
	var filename = "";
	var tempData = Buffer.from([]);
	var isDownload;

	function handlefilename(){
		if(filenameData.length >= filenameSize){
			filename = filenameData.slice(0,filenameSize).toString();//default utf8

			if(!isDownload){//if it is not a download it is an upload
				console.log("filename from socket is : "+filename);
				
				stream = resolveRejectAcosiArr[""+ipClient+filename]['streamFunc']();
				var towrite = filenameData.slice(filenameSize);
				stream.write(towrite);
				fileSentFromClientSize = fileSentFromClientSizeLastIteration + towrite.length;
				fileSentFromClientSizeLastIteration = fileSentFromClientSize;
				if(fileSentFromClientSize === resolveRejectAcosiArr[""+ipClient+filename]['size']){//check if size of what was received mathces the size expected 
					socket.end();
					isUploadsSucceded[""+ipClient+filename]=true;
				}
				socket.on('error', function () {
					//clients.splice(clients.indexOf(socket), 1);
					autorizedIps = autorizedIps.filter((item)=>{return (ip.isEqual(socket.remoteAddress , item[0])) && (filename===item[1]);});
					stream.end();
					//errData
					resolveRejectAcosiArr[""+ipClient+filename]['reject']('Failed Upload : '+resolveRejectAcosiArr[""+ipClient+filename]['username']+' - file NOT recieved : '+resolveRejectAcosiArr[""+ipClient+filename]['filenamePath']+' | ipClient : '+ipClient);
					delete resolveRejectAcosiArr[""+ipClient+filename];
					
				});
				filenameSize = -1; //finished getting the filename and flagging for the next event
			}else{//it is a download
				//the server is connecting as a client to the requesting client which should be listening prior to its request
				
				//var client = new net.Socket();
				//client.connect(8000, getParams['ip'], function() { //***************8000 magic number if there is time needs fix****************************
				//	console.log('download Connected');
					
				//});
				
				filenameSize = -1; //finished getting the filename and flagging for the next event

				const fileStream = resolveRejectAcosiArr[""+ipClient+filename]['fileStream'];
				//client.on('error', function () {
				socket.on('error', function () {
					fileStream.destroy();
					let err = new Error();
					err.name = "error occured while client download";
					//TODO
			        resolveRejectAcosiArr[""+ipClient+filename]['reject'](err);
			        delete resolveRejectAcosiArr[""+ipClient+filename];
				});
				//client.on('close', function() {
				socket.on('close', function() {
					console.log('download Connection closed');
					fileStream.destroy();
					//TODO
					resolveRejectAcosiArr[""+ipClient+filename]['resolve']("client download ended");	
					delete resolveRejectAcosiArr[""+ipClient+filename];
				});
				//fileStream.pipe(client);
				//TODO
				fileStream.on('data',function(chunk){
					socket.write(chunk);
				});
				//TODO
				fileStream.on('end',function(){
					console.log("read for download from file ended");
					socket.end();//used end() instead of destroy() so if there is data on the socket it will arrive
				});
			}
		}
	}
	//var buf;
	// Handle incoming messages from clients.
	//delimeter is size of file
	socket.on('data', function (data) {
		if(filenameSize === 0){
			tempData = Buffer.concat([tempData,data]);
			if(tempData.length >=5 ){
				//filenameSize = data[0];
				isDownload = tempData[0];

				// filenameSizeData[0] = tempData[1];
				// filenameSizeData[1] = tempData[2];
				// filenameSizeData[2] = tempData[3];
				// filenameSizeData[3] = tempData[4];
				//data.copy(,0 , 0, 4);//data is of type Array Buffer so no splice method
				
				filenameSize = tempData.readUIntBE(1, 4);//Big Endian is used because this the java's deafult  
														 //confuguration and the client is written in java
				tempData = tempData.slice(5,tempData.length);
				filenameData = Buffer.from(tempData);
				//filenameSize = filenameSizeData[]
				//for ( var i = 3; i >= 0; i--) {
			    //    filenameSize = (filenameSize * 256) + (filenameSizeData[i]&0xFF);
			   // }
			    
			    console.log("size of file name from user : "+filenameSize +" | isDownload : "+isDownload);

			    handlefilename();
			    
			}
		}else if(filenameSize > 0) {
			filenameData = Buffer.concat([filenameData,data]);
			handlefilename();

		}else{ //filenameSize = -1
			stream = resolveRejectAcosiArr[""+ipClient+filename]['streamFunc']();
			stream.write(data);
			fileSentFromClientSize = fileSentFromClientSizeLastIteration + data.length;
			fileSentFromClientSizeLastIteration = fileSentFromClientSize;
			if(fileSentFromClientSize === resolveRejectAcosiArr[""+ipClient+filename]['size']){//check if size of what was received mathces the size expected 
				socket.end();
				isUploadsSucceded[""+ipClient+filename]=true;
			}
		}
	} );

	// when connection ends
	// Remove the client from the list when it leaves
	// remove ip from authorized array
	socket.on('end', function () {
		//clients.splice(clients.indexOf(socket), 1);
		autorizedIps = autorizedIps.filter((item)=>{ return (ip.isEqual(socket.remoteAddress , item[0])) && (filename===item[1]); } );
		if(stream){
			stream.end();//note to myself - why writestream has end() method while readstream doesnt and ends from itself?
		}
		if(!isDownload){
			if(fileSentFromClientSize === resolveRejectAcosiArr[""+ipClient+filename]['size']){
				resolveRejectAcosiArr[""+ipClient+filename]['resolve']('end upload of user : '+resolveRejectAcosiArr[""+ipClient+filename]['username']+' - file recieved : '+resolveRejectAcosiArr[""+ipClient+filename]['filenamePath']);
			}else{
				resolveRejectAcosiArr[""+ipClient+filename]['reject']("Error - upload failed sizes of file and sent file NOT match ");
				isUploadsSucceded[""+ipClient+filename]=false;
			}
			delete resolveRejectAcosiArr[""+ipClient+filename];
		}
		
	});
	// dealing with an error
	// remove the client from the list
	// remove ip from authorized array
	

});

server.listen(portSocket,()=>{console.log("socket server is up on port "+portSocket);});

