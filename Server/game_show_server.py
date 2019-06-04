from flask import Flask, request,jsonify
import socket, datetime
import sqlite3

# Status codes: 
# 001 - Could Not Connect
# 002 - Connected

# ip = socket.gethostbyname(socket.gethostname())
database_name = "database"
app = Flask(__name__)

def db_init():
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('''CREATE TABLE records
	             (username text, touch_time text, question text, ip text)''')

@app.route('/')
def hello_world():
   return 'Hello World'

@app.route('/latency', methods=['POST'])
def latency_test():
	if not request.json or "ip" not in request.json or "sent_time" not in request.json:
		return jsonify({ "data": {}, "error": "No JSON found"  }), 400
	
	ip = request.json['ip']
	sent_time = request.json['sent_time']
	return_time = datetime.datetime.now()

	result = jsonify({"result": {
		"ip": ip,
		"sent_time": sent_time,
		"return_time": return_time}})
   	return result

@app.route('/connect', methods=['POST'])
def connect():
	if not request.json or "ip" not in request.json or "username" not in request.json:
		return jsonify({ "data": {}, "error": "Incorrect JSON format"  }), 400
	
	ip = request.json['ip']
	username = request.json['username']
	sent_time = request.json['sent_time']
	return_time = datetime.datetime.now()
	result_msg = "Failed"
	status_code = "001"

	try:
		with sqlite3.connect("{}.db".format(database_name)) as conn:
			c = conn.cursor()
			command = "INSERT INTO records VALUES ('{}','{}','{}','{}')".format(username,"","",ip)
			print command
			c.execute(command)
			conn.commit()
			# conn.close()
			result_msg = "[{}] {} - connected ".format(ip,username)
			status_code = "002"
	except Exception, e:
		result_msg = "[{}] {} - failed to connect : {}".format(ip,username,e)
		status_code = "001"

	print result_msg
	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"result": result_msg}})
   	return result

@app.route('/ring_buzzer')
def ring_buzzer():
   return 'Hello World'

@app.route('/reset')
def reset():
   return 'Hello World'

@app.route('/get_data')
def get_data():
   return 'Hello World'

@app.route('/print_data')
def print_data():
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('SELECT * FROM records')
	   	return c.fetchone()

def get_ip():
	s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	try:
		# doesn't even have to be reachable
		s.connect(('10.255.255.255', 1))
		IP = s.getsockname()[0]
	except:
		IP = '127.0.0.1'
	finally:
		s.close()
	return IP

if __name__ == '__main__':
   app.run(host=get_ip(), port=9001, debug=True)
   db_init()