JFLAGS = -d out -cp out
JC = javac
JVM = java
MAIN = com.server.Server
.SUFFIXES: .java .class

CLASSES_BEFORE_SERVER = src/com/server/Authenticator.java src/com/server/HTMLGenerator.java src/com/server/QbRequester.java src/com/server/Question.java src/com/server/RequestHandler.java src/com/server/ResponseHandler.java src/com/server/Session.java src/com/server/UserConnectHandler.java src/com/server/Server.java
CLASSES_AFTER_SERVER = $(filter-out $(CLASSES_BEFORE_SERVER) $(SERVER_CLASS), $(wildcard src/com/server/*.java))

default: classes

classes: $(CLASSES_BEFORE_SERVER:.java=.class) $(SERVER_CLASS:.java=.class) $(CLASSES_AFTER_SERVER:.java=.class)

.java.class:
	$(JC) $(JFLAGS) $*.java

run:
	$(JVM) -cp out $(MAIN)

clean:
	$(RM) $(shell find ./out -name "*.class")



