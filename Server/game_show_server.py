from flask import Flask, request,jsonify
import socket, datetime
# ip = socket.gethostbyname(socket.gethostname())

app = Flask(__name__)

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

@app.route('/ring_buzzer')
def ring_buzzer():
   return 'Hello World'

@app.route('/reset')
def reset():
   return 'Hello World'

@app.route('/get_data')
def get_data():
   return 'Hello World'


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