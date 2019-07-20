from flask import Flask, request,jsonify,send_file,render_template
import socket, datetime
import sqlite3
import qrcode
import PySimpleGUI27 as sg

#Backend Server to To Work With Buzzer App

# Status codes: 
# 001 - Could Not Connect
# 002 - Connected
# 003 - Could Not Ring Buzzer
# 004 - Buzzer Ring Successful 
# 005 - Could Not Get Winner
# 006 - Winner Getting Successful 
# 007 - Could Not Set Ready
# 008 - You Are Ready
# 009 - Everyone Is Not Ready
# 010 - Everyone Is Ready

# ip = socket.gethostbyname(socket.gethostname())
database_name = "database"
app = Flask(__name__)


status = "Loading...."
first_user = ""
ready_list = []
connected_list = []
question_number = 1

def db_init():
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('''CREATE TABLE records
				 (username text, touch_time text, question text, session text, ip text, entry_time text)''')

@app.route('/gui/<session>/<question>', methods=['POST','GET'])
def gui(session,question):
	next_question = str(int(question) + 1)
	# session = request.args['session']
	# question = request.args['question']
	with sqlite3.connect("{}.db".format(database_name)) as conn:
		c = conn.cursor()
		c.execute('SELECT * FROM records WHERE session={} AND question={}'.format(session,question))
		matching_records = c.fetchall()
		print matching_records

	return render_template('gui.html', matching_records=matching_records, question=question,session=session,next_question=next_question)

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

@app.route('/is_everyone_ready', methods=['POST'])
def is_everyone_ready():
	global connected_list
	global ready_list
	global question_number

	if not request.json or "ip" not in request.json or "username" not in request.json:
		return jsonify({ "data": {}, "error": "Incorrect JSON format"  }), 400
	print connected_list
	print ready_list
	ip = request.json['ip']
	everyone_ready = False
	username = request.json['username']
	sent_time = request.json['sent_time']
	return_time = datetime.datetime.now()
	status_code = "002"
	result_msg = "LOADING..."
	if len(ready_list) == len(connected_list) or len(ready_list) == 0:
		if len(ready_list) == len(connected_list):
			question_number = question_number +1
		result_msg = "READYYY!!"
		everyone_ready = True
		status_code = "010"
		ready_list = []
		
	else:
		result_msg = "WAITING FOR "
		for player in connected_list:
			if player not in ready_list:
				result_msg = result_msg + "," + player
		everyone_ready = False
		status_code = "009"
	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"result": result_msg,
		"question_number": str(question_number)}})
	return result

@app.route('/i_am_ready', methods=['POST'])
def i_am_ready():
	global question_number
	global ready_list
	return_time = datetime.datetime.now()
	if not request.json or "ip" not in request.json or "username" not in request.json:
		return jsonify({ "data": {}, "error": "Incorrect JSON format"  }), 400
	ip = request.json['ip']
	username = request.json['username']
	sent_time = request.json['sent_time']
	result_msg = "you are ready!"
	status_code = "008"
	ready_list.append(username)
	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"result": result_msg}})
	return result




@app.route('/connect', methods=['POST'])
def connect():
	global connected_list
	global question_number
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
			if username not in connected_list:
				connected_list.append(username)
			status_code = "002"
	except Exception, e:
		result_msg = "[{}] {} - failed to connect : {}".format(ip,username,e)
		status_code = "001"
	print connected_list
	print result_msg
	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"question_number":question_number,
		"result": result_msg}})
	return result

@app.route('/ring_buzzer', methods=['POST'])
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

@app.route('/get_winner', methods=['POST'])
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
			highest_time = 9999999
			highest_ip = "Null"
			print matching_records
			for results in matching_records:
				entry_datetime = datetime.datetime.strptime(results[1],"%m/%d/%y %H:%M:%S.%f")
				print entry_datetime
				time_diff = datetime.datetime.now() - entry_datetime
				if (highest_time == 9999999):
					highest_time = time_diff.total_seconds()
					highest_ip = results[4]
					print highest_ip + "," + str(highest_time)
				elif ((time_diff.total_seconds()) > highest_time):
					highest_time = time_diff.total_seconds()
					highest_ip = results[4]
					print highest_ip + "," + str(highest_time)
		result_msg = "[{}] {} - Fetched Winner".format(ip,username)
		status_code = "006"
		print "returned ip : " + highest_ip
	except Exception, e:
		result_msg = "[{}] {} - failed to get winner : {}".format(ip,username,e)
		status_code = "005"

	result = jsonify({"result": {
		"ip": ip,
		"status": status_code,
		"sent_time": sent_time,
		"return_time": return_time,
		"winning_ip" : highest_ip,
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
		app.run(host=get_ip(), port=9001, debug=True, threaded=True)
   