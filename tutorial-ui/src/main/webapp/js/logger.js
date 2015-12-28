function log(msg) {
	var appender = document.getElementById("appender");
	var record = document.createElement("div");
	record.className = "logRecord";
	
	var time = new Date();
	var logMsg = time.getHours() + ":" + time.getMinutes() + ":" + 
			time.getSeconds() + "." + time.getMilliseconds() + " - " + msg;
	var message = document.createTextNode(logMsg);
	
	record.appendChild(message);
	appender.appendChild(record);
}