const MAX_FIELD_NAME_LENGTH = 30;
const MAX_INDEX_NAME_LENGTH = 30;

const PRIMITIVE_TYPES = {
	INT16: "int16",
	INT32: "int32",
	INT64: "int64",
	FLOAT32: "float32",
	FLOAT64: "float64",
	NUMERIC: "numeric",
	BOOLEAN: "boolean",
	STRING: "string",
	TEXT: "text",
	HTML: "html",
	JSON: "json",
	XML: "xml",
	URI: "uri",
	EMAIL: "email",
	TEL: "tel",
	DATE: "date",
	TIME: "time",
	DATETIME: "datetime",
	TIMEZONE: "timezone",
	COLOR: "color",
	BINARY: "binary",
	IMAGE: "image",
	AUDIO: "audio",
	VIDEO: "video",
	DOCUMENT: "document",
	PASSWORD: "password"
};

const INDEX_MODES = {
	INDEX: "index",
	UNIQUE: "unique",
	FULLTEXT: "fulltext"
};

const DIALOG = {
	SUCCESS: "success",
	WARNING: "warning",
	ERROR: "error"
};

const ACTION = {
	CREATE: "create", 
	ALTER: "alter",
	RENAME: "rename",
	INSERT: "insert", 
	UPDATE: "update",
	UPDATE_ID: "update_id",
	UPDATE_PASSWORD: "update_password",
	DELETE: "delete",
	EXPORT_TYPES: "export_types",
	EXPORT_OBJECTS: "export_objects",
	DROP: "drop",
	IMPORT_TYPES: "import_types",
	IMPORT_OBJECTS: "import_objects",
	LOGIN: "login",
	LOGOUT: "logout"
}

const ICON = {
	CHECK: "check",
	WARNING: "warning",
	X: "x"
}

const COMPONENT = {
	TYPE: "type",
	REFERENCE: "reference"
}

var fieldCount = 0;
var indexCount = 0;
var filterCount = 0;

var pageURI = new URL(window.location);
var pageLang = document.documentElement.getAttribute("lang");
	
document.addEventListener("DOMContentLoaded", function(){
	initEventListeners();
	
	var fieldsTable = document.getElementById("fields");
	var indexesTable = document.getElementById("indexes");
	var filtersTable = document.getElementById("filters");

	if (fieldsTable != null) {
		fieldCount = fieldsTable.tBodies[0].rows.length;
	}
	
	if (indexesTable != null) {
		indexCount = indexesTable.tBodies[0].rows.length;
	}

	if (filtersTable != null) {
		filterCount = filtersTable.tBodies[0].rows.length;
	}
});

window.addEventListener("beforeunload", function(event){
	var forms = document.querySelectorAll("form.unload-confirmation");
	
	for(let form of forms){
		var changed = form.getAttribute("data-changed");
		if(changed == "true"){
			var confirmationMessage = "\o/";
			event.returnValue = confirmationMessage;
			return confirmationMessage;     
		}
	}
});

function initEventListeners(){
	addEventListeners(document, "button.add-field", "click", addTypeField);
	addEventListeners(document, "button.add-index", "click", addTypeIndex);
	addEventListeners(document, "button.add-filter", "click", addFilter);
	addEventListeners(document, "button.delete-row", "click", deleteRow);
	addEventListeners(document, "a.clear-binary-input", "click", clearBinaryInput);
	addEventListeners(document, "select.langs", "change", changeLanguage);
	addEventListeners(document, "select.years", "change", changeYear);
	addEventListeners(document, "select.months", "change", changeMonth);
	addEventListeners(document, "input.binary", "change", binaryInputChange);
	addEventListeners(document, "input.null", "change", nullInputChange);
	addEventListeners(document, "select.filter-field", "change", changeFilterField)
	addSelectTableEventListeners(document);
	
	var forms = document.querySelectorAll("form.unload-confirmation");
	for(let form of forms){
		addFormChangeEventListeners(form);
	}
}

