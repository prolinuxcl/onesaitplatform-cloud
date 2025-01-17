var virtualdatasourcesCreateController = function(){
	
	// DEFAULT PARAMETERS, VAR, CONSTS
	var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'CATEGORY Controller';
	logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.	
	var internalLanguage = 'en';
	var formId = "datasource_create_form";
	
    var form1 = $('#datasource_create_form');
	
	// CONTROLLER PRIVATE FUNCTIONS

	var handleValidation = function() {
		
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation: 
        // http://docs.jquery.com/Plugins/Validation
		
		// set current language
		currentLanguage = currentLanguage || LANGUAGE;
		
        form1.validate({
            errorElement: 'span', //default input error message container
            errorClass: 'help-block help-block-error', // default input error message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate all fields including form hidden input but not selectpicker
			lang: currentLanguage,
			// validation rules
            rules: {
            	identification:		{required: true, minlength: 5},
            	urlConnection:		{required: true, minlength: 5},
            },
            messages: {
            	identification: 	{required: virtualdatasourcesCreateJson.validform.emptyfields, minlength: virtualdatasourcesCreateJson.validform.minLength},
            	description:		{required: virtualdatasourcesCreateJson.validform.emptyfields, minlength: virtualdatasourcesCreateJson.validform.minLength},
            },
            invalidHandler: function(event, validator) { //display error alert on form submit
            	toastr.error(messagesForms.validation.genFormError,'');
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list, .radio-inline")); }
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error'); 
            },
            unhighlight: function(element) { // revert the change done by hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
            	$('#credentialsMaintain').val($("#maintainCredentials").is(":checked"));
            	
            	toastr.success(messagesForms.validation.genFormSuccess,'');
                form.submit();
            }
        });
    }
	
	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url; 
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {

		// CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm();
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});

		// CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		// CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');

		// CLEANING NUMBERS
		$('#poolSize').val(100);
		$('#queryLimit').val(100);
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');

		// CLEAN ALERT MSG
		$('.alert-danger').hide();

	}
	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() -> resetForm') : '';

		// Reset form
		$('#resetBtn').on('click',function(){ 
			cleanFields('datasource_create_form');
		});	
		
		// Fields OnBlur validation
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});
		
		if ( virtualdatasourcesCreateJson.actionMode != null){
			logControl ? console.log('|---> Action-mode: UPDATE') : '';
			
			$('#maintainCredentials').trigger('click');
			$("#div-credentials").hide();
		}
		
		$('#maintainCredentials').on("click", function(){
			if($('#maintainCredentials').is(":checked")){
				$("#div-credentials").hide();
			}else{
				$("#div-credentials").show();
			}
		})

	}
	
	// CONTROLLER PUBLIC FUNCTIONS
	return{
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			initTemplateElements();
			handleValidation();
		},
		
		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';	
			navigateUrl(url); 
		},
		submitform: function(){
			
			$("#category_create_form").submit();
		},
		
	};
	
}();


//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	virtualdatasourcesCreateController.load(virtualdatasourcesCreateJson);	
		
	// AUTO INIT CONTROLLER.
	virtualdatasourcesCreateController.init();
	
});