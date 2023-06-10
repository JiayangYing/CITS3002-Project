import csv
import socket
import subprocess
# for utf-8 decode
from urllib.parse import unquote


# process the question dict
def processQuestionSet(qType):
    # open csv file that contains the question and answer
    filePath = ""
    # user server type to let program to handle different language QB
    if qType == "python":
        filePath = "./pythonQ.csv"
    elif qType == "java":
        filePath = "./javaQ.csv"
    with open(filePath, newline='') as file:
        csvreader = csv.reader(file)
        questionDict = {}
        idx = 0
        for line in csvreader:
            questionDict[idx] = line[0:2]
            idx += 1
    return questionDict


# this function is designed to run python code
def compile_and_run_python_code(input_code: str, function_name: str, expected_result):
    try:
        compiled_code = compile(input_code, "<string>", "exec")
        local_namespace = {}
        exec(compiled_code, local_namespace)

        func = local_namespace.get(function_name)
        if not func:
            return ["0", f"Error: Function '{function_name}' not found.", expected_result]

        result = func()
        match = "1" if str(result) == expected_result else "0"
        return [match, str(result), expected_result]

    except SyntaxError as e:
        return ["0", f"Error: {e}", expected_result]
    except Exception as e:
        return ["0", f"Error: {e}", expected_result]


def compile_and_run_java_code(method_code: str, method_name: str, expected_result):
    class_name = "TestClass"
    class_code = f"""
    public class {class_name} {{
        {method_code}
    }}
    """

    with open(f"{class_name}.java", "w") as f:
        f.write(class_code)

    invoker_code = f"""
    public class Invoker {{
        public static void main(String[] args) {{
            System.out.println({class_name}.{method_name}());
        }}
    }}
    """

    with open("Invoker.java", "w") as f:
        f.write(invoker_code)

    try:
        compile_result = subprocess.run(["javac", f"{class_name}.java", "Invoker.java"], check=True)
    except subprocess.CalledProcessError as e:
        return ["0", f"Error: {e}", expected_result]

    try:
        run_result = subprocess.run(["java", "Invoker"], check=True, capture_output=True, text=True)
        result = run_result.stdout.strip()
        match = "1" if result == expected_result else "0"
        return [match, result, expected_result]
    except subprocess.CalledProcessError as e:
        return ["0", f"Error: {e}", expected_result]


# this function is to analyse the given request information
def process_data(qType, data, mcqDict, codeDict):
    '''
    :param qType: current server type
    :param data: request parameter
    :param mcqDict: multiple choice question map
    :param codeDict: coding challenge map
    :return: respond the data accordingly
    '''
    paraList = data.split("&")
    print(paraList)
    for i in range(len(paraList)):
        paraList[i] = paraList[i].split("=")
        print(paraList[i])
    # each if is to check the first query type, and then do corresponding action
    # the syntax will be queryType=type&queryID=id&ansSubmit
    # queryType will always be provided
    if (paraList[0][1] == "questionText"):
        # return the mcq text with given id
        return mcqDict[int(paraList[1][1])][0]
    if (paraList[0][1] == "questionAnswer"):
        # return the question answer with given id
        return mcqDict[int(paraList[1][1])][1]
    if (paraList[0][1] == "getProgram"):
        # return the coding text with given id
        return codeDict[int(paraList[1][1])][0]
    if (paraList[0][1] == "postProgram"):
        # get the code content and run in the specific QB
        if qType == "python":
            outcome = compile_and_run_python_code(unquote(paraList[2][1].replace('+', ' ')),
                                                  codeDict[int(paraList[1][1])][1], codeDict[int(paraList[1][1])][2])
        else:
            outcome = compile_and_run_java_code(unquote(paraList[2][1].replace('+', ' ')),
                                                codeDict[int(paraList[1][1])][1],codeDict[int(paraList[1][1])][2])
        return outcome[0] + "&" + outcome[1] + "&" + outcome[2]
    if (paraList[0][1] == "getProgAns"):
        # return the coding question answer with given id
        return  codeDict[int(paraList[1][1])][2]


def start_client(qType, host, port):
    # Create a TCP/IP socket
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # create the coding challenge question
    pythonDict = dict()
    pythonCode = {0 : ["Write a python code returnInt() to return 1", "returnInt", "1"],
                  1 : ["Write a python code returnStr() to return Hello", "returnStr", "Hello"]}

    # create the coding challenge question for java
    javaDict = dict()
    javaCode = {0 : ["Write a java static method returnOne() to return 1", "returnOne", "1"],
                1 : ["Write a java static method returnStr() to return Hello", "returnStr", "Hello"]}
    # Connect the socket to the server's address and port
    server_address = (host, int(port))

    # if the mode is for python qb
    if qType == 'python':
        pythonDict = processQuestionSet(qType)
    elif qType == "java":
        javaDict = processQuestionSet(qType)

    # connection to server
    client_socket.connect(server_address)
    print('Connected to server {}:{}'.format(*server_address))

    try:
        # permanently listen to the TM server if the server send request
        while True:
            # Receive header data from the server
            data = client_socket.recv(10)
            if data:
                # Make buffer to receive the remaining data
                length = int(data.decode().split("=")[1])
                remainData = client_socket.recv(length)
                print(data.decode())
                print(remainData.decode())
                if qType == "python":
                    # Process the received data
                    responseData = process_data(qType, remainData.decode(), pythonDict, pythonCode)
                    print(responseData)
                    response_length = f"len={str(len(responseData)).ljust(6)}"
                    response_content = response_length + responseData
                elif qType == "java":
                    responseData = process_data(qType, remainData.decode(), javaDict, javaCode)
                    print(responseData)
                    response_length = f"len={str(len(responseData)).ljust(6)}"
                    response_content = response_length + responseData
                client_socket.sendall(response_content.encode())
    finally:
        # Close the connection
        client_socket.close()
        print('Connection to server closed')


if __name__ == "__main__":
    qbType = input("please input qb type: ")
    qbType = qbType.strip().lower()
    if (qbType == "python" or qbType == "java"):
        tmHost = input("please input tm host: ")
        if qbType == "python":
            start_client(qbType, tmHost, "8081")
        else:
            start_client(qbType, tmHost, "8082")
    else:
        print("Please insert the proper format!")