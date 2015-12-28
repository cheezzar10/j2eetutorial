var subscription = null;

function subscribe(event) {
	var form = event.target.form;
	var userId = form.elements["userId"].value;
	log("creating notification subscription for user #" + userId);
	
	var request = new XMLHttpRequest();
	request.open("post", "/tutorial/rs/notifications/subscriptions/" + userId, false);
	request.send(null);
	
	var subscriptionId = request.responseText;
	log("user #" + userId + " subscribed for notifications. subscription id: " + subscriptionId);
	
	subscription = { "subscriptionId": subscriptionId };
	connect();
	
	event.preventDefault();
}

function connect() {
	var connUrl = "ws://" + document.location.host + "/tutorial/notifications/subscription/" + subscription.subscriptionId;
	log("connecting to notification subscription endpoint using url: " + connUrl);
	
	var conn = new WebSocket(connUrl);
	
	conn.onopen = function(evt) {
		log("connection to subscription " + subscription.subscriptionId + " established");
	};
	
	var notifications = document.getElementById("notifications");
	conn.onmessage = function(evt) {
		var notification = document.createElement("div");
		notification.appendChild(document.createTextNode(evt.data));
		notifications.appendChild(notification);
		// TODO acknowledgement logic
	};
	
	conn.onerror = function(evt) {
		log("connection error: " + evt);
	};
	
	conn.onclose = function(evt) {
		log("connection closed: " + evt.code + ":" + evt.reason);
		// if not normal close or server is down or violated policy
		if (!(evt.code == 1000 || evt.code == 1001 || evt.code == 1008)) {
			setTimeout(reopenConnection, 5000);
		}
	};
	
	subscription.conn = conn;
	subscription.connUrl = connUrl;
}

function closeConnection(event) {
	log("closing connection");
	subscription.conn.close(1000, "client closed connection");
	event.preventDefault();
}

function reopenConnection(event) {
	log("re-opening connection to notification subscription: " + subscription.subscriptionId);
	connect();
	if (event != null) {
		event.preventDefault();
	}
}

function unsubscribe(event) {
	subscription.conn.send("unsubscribe");
	event.preventDefault();
}