function addFormChangeEventListeners(rootElement){
	addEventListeners(rootElement, "input,textarea,select", "input", formChanged);
	addEventListeners(rootElement, "input,textarea,select", "change", formChanged);
}

function addSelectTableEventListeners(rootElement){
	addEventListeners(rootElement, "button.submit-form", "click", submitForm);
	addEventListeners(rootElement, "button.export", "click", exportFunction);
	addEventListeners(rootElement, "a.select-header-anchor", "click", selectTableHeaderAnchor);
	addEventListeners(rootElement, "a.offset", "click", selectTableIndexAnchor);
	addEventListeners(rootElement, "input.all-checkbox", "change", checkUncheckAll);
	addEventListeners(rootElement, "input.item-checkbox", "change", uncheckAll);
	addEventListeners(rootElement, "select.limit", "change", changeLimit);
}

function addEventListeners(rootElement, query, event, eventFunction){
	var elements = rootElement.querySelectorAll(query);
	for(let element of elements){
		element.addEventListener(event, eventFunction);
	}
}

function formChanged(event){
	event.currentTarget.form.setAttribute("data-changed", "true");
}

function uncheckAll(event){
	var checkbox = event.currentTarget;
	var form = checkbox.form;
	
	if(checkbox.checked == false){
		form.elements["all"].checked = false;
	}
}

function checkUncheckAll(event){
	var checkbox = event.currentTarget;
	var elements = checkbox.form.querySelectorAll("input.item-checkbox");
	
	if(elements != null){
		if(elements instanceof HTMLInputElement){
			elements.checked = checkbox.checked;
		}else{
			for(let element of elements){
				element.checked = checkbox.checked;
			}
		}
	}
}

function addTypeField(event){
	formChanged(event);
	
	var request = new XMLHttpRequest();
	request.open("GET", "/?view=json&names", true);
	request.onload = function(e){
		var types = Object.values(PRIMITIVE_TYPES).concat(JSON.parse(request.responseText));
				
		var table = document.getElementById("fields");
		var body = table.tBodies[0];
		var form = table.parentNode;
		
		var rowCount = body.rows.length;
		var field = "fields:"+fieldCount;
		fieldCount++;
    
		var row = body.insertRow(rowCount);

		row.insertCell(0).appendChild(select(field+":type", form.getAttribute("data-strings-type"), types));
		var fieldNameInput = input("text", field+":name", form.getAttribute("data-strings-name"));
		fieldNameInput.setAttribute("maxlength", MAX_FIELD_NAME_LENGTH);
		row.insertCell(1).appendChild(fieldNameInput);
		row.insertCell(2).appendChild(input("text", field+":parameters", form.getAttribute("data-strings-parameters")));
		
		var notNull = input("checkbox", field+":not_null", form.getAttribute("data-strings-not-null"));
		notNull.checked = true;
		row.insertCell(3).appendChild(notNull);
    
		var deleteRowButton = smallImageButton(form.getAttribute("data-strings-drop-field"), "minus");
		deleteRowButton.addEventListener("click", deleteRow);
		row.insertCell(4).appendChild(deleteRowButton);
		
		addFormChangeEventListeners(row);
	}
	
    request.send(null);
}

function select(name, title, values){
	var select = document.createElement("select")
	select.name = name;
	select.title = title;
	
	for(let value of values){
		var option = document.createElement("option");
		option.value = value;
		option.appendChild(document.createTextNode(value));
		select.appendChild(option);
	}
	
	return select;
}

