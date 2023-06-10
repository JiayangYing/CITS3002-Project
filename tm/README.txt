This TM Server will run with JDK 1.18(recommended)
After run the server file, the server will first listen through 8081 to connect to the Python QB
And listen to 8082 to connect to the Java QB
To run this project first run the tm and then run the main.py in qb for twice and input different value individually
At running the first qb, input: python, the tm ip address(if test through localhost: 127.0.0.1) The two argument separately.
At running the second qb, input: java, the tm ip address(if test through localhost: 127.0.0.1) The two argument separately.
After server have connected the two qb, use browser to visit http://{the tm ip address}:8080/login to visit the web.
The username will be from 20000001 to 20000010, and the passward will always be 123456

Autherticator Class is for check whether the user id and password is match in the map.
HTMLGenerator Class is for generate the thml content based on given value
QbRequester Class is for handling communicating to the QB when TM needs data
Question Class is a class that store the state of it, the remaining attemption, mark and the question id
RequestHandler Class is to handle the browser's request
ResponseHandler Class is to handle the response data to the browser
Server Class is the main java file to start the server
Session Class is the sepcific user's question set and progress of test
UserConnectHandler Class is to handle the request from browser and doing Session data manipulation
