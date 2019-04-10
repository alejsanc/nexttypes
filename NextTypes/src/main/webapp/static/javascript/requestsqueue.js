var urls = [];

makeRequest();

self.addEventListener("message", function(e) {
	urls.push(e.data);
}, false);

function makeRequest() {
	if (urls.length > 0) {
		
		var request = new XMLHttpRequest();
		request.open("GET", urls[0], false);
		request.onload = function(e) {
			
			if (request.status == 200) {
				self.postMessage(JSON.parse(request.responseText));
			} else {
				throw new Error(request.responseText);
			}
		}
		
		request.send(null);
		
		urls.shift();
	}
	
	setTimeout(makeRequest, 100);
}