function addTypeIndex(event){
	formChanged(event);
	
	var table = document.getElementById("indexes");
	var body = table.tBodies[0];
	var form = table.parentNode; 
	
    var rowCount = body.rows.length;
    var index = "indexes:"+indexCount;
    indexCount++;
    
    var row = body.insertRow(rowCount);

    row.insertCell(0).appendChild(select(index+":mode", form.getAttribute("data-strings-mode"), Object.values(INDEX_MODES)));
    var indexNameInput = input("text", index+":name", form.getAttribute("data-strings-name"));
    indexNameInput.setAttribute("maxlength", MAX_INDEX_NAME_LENGTH);
    row.insertCell(1).appendChild(indexNameInput);
    row.insertCell(2).appendChild(input("text", index+":fields", form.getAttribute("data-strings-fields")));
        
    var deleteRowButton = smallImageButton(form.getAttribute("data-strings-drop-index"), "minus");
    deleteRowButton.addEventListener("click", deleteRow);
    row.insertCell(3).appendChild(deleteRowButton);
    
    addFormChangeEventListeners(row);
}

function addFilter(event) {
	var table = document.getElementById("filters");
	var body = table.tBodies[0];
	
	var rowCount = body.rows.length;
	filterCount++;
	
	var row = body.insertRow(rowCount);
	
	loadFilter(row, null);	
}

function loadFilter(row, field) {
	var uri = new URL(window.location);
	uri.searchParams.set("filter_component", filterCount);
	
	if (field && field != "id") {
		uri.pathname = uri.pathname + "/id/" + field;
	}
	
	var request = new XMLHttpRequest();
	request.open("GET", uri, true);
	request.onload = function(e){
		
		if(request.status == 200){
			var container = document.createElement("div");
			container.innerHTML = request.responseText;
			container.querySelector("button.delete-row").addEventListener("click", deleteRow);
			container.querySelector("select.filter-field").addEventListener("change", changeFilterField);
			row.parentNode.replaceChild(container.firstChild, row);
		} else {
			alert(request.responseText);
		}
	}
	request.send(null);
}

function changeFilterField(event) {
	var select = event.currentTarget;
	var field = select.options[select.selectedIndex].value;
	
	loadFilter(select.parentNode.parentNode, field);
}

function deleteRow(event){
	formChanged(event);
	
	var row = event.currentTarget.parentNode.parentNode;
	var tableBody = row.parentNode;
	tableBody.deleteRow(row.rowIndex - 1);
}

function input(type, name, title){
	var input = document.createElement("input");
    input.type = type;
    input.name = name;
    input.title = title;
    return input;
}

function smallImageButton(text, image){
	var button = document.createElement("button");
	button.setAttribute("type", "button");
	button.appendChild(smallIcon(text, image));
	return button; 
}

function smallIcon(text, image){
	var iconElement = icon(text, image);
	iconElement.classList.add("small-icon");
	return iconElement;
}

function icon(text, imageName){
	return image(text, "/static/icons/"+imageName+".svg");
}

function image(text, image){
	var img = document.createElement("img");
	img.setAttribute("src", image);
	img.setAttribute("alt", text);
	return img;
}

