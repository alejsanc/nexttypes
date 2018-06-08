window.addEventListener("load", function (){
	textEditors();
});

function textEditors() {
	var textareas = document.querySelectorAll("textarea");
	
	for(const textarea of textareas){
		var editorMode = textarea.getAttribute("data-editor");
		
		if (editorMode != null) {
			
			if (editorMode == "visual") {
				
				tinymceEditor(textarea);
				
			} else {
				
				if (editorMode == "json") {
					editorMode = {name: "javascript", jsonld: true};
				} 
				
				codemirrorEditor(textarea, editorMode);
			}
		}
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
	    width: 700,
		height: 400,
		browser_spellcheck: true,
		entity_encoding: "raw",
	    setup : function(editor){
	    	 editor.on("change", function(e){
	    		 editor.save();
	    		 editor.getElement().form.setAttribute("data-changed", "true");
	         });
	    }
	});
}