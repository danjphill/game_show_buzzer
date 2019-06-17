from flask import Flask, request,jsonify,send_file
import socket, datetime
import sqlite3
import qrcode

#Backend Server to To Work With Buzzer App

# Status codes: 
# 001 - Could Not Connect
# 002 - Connected
# 003 - Could Not Ring Buzzer
# 004 - Buzzer Ring Successful 
# 005 - Could Not Get Winner
# 006 - Winner Getting Successful 

# ip = socket.gethostbyname(socket.gethostname())
database_name = "database"
app = Flask(__name__)

def db_init():
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('''CREATE TABLE records
	             (username text, touch_time text, question text, session text, ip text, entry_time text)''')

@app.route('/')
def hello_world():
   return 'Game Show Buzzer Server @ {}'.format(get_ip())

@app.route('/code')
def get_code():
	img = qrcode.make(get_ip())
   	return send_file(img, mimetype='image/jpeg')


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
			command = "INSERT INTO records VALUES ('{}','{}','{}','{}','{}','{}')".format(username,"","","",ip,str(datetime.datetime.now()))
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
	if not request.json or "ip" not in request.json or "username" not in request.json:
		return jsonify({ "data": {}, "error": "Incorrect JSON format"  }), 400
	
	ip = request.json['ip']
	username = request.json['username']
	sent_time = request.json['sent_time']
	return_time = datetime.datetime.now()
	result_msg = "Failed"
	status_code = "003"
	question = request.json['question']
	touch_time = request.json['touch_time']
	session = request.json['session']

	try:
		with sqlite3.connect("{}.db".format(database_name)) as conn:
			c = conn.cursor()
			command = "INSERT INTO records VALUES ('{}','{}','{}','{}','{}','{}')".format(username,touch_time,question,session,ip,str(datetime.datetime.now()))
			print command
			c.execute(command)
			conn.commit()
			# conn.close()
			result_msg = "[{}] {} - DING!! (buzzer rang) ".format(ip,username)
			status_code = "004"
	except Exception, e:
		result_msg = "[{}] {} - failed to ring buzzer : {}".format(ip,username,e)
		status_code = "003"

	print result_msg
	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"result": result_msg}})
   	return result
   

@app.route('/reset')
def reset():
   return 'Not Implemented'

@app.route('/get_winner')
def get_data():
	if not request.json or "ip" not in request.json or "username" not in request.json:
		return jsonify({ "data": {}, "error": "Incorrect JSON format"  }), 400
	
	ip = request.json['ip']
	username = request.json['username']
	sent_time = request.json['sent_time']
	return_time = datetime.datetime.now()
	result_msg = "Failed"
	status_code = "005"
	question = request.json['question']
	session = request.json['session']
	try:
		with sqlite3.connect("{}.db".format(database_name)) as conn:
			c = conn.cursor()
			c.execute('SELECT * FROM records WHERE session={} AND question={}'.format(session,question))
			matching_records = c.fetchall()
		   	lowest_time = -1
		   	lowest_ip = "Null"
		   	print matching_records
		   	for results in matching_records:
		   		entry_datetime = datetime.datetime.strptime(results[1],"%m/%d/%y %H:%M:%S")
		   		print entry_datetime
		   		time_diff = datetime.datetime.now() - entry_datetime
		   		if (((time_diff.total_seconds()) < lowest_time) and lowest_time != -1):
		   			lowest_time = time_diff.total_seconds()
		   			lowest_ip = results[4]
		   			print lowest_ip
		   		else:
		   			lowest_time = time_diff.total_seconds()
		   			lowest_ip = results[4]
		   		print lowest_ip
		result_msg = "[{}] {} - Fetched Winner".format(ip,username)
		status_code = "006"
	except Exception, e:
		result_msg = "[{}] {} - failed to get winner : {}".format(ip,username,e)
		status_code = "005"

   	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"winning_ip" : lowest_ip,
		"result": result_msg}})
   	return result

@app.route('/print_data')
def print_data():
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('SELECT * FROM records')
	   	return str(c.fetchall())

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
	try:
		db_init()
	except Exception as e:
		print "[database] {} - {}".format("ERR",e )
		print "[database] {} - {}".format("MSG","Delete Database to Clear Records")
	finally:
   		app.run(host=get_ip(), port=9001, debug=True)
   