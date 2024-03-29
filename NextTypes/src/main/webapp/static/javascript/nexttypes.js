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
	URL: "url",
	EMAIL: "email",
	TEL: "tel",
	DATE: "date",
	TIME: "time",
	DATETIME: "datetime",
	TIMEZONE: "timezone",
	COLOR: "color",
	BINARY: "binary",
	FILE: "file",
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
};

const ICON = {
	CHECK: "check",
	WARNING: "warning",
	X: "x"
};

const COMPONENT = {
	TYPE: "type",
	REFERENCE: "reference"
};

const NOT_INPUT_KEYS = ["ArrowDown", "ArrowLeft", "ArrowRight", "ArrowUp", "Dead", "Enter"];

var fieldCount = 0;
var indexCount = 0;
var filterCount = 0;

var pageURL = new URL(window.location);
var pageLang = document.documentElement.getAttribute("lang");
	
document.addEventListener("DOMContentLoaded", function() {
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

window.addEventListener("beforeunload", function(event) {
	var forms = document.querySelectorAll("form[data-show-unload-warning]");
	
	for (let form of forms) {
		var changed = form.getAttribute("data-changed");
		
		if (changed) {
			var confirmationMessage = "\o/";
			event.returnValue = confirmationMessage;
			return confirmationMessage;     
		}
	}
});

window.addEventListener("load", function (){
	var textareas = document.querySelectorAll("textarea");
	
	for(const textarea of textareas){
		var editorMode = textarea.getAttribute("data-editor");
		
		if (editorMode != null) {
			
			if (editorMode == "visual") {
				
				tinymceEditor(textarea);
				
			} else {
				
				if (editorMode == "json") {
					editorMode = {name: "javascript", json: true};
				} 
				
				codemirrorEditor(textarea, editorMode);
			}
		}
	}
});

function objectListInput(input) {
	var url = input.getAttribute("data-url");
	var list = input.parentNode.querySelector("datalist");	
	var worker = new Worker("/static/javascript/requestsqueue.js");
	
	worker.addEventListener("message", function(e) {
		
		list.innerHTML = "";
		
		var items = e.data["items"];
		
		for (let id in items) {
			var option = document.createElement("option");
			option.appendChild(document.createTextNode(id));
			option.setAttribute("label", items[id]);
			list.appendChild(option);
		}
	
	}, false);
	
	worker.addEventListener("error", function(e) {
		alert(e.message);
	}, false);
	
	input.addEventListener("keyup", function(e) {
		if (e.key !== undefined && !NOT_INPUT_KEYS.includes(e.key)) {
			
			var search = input.value;
						
			if (search != null && search.length > 0) {
				worker.postMessage(url + "&search=" + search);
			} else {
				list.innerHTML = "";
			}
		}
		
	}, false);
}

function initEventListeners() {
	addEventListeners(document, "button.add-field", "click", addTypeField);
	addEventListeners(document, "button.add-index", "click", addTypeIndex);
	addEventListeners(document, "button.add-filter", "click", addFilter);
	addEventListeners(document, "button.delete-row", "click", deleteRow);
	addEventListeners(document, "a.clear-binary-input", "click", clearBinaryInput);
	addEventListeners(document, "select[data-url-parameter]", "change", changeURLParameter);
	addEventListeners(document, "input.binary", "change", binaryInputChange);
	addEventListeners(document, "input.null", "change", nullInputChange);
	addEventListeners(document, "select.object", "change", loadNames);
	addSelectTableEventListeners(document);
	addFilterEventListeners(document);
	
	var forms = document.querySelectorAll("form[data-show-unload-warning]");
	for (let form of forms) {
		addFormChangeEventListeners(form);
	}
}

function addFormChangeEventListeners(rootElement) {
	addEventListeners(rootElement, "input,textarea,select", "input", formChange);
	addEventListeners(rootElement, "input,textarea,select", "change", formChange);
}

function addSelectTableEventListeners(rootElement) {
	addEventListeners(rootElement, "button.submit-form", "click", submitForm);
	addEventListeners(rootElement, "button.export", "click", exportFunction);
	addEventListeners(rootElement, "a.select-header-anchor", "click", selectHeaderAnchor);
	addEventListeners(rootElement, "a.offset", "click", selectIndexAnchor);
	addEventListeners(rootElement, "a.near-selected-offset", "click", selectIndexAnchor);
	addEventListeners(rootElement, "input.all-checkbox", "change", checkUncheckAll);
	addEventListeners(rootElement, "input.item-checkbox", "change", uncheckAll);
	addEventListeners(rootElement, "select[data-url-parameter", "change", changeURLParameter);
}

function addFilterEventListeners(rootElement) {
	addEventListeners(rootElement, "select.filter-field", "change", changeFilterField);
	addEventListeners(rootElement, "select.filter-comparison", "change", filterComparisonChange);
	
	var inputs = rootElement.querySelectorAll("input.object-list-input");
	for (let input of inputs) {
		objectListInput(input);
	}
}

function addEventListeners(rootElement, query, event, eventFunction) {
	var elements = rootElement.querySelectorAll(query);
	for (let element of elements) {
		element.addEventListener(event, eventFunction);
	}
}

function formChange(event) {
	event.currentTarget.form.setAttribute("data-changed", "data-changed");
}

function uncheckAll(event) {
	var checkbox = event.currentTarget;
	var form = checkbox.form;
	
	if (checkbox.checked == false) {
		form.querySelector("input.all-checkbox").checked = false;
	}
}

function checkUncheckAll(event) {
	var checkbox = event.currentTarget;
	var elements = checkbox.form.querySelectorAll("input.item-checkbox");
	
	if (elements != null) {
		for (let element of elements) {
			if (!element.disabled) {
				element.checked = checkbox.checked;
			}
		}
	}
}

function addTypeField(event) {
	formChange(event);
	
	var request = new XMLHttpRequest();
	request.open("GET", "/?view=json&names", true);
	request.onload = function(e) {
		var types = Object.values(PRIMITIVE_TYPES).concat(JSON.parse(request.responseText));
				
		var table = document.getElementById("fields");
		var body = table.tBodies[0];
		var form = table.parentNode;
		
		var rowCount = body.rows.length;
		var field = "fields:" + fieldCount;
		fieldCount++;
    
		var row = body.insertRow(rowCount);

		row.insertCell(0).appendChild(select(field + ":type", form.getAttribute("data-strings-type"), types));
		
		var name = form.getAttribute("data-strings-name");
		var nameCell = row.insertCell(1);
		var fieldNameInput = input("text", field + ":name", name);
		fieldNameInput.setAttribute("maxlength", MAX_FIELD_NAME_LENGTH);
		nameCell.appendChild(fieldNameInput);
		nameCell.appendChild(input("hidden", field + ":old_name", name));
		
		row.insertCell(2).appendChild(input("text", field + ":parameters",
			form.getAttribute("data-strings-parameters")));
		
		var notNull = input("checkbox", field + ":not_null", form.getAttribute("data-strings-not-null"));
		notNull.checked = true;
		row.insertCell(3).appendChild(notNull);
    
		var deleteRowButton = smallButton(form.getAttribute("data-strings-drop-field"), "minus");
		deleteRowButton.addEventListener("click", deleteRow);
		row.insertCell(4).appendChild(deleteRowButton);
		
		addFormChangeEventListeners(row);
	}
	
    request.send(null);
}

function select(name, title, values) {
	var select = document.createElement("select");
	select.name = name;
	select.title = title;
	
	for (let value of values) {
		var option = document.createElement("option");
		option.value = value;
		option.appendChild(document.createTextNode(value));
		select.appendChild(option);
	}
	
	return select;
}

function addTypeIndex(event) {
	formChange(event);
	
	var table = document.getElementById("indexes");
	var body = table.tBodies[0];
	var form = table.parentNode; 
	
    var rowCount = body.rows.length;
    var index = "indexes:" + indexCount;
    indexCount++;
    
    var row = body.insertRow(rowCount);

    row.insertCell(0).appendChild(select(index + ":mode", form.getAttribute("data-strings-mode"),
    	Object.values(INDEX_MODES)));
    
    var name = form.getAttribute("data-strings-name");
    var nameCell = row.insertCell(1);
    var indexNameInput = input("text", index + ":name", name);
    indexNameInput.setAttribute("maxlength", MAX_INDEX_NAME_LENGTH);
    nameCell.appendChild(indexNameInput);
    nameCell.appendChild(input("hidden", index + ":old_name", name));
    
    row.insertCell(2).appendChild(input("text", index + ":fields", form.getAttribute("data-strings-fields")));
        
    var deleteRowButton = smallButton(form.getAttribute("data-strings-drop-index"), "minus");
    deleteRowButton.addEventListener("click", deleteRow);
    row.insertCell(3).appendChild(deleteRowButton);
    
    addFormChangeEventListeners(row);
}

function loadNames(event) {
	var select = event.currentTarget;
	var value = select.options[select.selectedIndex].value;
	var url = select.getAttribute("data-url");
	var notNull = select.getAttribute("data-not-null");
	var previous = select.getAttribute("data-strings-previous");
	var next = select.getAttribute("data-strings-next");
	
	if (value == "@previous" || value == "@next") {
		
		var offset = Number(select.getAttribute("data-offset"));
		var limit = Number(select.getAttribute("data-limit"));
		
		if (value == "@previous") {
			offset = offset - limit;
		} else if (value == "@next") {
			offset = offset + limit;
		}
		
		var request = new XMLHttpRequest();
		request.open("GET", url + "&offset=" + offset, true);
		request.onload = function(e) {
			
			if (request.status == 200) {
				select.setAttribute("data-offset", offset);
				
				select.options.length = 0;
				
				var names = JSON.parse(request.responseText);
				var items = names["items"];
						
				for (let id in items) {
					var option = document.createElement("option");
					option.appendChild(document.createTextNode(items[id]));
					option.setAttribute("value", id);
					select.appendChild(option);
				}
				
				if (value == "@previous") {
					select.options[select.length - 1].selected = true;
				} else if (value == "@next") {
					select.options[0].selected = true;
				}
				
				if (offset > 0) {
					var option = document.createElement("option");
					option.value = "@previous";
					option.appendChild(document.createTextNode("<<<< " + previous));
					select.prepend(option);
				} else if (!notNull) {
					var option = document.createElement("option");
					select.prepend(option);
				}
				
				if (offset + limit < names["count"]) {
					option = document.createElement("option");
					option.value = "@next";
					option.appendChild(document.createTextNode(next + " >>>>"));
					select.appendChild(option);
				}
				
			} else {
				alert(request.responseText);
			}
		}
		
		request.send(null);
	}
}

function addFilter(event) {
	var table = document.getElementById("filters");
	var body = table.tBodies[0];
	var rowCount = body.rows.length;
	var row = body.insertRow(rowCount);
	
	loadFilter(row, null);	
	
	filterCount++;
}

function loadFilter(row, field) {
	var url = new URL(window.location);
	url.searchParams.set("filter_component", filterCount);
	
	if (field && field != "id") {
		url.pathname = url.pathname + "/id/" + field;
	}
	
	var request = new XMLHttpRequest();
	request.open("GET", url, true);
	request.onload = function(e) {
		
		if (request.status == 200) {
			var container = document.createElement("div");
			container.innerHTML = request.responseText;
						
			addEventListeners(container, "button.delete-row", "click", deleteRow);
			addFilterEventListeners(container);
			
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

function filterComparisonChange(event) {
	var select = event.currentTarget;
	var comparison = select.options[select.selectedIndex].value;
	var filterInput = select.parentNode.parentNode.querySelector(".filter-input");
	var filterTextInput = filterInput.parentNode.querySelector(".filter-text-input");
	
	if (comparison == "like" || comparison == "not_like") {
		if (filterTextInput != null) {
			filterInput.classList.add("hidden");
			filterTextInput.classList.remove("hidden");
			
			if (filterInput.classList.contains("input-group")
					|| filterInput.classList.contains("list-input")) {
				var inputs = filterInput.querySelectorAll("input");
								
				for (let input of inputs) {
					input.disabled = true;
				}
			} else {
				filterInput.disabled = true;
			}
			
			filterTextInput.disabled = false;
		}
	} else {
		if (filterTextInput != null) {
			filterTextInput.classList.add("hidden");
			filterInput.classList.remove("hidden");
			
			filterTextInput.disabled = true;
			
			if (filterInput.classList.contains("input-group")
					|| filterInput.classList.contains("list-input")) {
				var inputs = filterInput.querySelectorAll("input");
				
				for (let input of inputs) {
					input.disabled = false;
				}
			} else {
				filterInput.disabled = false;
			}
		}
	}
}

function objectListInputChange(event) {
	var input = event.currentTarget;
	var url = input.getAttribute("data-url");
	var search = input.value;
	var list = input.parentNode.querySelector("datalist");
	
	list.innerHTML = "";
	
	if (search != null && search.length > 0) {
		var request = new XMLHttpRequest();
		request.open("GET", url + "&search=" + search, true);
		request.onload = function(e) {
			
			if (request.status == 200) {
				var names = JSON.parse(request.responseText);
				var items = names["items"];
				
				for (let id in items) {
					var option = document.createElement("option");
					option.appendChild(document.createTextNode(id));
					option.setAttribute("label", items[id]);
					list.appendChild(option);
				}
			} else {
				alert(request.responseText);
			}
		}
		
		request.send(null);
	}
}

function deleteRow(event) {
	formChange(event);
	
	var row = event.currentTarget.parentNode.parentNode;
	var tableBody = row.parentNode;
	tableBody.deleteRow(row.rowIndex - 1);
}

function input(type, name, title) {
	var input = document.createElement("input");
    input.type = type;
    input.name = name;
    input.title = title;
    return input;
}

function smallButton(text, image) {
	var button = document.createElement("button");
	button.setAttribute("type", "button");
	button.appendChild(smallIcon(text, image));
	return button; 
}

function smallIcon(text, image) {
	var iconElement = icon(text, image);
	iconElement.classList.add("small-icon");
	return iconElement;
}

function icon(text, imageName) {
	return image(text, "/static/icons/" + imageName + ".svg");
}

function image(text, image) {
	var img = document.createElement("img");
	img.setAttribute("src", image);
	img.setAttribute("alt", text);
	return img;
}

function submitForm(event) {
	var button = event.currentTarget;
	var action = button.value;
	var confirmationMessage = button.getAttribute("data-confirmation-message");
	var execute = true;
	var form = button.form;
	var acceptString = form.getAttribute("data-strings-accept");
	var cancelString = form.getAttribute("data-strings-cancel");
	var showProgress = form.getAttribute("data-show-progress");
	
	if (confirmationMessage != null) {
		execute = confirm(confirmationMessage);
	}
	
	if (execute) {
		setFormAction(form, action);
		
		var request = new XMLHttpRequest();
		
		if (showProgress) {
			createProgress(request, cancelString);
		}
		
		request.onload = function(event) {
			var result = null;
			var message = null;
			var dialogType = null;
			var textAlign = "center";
			var callback = null;
			
			if (request.status == 200) {
				
				if (action == ACTION.IMPORT_TYPES || action == ACTION.IMPORT_OBJECTS) {
					textAlign = "left";
				}
				
				form.removeAttribute("data-changed");
				
				dialogType = DIALOG.SUCCESS;
							
				switch (action) {
					case ACTION.DELETE:
						message = request.responseText;
						
						loadSelectTable(button, form.getAttribute("data-url"),
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
						result = JSON.parse(request.responseText);
						message = result["message"];
						
						if (result["altered"]) {
							form.elements["adate"].value = result["adate"];
							
							var inputs = form.querySelectorAll("input[name$=':name']");
							for (let input of inputs) {
								var field = input.name.split(":");
								form.elements[field[0] + ":" + field[1] + ":old_name"].value = input.value;
							}
						} else {
							dialogType = DIALOG.WARNING;
						}						
						
						break;
						
					case ACTION.RENAME:
						result = JSON.parse(request.responseText);
						message = result["message"];
						
						callback = function() {
							var pathname = pageURL.pathname;
							var newName = form.elements["new_name"].value;
							pageURL.pathname = pathname.substr(0, pathname.lastIndexOf("/") + 1) + newName;
							pageURL.searchParams.set("form", "alter");
							window.location = pageURL;
						}
						
						break;
							
					case ACTION.UPDATE:
						result = JSON.parse(request.responseText);
						message = result["message"];
						
						form.elements["udate"].value = result["udate"];
						
						resetBinaryInputs(form);
						
						break;			
							
					case ACTION.UPDATE_ID:
						result = JSON.parse(request.responseText);
						message = result["message"];
						var newId = result["new_id"];
						
						callback = function() {
							var pathname = pageURL.pathname;
							pageURL.pathname = pathname.substr(0, pathname.lastIndexOf("/") + 1) + newId;
							pageURL.searchParams.set("form", "update");
							window.location = pageURL;
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
						result = JSON.parse(request.responseText);
						message = result["message"]; 
						break;
				}
			} else {
				message = request.responseText;
				dialogType = DIALOG.ERROR;
			}
			
			resultDialog(message, dialogType, textAlign, callback, acceptString);
		}
		
		request.open("POST", form.action, true);
		request.send(new FormData(form));
	}
}

function createDialog() {
	var dialog = document.getElementById("dialog");
	
	if (dialog == null) {
	
		var body = document.querySelector("body");
		dialog = document.createElement("dialog");
		dialog.id = "dialog";
		body.appendChild(dialog);
		dialog.showModal();
	}
	
	return dialog;	
}

function createProgress(request, cancelString) {
	var dialog = createDialog();
	dialog.classList.add("progress-dialog");
		
	var bar = document.createElement("progress");
	bar.id = "progress-bar";
	dialog.appendChild(bar);
	
	var text = document.createElement("span");
	text.id = "progress-text";
	dialog.appendChild(text);
	
	var cancelButton = document.createElement("button");
	cancelButton.id = "cancel-button";
	cancelButton.appendChild(document.createTextNode(cancelString));
	dialog.appendChild(cancelButton);
	
	cancelButton.addEventListener("click", function() {
		request.abort();
		removeDialog();
	});
	
	dialog.appendChild(document.createElement("br"));
	
	var start = Date.now();
											
	request.upload.onprogress = function(e) {			
		if (e.lengthComputable) {
			var speed = e.loaded / ((Date.now() - start) / 1000);
												
			bar.max = e.total;
			bar.value = e.loaded;
		    text.innerText = progressText(e.loaded, e.total, speed, pageLang);
		}
	};
}

function progressText(loaded, total, speed, lang) {
	return humanReadableBytes(loaded, lang)
			+ " / " + humanReadableBytes(total, lang)
			+ " - " + humanReadableBytes(speed, lang) + "/s" 
			+ " - " + localeNumeric(loaded * 100 / total, lang) + "%";
}

function humanReadableBytes(bytes, lang) {
	var humanReadableBytes = null;
	
	if (bytes < 1024) {
		humanReadableBytes = bytes + " B";
	} else {
		var exponent = Math.trunc((Math.log(bytes) / Math.log(1024)));
		var unit = "KMGTPEZY".charAt(exponent - 1) + "iB";
		humanReadableBytes = localeNumeric((bytes / Math.pow(1024, exponent)), lang) + " " + unit;
	}
	
	return humanReadableBytes;
}

function localeNumeric(value, lang) {
	return value.toLocaleString(lang, { maximumFractionDigits:2 });
}

function resultDialog(message, type, textAlign, callback, acceptString) {
	var iconName = null;
				
	switch (type) {
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
	
	var dialog = createDialog();
	
	var cancelButton = document.getElementById("cancel-button");
	if (cancelButton) {
		cancelButton.parentNode.removeChild(cancelButton);
	}
		
	dialog.style.textAlign = textAlign;
	dialog.appendChild(icon(iconName, iconName));
	dialog.appendChild(document.createTextNode(message + " "));
	dialog.classList.remove("progress-dialog");
	dialog.classList.add(type + "-dialog");
				
	var acceptButton = document.createElement("button");
	acceptButton.appendChild(document.createTextNode(acceptString));
	acceptButton.addEventListener("click", removeDialog);
	
	if (callback != null) {
		acceptButton.addEventListener("click", callback);
	}
	
	dialog.appendChild(acceptButton);
}

function removeDialog() {
	var dialog = document.getElementById("dialog");
	dialog.parentNode.removeChild(dialog);
}

function exportFunction(event) {
	var button = event.currentTarget;
	var action = button.value;
	var form = button.form;
		
	setFormAction(form, action);
		
	form.method = "POST";
	form.submit();
}

function setFormAction(form, value) {
	var formAction = form.elements["_action"];
	if (formAction == null) {
		formAction = input("hidden", "_action");
		form.appendChild(formAction); 
	}
	formAction.value = value;
}

function selectIndexAnchor(event) {
	var anchor = event.currentTarget;
	var component = anchor.getAttribute("data-component");
	
	if (component) {
		event.preventDefault();
		loadSelectTable(anchor, anchor.href, component);
	}
}

function selectHeaderAnchor(event) {
	event.preventDefault();
	
	var anchor = event.currentTarget;
	
	var url = anchor.href;
		
	if (event.ctrlKey) {
		url = new URL(url);
		var order = anchor.getAttribute("data-multi-order");
		if (order != null && order.length > 0) {
			url.searchParams.set("order", order);
		}
	}
	
	var component = anchor.getAttribute("data-component");
		
	if (component) {
		loadSelectTable(anchor, url, component);
	} else {
		window.location = url;
	}
}

function loadSelectTable(element, url, component) {
	while (!element.classList.contains("select")) {
		element = element.parentNode;
	}
		
	var request = new XMLHttpRequest();
	request.open("GET", url + "&component=" + component, true);
	request.onload = function(e) {
		if (request.status == 200) {
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
	var input = event.currentTarget;
	var binaryInput = input.parentNode;
	var file = input.files[0];
	
	var binaryInputSize = binaryInput.querySelector("span.binary-input-size");
	binaryInputSize.innerText = humanReadableBytes(file.size, pageLang);
	
	var clearAnchor = binaryInput.querySelector("a.clear-binary-input");
	if (clearAnchor != null) {
		clearAnchor.classList.remove("hidden");
	}
}

function nullInputChange(event) {
	var nullInput = event.currentTarget;
	var binaryInput = nullInput.parentNode.parentNode;
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

function changeURLParameter(event) {
	var select = event.currentTarget;
	var value = select.options[select.selectedIndex].value;
	var parameter = select.getAttribute("data-url-parameter");
	var component = select.getAttribute("data-component");
		
	if (component) {
		var url = new URL(select.form.getAttribute("data-url"));
		url.searchParams.set(parameter, value);
		loadSelectTable(select, url, component);
	} else {
		pageURL.searchParams.set(parameter, value);
		window.location = pageURL;
	}
}

function codemirrorEditor(textarea, editorMode) {
	var codemirror = CodeMirror.fromTextArea(textarea, {
		lineWrapping: true,
		lineNumbers: true,
		mode: editorMode
	});

	codemirror.on("change", function(editor){
		editor.save();
		editor.getTextArea().form.setAttribute("data-changed", "true");
	});
}

function tinymceEditor(textarea) {
	tinymce.init({
	    target: textarea,
	   	browser_spellcheck: true,
		entity_encoding: "raw",
		language: pageLang,
		promotion: false,
	    setup : function(editor){
	    	 editor.on("change", function(e){
	    		 editor.save();
	    		 editor.getElement().form.setAttribute("data-changed", "true");
	         });
	    }
	});
}