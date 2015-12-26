function receive(event) {
	var form = event.target.form;
	var userId = form.elements["userId"].value;
	
	log("trying to receive notification for user: " + userId);
	
	var request = new XMLHttpRequest();
	request.onreadystatechange = function() {
		if (request.readyState == 4) {
			log("response status: " + request.status + ", response: " + request.responseText);
		}
	};
	request.open("get", "/tutorial/rs/receiver/1", true);
	request.send(null);
	
	event.preventDefault();
}