function subscribe(event) {
	var form = event.target.form;
	var userId = form.elements["userId"].value;
	log("creating notification subscription for user #" + userId);
	
	var request = new XMLHttpRequest();
	request.open("post", "/tutorial/rs/notifications/subscriptions/" + userId, false);
	request.send(null);
	
	var subscriptionId = request.responseText;
	log("user #" + userId + " subscribed for notifications. subscription id: " + subscriptionId);
	
	connect(subscriptionId);
}

function connect(subscriptionId) {
	var connUrl = "ws://" + document.location.host + "/tutorial/notifications/subscription/" + subscriptionId;
	log("connecting to notification subscription endpoint using url: " + connUrl);
	
	var conn = new WebSocket(connUrl);
	
	conn.onopen = function(evt) {
		log("connection to subscription " + subscriptionId + " established");
	}
	
	var notifications = document.getElementById("notifications");
	conn.onmessage = function(evt) {
		var notification = document.createElement("div");
		notification.appendChild(document.createTextNode(evt.data));
		notifications.appendChild(notification);
	}
	
	conn.onerror = function(evt) {
		log("subscription connection error: " + evt.toString());
	}
}