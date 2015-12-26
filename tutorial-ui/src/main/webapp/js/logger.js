function log(msg) {
	var appender = document.getElementById("appender");
	var record = document.createElement("div");
	record.className = "logRecord";
	
	var message = document.createTextNode(msg);
	
	record.appendChild(message);
	appender.appendChild(record);
}