function submitForm(event){
	var button = event.currentTarget;
	var action = button.value;
	var exec = false;
	var form = button.form;
	var acceptString = form.getAttribute("data-strings-accept");
	var cancelString = form.getAttribute("data-strings-cancel");
	var showProgress = form.getAttribute("data-show-progress");
	
	switch(action){
		case ACTION.DELETE:
			exec = confirm(form.getAttribute("data-strings-objects-delete-confirmation"));
			break;
			
		case ACTION.DROP:
			exec = confirm(form.getAttribute("data-strings-types-drop-confirmation"));
			break;
			
		default:
			exec = true;
	}
	
	if(exec){
		setFormInput(form, "type_action", action);
		
		var request = new XMLHttpRequest();
		
		if(showProgress) {
				
			progress(request, cancelString);
			var bar = document.getElementById("progress-bar");
			var text = document.getElementById("progress-text");
			var start = Date.now();
											
			request.upload.onprogress = function(e) {			
				if(e.lengthComputable) {
					var speed = e.loaded / ((Date.now() - start) / 1000);
												
					bar.max = e.total;
					bar.value = e.loaded;
		    		text.innerText = progressText(e.loaded, e.total, speed, pageLang);
				}
			};
		}
				request.onload = function(event){
			var response = null;
			var message = null;
			var dialogType = null;
			var textAlign = "center";
			var callback = null;
			
			if(request.status == 200){
				
				if(action == ACTION.IMPORT_TYPES || action == ACTION.IMPORT_OBJECTS){
					textAlign = "left";
				}
				
				form.setAttribute("data-changed", "false");
				
				dialogType = DIALOG.SUCCESS;
							
				switch(action){
					case ACTION.DELETE:
						message = request.responseText;
						
						loadSelectTable(button, form.getAttribute("data-uri"),
								button.getAttribute("data-component"));
												
						break;
					
					case ACTION.DROP:
						message = request.responseText;
						
						var allCheckBox = form.querySelector("input.all-checkbox");
						allCheckBox.checked = false;
							
						var inputs = form.querySelectorAll("input.item-checkbox[type='checkbox']:checked");
						for (let input of inputs) {
							var row = input.parentNode.parentNode;
							row.parentNode.deleteRow(row.rowIndex - 1);
						}
						
						break;
				
					case ACTION.ALTER:
						response = JSON.parse(request.responseText);
						message = response["message"];
						
						form.elements["adate"].value = response["adate"];
						
						var inputs = form.querySelectorAll("input[name$=':name']");
						for (let input of inputs) {
							var field = input.name.split(":");
							form.elements[field[0] + ":" + field[1]+ ":old_name"].value = input.value;
						}
						break;
						
					case ACTION.RENAME:
						response = JSON.parse(request.responseText);
						message = response["message"];
						
						callback = function() {
							var pathname = pageURI.pathname;
							var newName = form.elements["new_name"].value;
							pageURI.pathname = pathname.substr(0, pathname.lastIndexOf("/")+1)+newName;
							pageURI.searchParams.set("form", "alter");
							window.location = pageURI;
						}
						
						break;
							
					case ACTION.UPDATE:
						response = JSON.parse(request.responseText);
						message = response["message"];
						
						form.elements["udate"].value = response["udate"];
						
						resetBinaryInputs(form);
						
						break;			
							
					case ACTION.UPDATE_ID:
						response = JSON.parse(request.responseText);
						message = response["message"];
						
						callback = function(){
							var pathname = pageURI.pathname;
							var newId = form.elements["new_id"].value;
							pageURI.pathname = pathname.substr(0, pathname.lastIndexOf("/")+1)+newId;
							pageURI.searchParams.set("form", "update");
							window.location = pageURI;
						}
						
						break;
					
					case ACTION.LOGIN:
						message = request.responseText;
						
						var user = document.getElementById("user");
						user.classList.remove("hidden");
						
						var logoutButton = document.getElementById("logout-button");
						logoutButton.classList.remove("hidden");
						
						var userName = document.getElementById("user-name");
						userName.innerText = form.elements["login_user"].value;
						
						break;
						
					case ACTION.LOGOUT:
						message = request.responseText;
						
						var user = document.getElementById("user");
						user.classList.add("hidden");
						
						break;
						
					case ACTION.CREATE:
					case ACTION.INSERT:
					case ACTION.UPDATE_PASSWORD:
					case ACTION.EXPORT_TYPES:
					case ACTION.EXPORT_OBJECTS:
					case ACTION.IMPORT_TYPES:
					case ACTION.IMPORT_OBJECTS:
						message = request.responseText;
						break;
						
					default:
						response = JSON.parse(request.responseText);
						message = response["message"]; 
						break;
				}
			}else{
				message = request.responseText;
				dialogType = DIALOG.ERROR;
			}
			
			result(message, dialogType, textAlign, callback, acceptString);
		}
		
		request.open("POST", form.action, true);
		request.send(new FormData(form));
	}
}

function dialogElement() {
	var dialog = document.getElementById("dialog");
	
	if (dialog == null) {
	
		var body = document.querySelector("body");
	
		var dialogBackground = document.createElement("div");
		dialogBackground.id = "dialog-background";
		body.appendChild(dialogBackground);
	
		dialog = document.createElement("div");
		dialog.id = "dialog"
		dialogBackground.appendChild(dialog);
	}
	
	return dialog;	
}

function progress(request, cancelString) {
	var dialog = dialogElement();
	dialog.classList.add("progress-dialog");
		
	var progressBar = document.createElement("progress");
	progressBar.id = "progress-bar"
	dialog.appendChild(progressBar);
	
	var progressText = document.createElement("span");
	progressText.id = "progress-text";
	dialog.appendChild(progressText);
	
	var cancelButton = document.createElement("button");
	cancelButton.id = "cancel-button";
	cancelButton.appendChild(document.createTextNode(cancelString));
	dialog.appendChild(cancelButton);
	
	cancelButton.addEventListener("click", function(){
		request.abort();
		removeDialog();
	});
	
	dialog.appendChild(document.createElement("br"));
}

function progressText(loaded, total, speed, lang) {
	return humanReadableBytes(loaded, lang)
			+ " / " + humanReadableBytes(total, lang)
			+ " - " + humanReadableBytes(speed, lang) + "/s" 
			+ " - " + langNumber(loaded * 100 / total, lang) + "%";
}

function humanReadableBytes(bytes, lang) {
	var humanReadableBytes = null;
	
	if (bytes < 1024) {
		humanReadableBytes = bytes + " B";
	} else {
		var exponent = Math.trunc((Math.log(bytes) / Math.log(1024)));
		var unit = "KMGTPEZY".charAt(exponent - 1) + "iB";
		humanReadableBytes = langNumber((bytes / Math.pow(1024, exponent)), lang) + " " + unit;
	}
	
	return humanReadableBytes;
}

function langNumber(number, lang) {
	return number.toLocaleString(lang, { maximumFractionDigits:2 })
}

function result(message, type, textAlign, callback, acceptString){
	var iconName = null;
				
	switch(type){
		case DIALOG.SUCCESS:
			iconName = ICON.CHECK;
			break;
			
		case DIALOG.WARNING:
			iconName = ICON.WARNING;
			break;
						
		case DIALOG.ERROR:
			iconName = ICON.X;
			break;
	}
	
	var dialog = dialogElement();
	
	var cancelButton = document.getElementById("cancel-button");
	if (cancelButton) {
		cancelButton.parentNode.removeChild(cancelButton);
	}
		
	dialog.style.textAlign = textAlign;
	dialog.appendChild(icon(iconName, iconName));
	dialog.appendChild(document.createTextNode(message+" "));
	dialog.classList.remove("progress-dialog");
	dialog.classList.add(type+"-dialog");
				
	var button = document.createElement("button");
	button.appendChild(document.createTextNode(acceptString));
	button.addEventListener("click", removeDialog);
	
	if(callback != null){
		button.addEventListener("click", callback);
	}
	
	dialog.appendChild(button);
}

function removeDialog(){
	var dialogBackground = document.getElementById("dialog-background");
	dialogBackground.parentNode.removeChild(dialogBackground);
}

function exportFunction(event){
	var button = event.currentTarget;
	var action = button.value;
	var form = button.form;
		
	setFormInput(form, "type_action", action);
		
	form.method = "POST";
	form.submit();
}

function setFormInput(form, name, value){
	var formInput = form.elements[name];
	if(formInput == null){
		formInput = input("hidden", name);
		form.appendChild(formInput); 
	}
	formInput.value = value;
}

function selectTableIndexAnchor(event){
	var anchor = event.currentTarget;
	var component = anchor.getAttribute("data-component");
	
	if (component) {
		event.preventDefault();
		loadSelectTable(anchor, anchor.href, component);
	}
}

function selectTableHeaderAnchor(event){
	event.preventDefault();
	
	var anchor = event.currentTarget;
	
	var uri = anchor.href;
		
	if(event.ctrlKey){
		uri = new URL(uri);
		var order = anchor.getAttribute("data-multi-order");
		if(order != null && order.length > 0){
			uri.searchParams.set("order", order);
		}
	}
	
	var component = anchor.getAttribute("data-component");
		
	if (component) {
		loadSelectTable(anchor, uri, component);
	} else {
		window.location = uri;
	}
}

function loadSelectTable(element, uri, component){
	while(!element.classList.contains("select")){
		element = element.parentNode;
	}
		
	var request = new XMLHttpRequest();
	request.open("GET", uri + "&component=" + component, true);
	request.onload = function(e){
		if(request.status == 200){
			var container = document.createElement("div");
			container.innerHTML = request.responseText;
			addSelectTableEventListeners(container);
			element.parentNode.replaceChild(container.firstChild, element);
		} else {
			alert(request.responseText);
		}
	}
	request.send(null);
}

function binaryInputChange(event) {
	var input = event.target;
	var binaryInput = input.parentNode;
	var file = input.files[0];
	
	var binaryInputSize = binaryInput.querySelector("span.binary-input-size");
	binaryInputSize.innerText = humanReadableBytes(file.size, pageLang);
	
	var clearAnchor = binaryInput.querySelector("a.clear-binary-input");
	clearAnchor.classList.remove("hidden");
}

function nullInputChange(event) {
	var nullInput = event.target;
	var binaryInput = event.target.parentNode.parentNode;
	var input = binaryInput.querySelector("input.binary");
	input.disabled = nullInput.checked;
}

function clearBinaryInput(event) {
	event.preventDefault();
	
	var clearAnchor = event.currentTarget;
	var binaryInput = clearAnchor.parentNode;
	var input = binaryInput.querySelector("input.binary");
	input.value = "";
	
	var binaryInputSize = binaryInput.querySelector("span.binary-input-size");
	binaryInputSize.innerText = binaryInputSize.getAttribute("data-size");
	
	clearAnchor.classList.add("hidden");
}

function resetBinaryInputs(form) {
	var binaryInputSizes = form.querySelectorAll("span.binary-input-size");
	for (let binaryInputSize of binaryInputSizes) {
		binaryInputSize.setAttribute("data-size", binaryInputSize.innerText);
	}
	
	var clearAnchors = form.querySelectorAll("a.clear-binary-input");
	for (let clearAnchor of clearAnchors) {
		clearAnchor.classList.add("hidden");
	}
	
	var nullFieldInputs = form.querySelectorAll("span.null-field-input");
	for (let nullFieldInput of nullFieldInputs) {
		var nullInput = nullFieldInput.querySelector("input.null");
		var binaryInput = nullFieldInput.parentNode;
		var input = binaryInput.querySelector("input.binary");
									
		if (nullInput.checked) {
			nullInput.checked = false;
			input.disabled = false;
			nullFieldInput.classList.add("hidden");
			var binaryInputSize = binaryInput.querySelector("span.binary-input-size");
			binaryInputSize.innerText = "0 B";
			binaryInputSize.setAttribute("data-size", "0 B");
		
		} else if (input.value != "") {
			nullFieldInput.classList.remove("hidden");
		}
	}
	
	var inputs = form.querySelectorAll("input.binary");
	for (let input of inputs) {
		input.value = "";
	}
}

function changeLanguage(event){
	changeURIParameter(event, "lang");
}

function changeYear(event){
	changeURIParameter(event, "year");
}

function changeMonth(event){
	changeURIParameter(event, "month");
}

function changeLimit(event) {
	var select = event.currentTarget;
	var component = select.getAttribute("data-component")
		
	if (component) {
		var uri = new URL(select.form.getAttribute("data-uri"));
		var limit = select.options[select.selectedIndex].value;
		uri.searchParams.set("limit", limit);
		loadSelectTable(select, uri, component);
	} else {
		changeURIParameter(event, "limit");
	}
}

function changeURIParameter(event, parameter){
	var select = event.currentTarget;
	var value = select.options[select.selectedIndex].value;
	
	pageURI.searchParams.set(parameter, value);
	window.location = pageURI;
}