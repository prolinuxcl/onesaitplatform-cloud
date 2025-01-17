var authorizationsArr 		= []; // add authorizations
var authorizationUpdateArr  = []; // get authorizations of the ontology
var authorizationsIds 		= []; // get authorizations ids for actions
var authorizationObj 		= {}; // object to receive authorizations

// responses.
var referencesArr 			=[]; // references LD array
var referencesIds			=[]; // references ID avoid duplication

var wizardStep = 1;

var OntologyCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
    var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Ontology Controller';
    var logControl = 1;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	var internalLanguage = 'en';
	

	var validIdName = false;
	var validJsonSchema = false;
	var validMetaInf = false;
	var hasId = false; // instance
	
	var mapPropType = {};
	
	var schemaUrl = 'ontologies/schema/';
	var myCodeMirror;
	
	// CONTROLLER PRIVATE FUNCTIONS
	// --------------------------------------------------------------------------------
	
	var handleCodeMirror = function () {

        var myTextArea = document.getElementById('jsonLdContext');
        myCodeMirror = CodeMirror.fromTextArea(myTextArea, {
        	mode: "application/json",
	    	autoRefresh: true,
	    	autoCloseBrackets: true,
	        matchBrackets: true,
	        styleActiveLine: true,
	        theme:"material",
	        lineWrapping: true
    	});
		myCodeMirror.setSize("100%",100);
    };
    
    var checkJsonLdContext = function(){
		if($('#supportsJsonLd').is(':checked')){
			$('#jsonLdCtxt').removeClass('hide');
		}else{
			$('#jsonLdCtxt').addClass('hide');
		}
	}
    
	var getTargetOntologyProperties = function(){
		$('#target-property option').each(function(i, option){
			if(option.value != "")
				option.remove();
		});
		var dstOnt = $('#target-ontology').val();
		var srcAtt = $('#self-property :selected').text();
		jQuery.get('/controlpanel/ontologies/' + dstOnt + '/properties/type/' + mapPropType[srcAtt], function(data){
			var properties = data;
			Object.keys(properties).forEach(function(key){
				$('#target-property').append($('<option>', {value: properties[key] , text: key }));
			});
			$('#target-property').selectpicker('refresh');
		});
		
	}
	
	// CHECK IF JSON STRING WHEN JSON PARSE IS OK OR NOT, IF THE JSON IS
	// MALFORMED THE JSON.parse() is broken.
	var IsJsonString = function(str) {
		try {
			JSON.parse(str);
		}
		catch (e) {	return false; }

		return true;
	}

	// REDIRECT URL
	var navigateUrl = function(url){
		window.location.href = url;
	}

	// VALIDATE TAGSINPUT
	var validateIdName = function(){
		var error1 = $('.alert-danger');
		var name = $('#name').val();
		if (name.match(/^[0-9]/)!=null || name.trim().length < 0) {
			error1.show();
			$('#nameerror').removeClass('hide').addClass(' font-red');
			$('#name').closest('.form-group').addClass('has-error')
			return false; 
		} else { 
			$('#nameerror').addClass('hide');
			$('#name').closest('.form-group').removeClass('has-error')
			return true;
		}
	}
	
	var validateMetaInfo = function(){
		var metainf = $('#metainf').val();
		if(typeof metainf === 'undefined' || metainf.trim().length < 5 ){
			$('#metainferror').removeClass('hide');
			$('#metainf').prev().addClass('tagsinput-has-error');
			return false;
		}else{
			 $('#metainferror').addClass('hide');
			 $('#metainf').prev().removeClass('tagsinput-has-error');
			return true;
		}
	}	
	
	var validateDescription = function(){		
		var description = $('#description').val();
		var error1 = $('.alert-danger');
		if(typeof description === 'undefined' || description.trim().length < 5 ){
			error1.show();
			$('#descriptionerror').removeClass('hide').addClass(' font-red');
			$('#description').closest('.form-group').addClass('has-error')
			return false;
		}else{
			$('#descriptionerror').addClass('hide');
			$('#description').closest('.form-group').removeClass('has-error')
			return true;
		}
	}
	
	var validateCron = function(){		
		if ($('#cron').val()!==''){
			$('#cron').closest('.input-group').removeClass('has-error');
			$('#cronerror').addClass('hide');
			return true;
		} else {
			$('#cron').closest('.input-group').addClass('has-error');
			$('#cronerror').removeClass('hide').addClass(' font-red');
			return false;
		}	
	}
	
	var validateQuery = function(){		
		var query = myCodeMirrorQueryJs.getValue();
		if(myCodeMirrorQueryJs!=null){
		   $('#query').val(myCodeMirrorQueryJs.getValue());
		}
		if(myCodeMirrorJs!=null){
			$('#postProcess').val(myCodeMirrorJs.getValue());
		}
		if(typeof query === 'undefined' || query.trim().length < 5 ){
			$('#queryerror').removeClass('hide').addClass(' font-red');
			$('#query').closest('.form-group').addClass('has-error');
			return false;
		}else{
			$('#queryerror').addClass('hide');
			$('#query').closest('.form-group').removeClass('has-error');
			return true;
		}
	}
	
	var validateOntology = function(){	
		if ($('#ontology').val()!==''){
			$('#ontology').closest('.form-group').removeClass('has-error');
			$('#ontologyerror').addClass('hide');
			return true;
		} else {
			$('#ontology').closest('.form-group').addClass('has-error');
			$('#ontologyerror').removeClass('hide');
			return false;
		}
		
	}
	
	
	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
        // for more info visit the official plugin documentation:
        // http://docs.jquery.com/Plugins/Validation

        var form1 = $('#ontology_create_form');
        var error1 = $('.alert-danger');
        var success1 = $('.alert-success');


		// set current language
		currentLanguage = ontologyCreateReg.language || LANGUAGE;

        form1.validate({
            errorElement: 'span', // default input error message container
            errorClass: 'help-block help-block-error', // default input error
														// message class
            focusInvalid: false, // do not focus the last invalid input
            ignore: ":hidden:not('.selectpicker, .hidden-validation')", // validate
																		// all
																		// fields
																		// including
																		// form
																		// hidden
																		// input
																		// but
																		// not
																		// selectpicker
			lang: currentLanguage,
			// custom messages
            messages: {
			},
			// validation rules
            rules: {
            },
            invalidHandler: function(event, validator) { // display error
															// alert on form
															// submit
            	validateFormPrevieusly(form1);
            },
            errorPlacement: function(error, element) {
                if 		( element.is(':checkbox'))	{ error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline")); }
				else if ( element.is(':radio'))		{ error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline")); }
				else if ( element.is(':hidden'))	{
					if ($('#datamodelid').val() === '') { $('#datamodelError').removeClass('hide');}
				}
				else { error.insertAfter(element); }
            },
            highlight: function(element) { // hightlight error inputs
                $(element).closest('.form-group').addClass('has-error');
            },
            unhighlight: function(element) { // revert the change done by
												// hightlight
                $(element).closest('.form-group').removeClass('has-error');
            },
            success: function(label) {
            	
                label.closest('.form-group').removeClass('has-error');
            },
			// ALL OK, THEN SUBMIT.
            submitHandler: function(form) {
            	if(document.getElementById("supportsJsonLd").checked && !IsJsonString(myCodeMirror.getValue())){
    				toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.jsonldformat);
    				return false;
    			}
            	document.getElementById('jsonLdContext').innerHTML = myCodeMirror.getValue();
            	
                validateFormPrevieusly(form1);			
			}
        });
    }

	var validateFormPrevieusly = function (form1){
		//Validate form 
		var isNewOntology = $('#check-new-ontology').is(':checked');
		
		if(isNewOntology){
			// validate mandatory fields if is a new ontology
			console.log("validate form new Ontology");		
			var validIdName = validateIdName();
			var validMetaInfo = validateMetaInfo();
			var validDescription = validateDescription();
			var validCron = validateCron();
			var validQuery = validateQuery();
			
			if (validIdName && validMetaInfo && validDescription && validCron && validQuery){
				console.log("create kpi data for new ontology");
				createOntologyFromQuery(form1);
			}
			else {
				toastr.error(messagesForms.validation.genFormError,'');
			}
		}else{
			//else create data for kpi ontology
			console.log("create kpi data for exist ontology");
			
			var validOntology = validateOntology();
			var validCron = validateCron();
			var validQuery = validateQuery();
			
			if (validOntology && validCron && validQuery){
				var testOntology = GenericFunctions.getOntologyFromQuery(myCodeMirrorQueryJs.getValue());
				
				$.post( '/controlpanel/ontologies/queryKPIOne', { 'query': myCodeMirrorQueryJs.getValue(), 'queryType':'SQL', 'ontologyIdentification': testOntology}, function( data ) {
					console.log("callback post queryKPIOne");

					//Case bad query 
					if(data === "querytool/show :: query"){
						toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.invalidQuery);
					}else{
						try {
							var parseData = JSON.parse(data);									
							if (parseData.length>0){
								$('#schema').val(processJSON(JSON.stringify(parseData[0])));				
								//send form to controller 
								form1.ajaxSubmit(
										{ type: 'post', success : function(data){				
													if(ontologyCreateReg.actionMode != null){
														toastr.success(messagesForms.operations.genOpSuccess,'');
														navigateUrl(data.redirect);
													}else{
														$('#modal-created').modal('show');
													}
												}, error: function(data){
													toastr.error(messagesForms.operations.genOpError,data.responseJSON.cause);
												}
										}
									);
							} else {
								toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.noinstances);
							} 
						} catch (error){
							console.log(error);
							toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.invalidSchema);
						}
					}	
				});
			} else {
				toastr.error(messagesForms.validation.genFormError,'');
			}
		}
	}
	var createOntologyFromQuery = function(form1){
		
		//Check  the query has been executed
		if (!$('#result-panel').is(":visible")){
			toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.executeQuery);
		} else {
		
			var testOntology = GenericFunctions.getOntologyFromQuery(myCodeMirrorQueryJs.getValue());
			
			$.post( '/controlpanel/ontologies/queryKPIOne', { 'query': myCodeMirrorQueryJs.getValue(), 'queryType':'SQL', 'ontologyIdentification': testOntology}, function( data ) {
				
				console.log("callback post queryKPIOne");			
				
				//Case bad query 
				if(data === "querytool/show :: query"){
					toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.invalidQuery);
				}else{
					try {
						var parseData = JSON.parse(data);									
						if (parseData.length>0){
							$('#schema').val(processJSON(JSON.stringify(parseData[0])));				
							//send form to controller 
							form1.ajaxSubmit({type: 'post', success : function(data){						
									if(ontologyCreateReg.actionMode != null){
										toastr.success(messagesForms.operations.genOpSuccess,'');
										navigateUrl(data.redirect);
									} else {
										$('#modal-created').modal('show');
									}
								}, error: function(data){
									toastr.error(messagesForms.operations.genOpError,data.responseJSON.cause);
								}
							})
			
						} else {
							toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.noinstances);
						} 
					} catch (error){
						console.log(error);
						toastr.error(messagesForms.operations.genOpError,ontologyCreateJson.validations.invalidSchema);
					}
				}
			});
		}
		
	}
	
	// CLEAN FIELDS FORM
	var cleanFields = function (formId) {
		logControl ? console.log('cleanFields() -> ') : '';
		
		//CLEAR OUT THE VALIDATION ERRORS
		$('#'+formId).validate().resetForm(); 
		$('#'+formId).find('input:text, input:password, input:file, select, textarea').each(function(){
			// CLEAN ALL EXCEPTS cssClass "no-remove" persistent fields
			if(!$(this).hasClass("no-remove")){$(this).val('');}
		});
		
		//CUSTOM ERRORS
		$('#nameerror').addClass('hide');
		$('#descriptionerror').addClass('hide');
		$('#cron').closest('.input-group').removeClass('has-error');
		$('#cronerror').addClass('hide');
		$('#query').closest('.form-group').removeClass('has-error');		
		$('#queryerror').addClass('hide');
		$('#ontology').closest('.form-group').removeClass('has-error');
		$('#ontologyerror').addClass('hide');
		
		//CLEANING CHECKS
		$('input:checkbox').not('.no-remove').removeAttr('checked');
		loadCodeMirror();
		
		//CLEANING SELECTs
		$(".selectpicker").each(function(){
			$(this).val( '' );
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});
		
		// CLEANING tagsinput
		$('.tagsinput').tagsinput('removeAll');
		$('.tagsinput').prev().removeClass('tagsinput-has-error');
		$('.tagsinput').nextAll('span:first').addClass('hide');
				
		// CLEAN ALERT MSG
		$('.alert-danger').hide();
		
	}	
	
	var manageWizardStep = function(){
		if (wizardStepValid()){
			wizardStepForward();
		} else {
			wizardStepReset();
		}
	}
	
	var wizardStepValid = function(){
		if (wizardStep == 1){
			return (($('#check-new-ontology').is(':checked') &&	$('#name').val().length >= 5 &&	$('#description').val().length >= 5 && $('#metainf').val().length >= 5) || 
					(!$('#check-new-ontology').is(':checked') && validateOntology()));
		} else if (wizardStep == 2){
			return (validateCron() && validateQuery());
		}
	}
	
	var wizardStepForward = function(){
		$('#continueBtn').prop('disabled', false);
		if (wizardStep == 1){
			$('#stepOneCheckbox').prop('checked', true);
			$('#stepOneCheckbox').prop('disabled', false);
			$('#stepOneCheckbox').nextAll('span:first').addClass('wizard-success-step');
		} else if (wizardStep == 2){
			$('#stepTwoCheckbox').prop('checked', true);
			$('#stepTwoCheckbox').prop('disabled', false);
			$('#stepTwoCheckbox').nextAll('span:first').addClass('wizard-success-step');
			$('#createWizardBtn').prop('disabled', false);
		}
	}
	
	var wizardStepReset = function(){
		$('#continueBtn').prop('disabled', true);
		if (wizardStep == 1){
			$('#stepOneCheckbox').prop('checked', false);
			$('#stepOneCheckbox').prop('disabled', true);
			$('#stepOneCheckbox').nextAll('span:first').removeClass('wizard-success-step');
		}  else if (wizardStep == 2){
			$('#stepTwoCheckbox').prop('checked', false);
			$('#stepTwoCheckbox').prop('disabled', true);
			$('#stepTwoCheckbox').nextAll('span:first').removeClass('wizard-success-step');
			$('#createWizardBtn').prop('disabled', true);
		}
	}
	
	var wizardStepContinue = function(){
		if (wizardStep == 1){
			if(!document.getElementById("supportsJsonLd").checked) {
				$('#tab-kpi-conf a').removeClass('disabled');
				$('#tab-kpi-conf a').click();
				$('#continueBtn').addClass('hide');
				$('#createWizardBtn').removeClass('hide');
				wizardStep = 2;
			} else{
				if(IsJsonString(myCodeMirror.getValue())){
					$('#tab-kpi-conf a').removeClass('disabled');
					$('#tab-kpi-conf a').click();
					$('#continueBtn').addClass('hide');
					$('#createWizardBtn').removeClass('hide');
					wizardStep = 2;
				} else {
					toastr.error(messagesForms.operations.genOpError, ontologyCreateReg.validations.jsonldformat);
				}
			}
		} else if (wizardStep == 2){
			
		}
	}

	
	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function(){
		logControl ? console.log('initTemplateElements() ->  resetForm,  currentLanguage: ' + currentLanguage) : '';

		// tagsinput validate fix when handleValidation()
		$('#metainf').on('itemAdded', function(event) {

			if ($(this).val() !== ''){ $('#metainferror').addClass('hide');}
		});

		// authorization tab control
		$(".nav-tabs a[href='#tab_2']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
			e.preventDefault();
			$.alert({title: 'INFO!', type: 'blue' , theme: 'dark', content: ontologyCreateReg.validations.authinsert});
			return false;
		  }
		});

		
		$("#check-new-ontology").change(function () { // fires on every change
			if($('#newOntologyDiv').hasClass('hide')){			
				$('#newOntologyDiv').removeClass('hide');
				$('.newOntology').addClass('hide');
			}else{			
				$('#newOntologyDiv').addClass('hide');
				$('.newOntology').removeClass('hide');
			}
			manageWizardStep();
		});


		// INPUT MASK FOR ontology identification allow only letters, numbers
		// and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_]*", greedy: false });

		// Reset form
		$('#resetBtn').on('click',function(){
			cleanFields('ontology_create_form');
		});
		
		// Fields OnBlur validation
		$('#name').bind('blur', function (ev) { // fires on every blur
			if ($('#check-new-ontology').is(':checked')){
				validateIdName();             // checks form for validity
			}
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});		

		$('#metainf').parent().on('blur', 'input', function(event) {
			if ($('#check-new-ontology').is(':checked')){
				if ($(event.target).parent().next().val() !== ''){
					$(event.target).parent().next().nextAll('span:first').addClass('hide');
					$(event.target).parent().removeClass('tagsinput-has-error');
				} else {
					$(event.target).parent().next().nextAll('span:first').removeClass('hide');
					$(event.target).parent().addClass('tagsinput-has-error');
				}
			}
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});		
		
		$('#description').bind('blur', function (ev) { // fires on every blur
			if ($('#check-new-ontology').is(':checked')){
				validateDescription();             // checks form for validity
			}
		}).on('keyup', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});
		
		$('#ontology').parent().on('blur', 'div', function(event) {
			if (!$('#check-new-ontology').is(':checked')){
				if (event.currentTarget.getElementsByTagName('select')[0]){
					if ($('#' + event.currentTarget.getElementsByTagName('select')[0].getAttribute('id')).val()!==''){
						$(event.target).closest('.form-group').removeClass('has-error');
						$('#ontologyerror').addClass('hide');
					} else {
						$(event.target).closest('.form-group').addClass('has-error');
						$('#ontologyerror').removeClass('hide');
					}
				}
			}
		}).on('change', function(){
			if (ontologyCreateJson.actionMode==null){
				manageWizardStep();
			}
		});
		
		$('#cron').bind('blur', function (ev) { //cron validation
			validateCron();
			manageWizardStep();
		}).change(function () { // fires on every change
			validateCron();
			manageWizardStep();
		});
		
		
		$('#queryeditor').on('blur', 'div', function(event) {
			if (event.currentTarget.closest('.CodeMirror').CodeMirror.getValue() !== ''){ 
				$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').addClass('hide');
				$(event.currentTarget.closest('.CodeMirror')).removeClass('editor-has-error');
			} else {
				$(event.currentTarget.closest('.CodeMirror')).nextAll('span:first').removeClass('hide');
				$(event.currentTarget.closest('.CodeMirror')).addClass('editor-has-error');
			}
			manageWizardStep();
		})
		
		
		
		$('#query').bind('blur', function (ev) { // fires on every blur
			validateQuery();
		}).change(function () { // fires on every change
			validateQuery();	
		});
		
		// Wizard container
		
		// general inf tab control
		$(".wizard-option a[href='#tab_1']").on("click", function(e) {
	        $('.wizardContainer').find('.wizard-option').removeClass('active');
	        $('#tab-datos').addClass('active');
		
			$('#continueBtn').removeClass('hide');
			$('#continueBtn').prop('disabled', false);
			$('#createWizardBtn').addClass('hide');			
			
			wizardStep = 1;
	    });
		
		// general template/schema tab control
		$(".wizard-option a[href='#tab_kpi_conf']").on("click", function(e) {
		  if ($(this).hasClass("disabled")) {
				e.preventDefault();
				return false;
		  } else {
	        $('.wizardContainer').find('.wizard-option').removeClass('active');
	        $('#tab-kpi-conf').addClass('active');
	        
			$('#continueBtn').removeClass('hide');
			$('#continueBtn').prop('disabled', false);
			$('#createWizardBtn').addClass('hide');			
			
			wizardStep = 2;
		  }
	    });
		
		// INSERT MODE ACTIONS (ontologyCreateReg.actionMode = NULL )
		if ( ontologyCreateReg.actionMode === null){
			logControl ? console.log('|---> Action-mode: INSERT') : '';

			// Set active
			$('#active').trigger('click');

		}
		// EDIT MODE ACTION
		else {
			logControl ? console.log('|---> Action-mode: UPDATE') : '';

			// if ontology has authorizations we load it!.
			authorizationsJson = ontologyCreateReg.authorizations;
			if (authorizationsJson.length > 0 ){

				// MOUNTING AUTHORIZATIONS ARRAY
				var authid_update, accesstype_update , userid_update , authorizationUpdate , authorizationIdUpdate = '';
				$.each( authorizationsJson, function (key, object){

					authid_update 		= object.id;
					accesstype_update 	= object.typeName;
					userid_update 		= object.userId;

					logControl ? console.log('      |----- authorizations object on Update, ID: ' +  authid_update + ' TYPE: ' +  accesstype_update + ' USER: ' +  userid_update  ) : '';

					// AUTHs-table {"users":user,"accesstypes":accesstype,"id":
					// response.id}
					authorizationUpdate = {"users": userid_update, "accesstypes": accesstype_update, "id": authid_update};
					authorizationsArr.push(authorizationUpdate);

					// AUTH-Ids {[user_id]:auth_id}
					authorizationIdUpdate = {[userid_update]:authid_update};
					authorizationsIds.push(authorizationIdUpdate);

					// disable this users on users select
					$("#users option[value=" + userid_update + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');

				});

				// TO-HTML
				if ($('#authorizations').attr('data-loaded') === 'true'){
					$('#ontology_autthorizations > tbody').html("");
					$('#ontology_autthorizations > tbody').append(mountableModel2);
				}
				logControl ? console.log('authorizationsArr on UPDATE: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr)) : '';
				$('#ontology_autthorizations').mounTable(authorizationsArr,{
					model: '.authorization-model',
					noDebug: false
				});

				// hide info , disable user and show table
				$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));
				$('#authorizations').removeClass('hide');
				$('#authorizations').attr('data-loaded',true);// TO-HTML
				$("#users").selectpicker('deselectAll');

			}

			// take schema from ontology and load it
			schema = ontologyCreateReg.schemaEditMode;

			// overwrite datamodel schema with loaded ontology schema generated
			// with this datamodel template.
		//	var theSelectedModel = $("li[data-model='"+ ontologyCreateReg.dataModelEditMode +"']");
			//var theSelectedModelType = theSelectedModel.closest('div .panel-collapse').parent().find("a").trigger('click');
		//	theSelectedModel.attr('data-schema',schema).trigger('click');

		}
	}


	// DATAMODEL TEMPLATE COUNTERS
	var dataModeltemplateCounters = function(){

		var datamodels = $('.datamodel-types');
		datamodels.each(function(ind,elem){
			var templates = $(elem).find('ul.datamodel-template').length;
			var typeHref = $(elem).find('a.list-toggle-container > div.list-toggle');
			$('<span class="pull-right badge badge-success">'+ templates +'</span>').appendTo(typeHref);
			if (templates == 0 ) { $(elem).find('div.list-toggle').removeClass('bg-blue-hoki font-grey-carrara').addClass('bg-grey-steel font-grey-cascade hide');	}
		});
	}


	// DELETE ONTOLOGY
	var deleteOntologyConfirmation = function(ontologyId){
		console.log('deleteOntologyConfirmation() -> formId: '+ ontologyId);

		// no Id no fun!
		if ( !ontologyId ) {$.alert({title: 'ERROR!', type: 'red' , theme: 'dark', content: ontologyCreateReg.validations.validform}); return false; }

		logControl ? console.log('deleteOntologyConfirmation() -> formAction: ' + $('.delete-ontology').attr('action') + ' ID: ' + $('#delete-ontologyId').attr('ontologyId')) : '';

		// call ontology Confirm at header.
		HeaderController.showConfirmDialogOntologia('delete_ontology_form',ontologyId);
	}

	

	// AJAX AUTHORIZATION FUNCTIONS
	var authorization = function(action,ontology,user,accesstype,authorization,btn){
		logControl ? console.log('|---> authorization()') : '';
		var insertURL = '/controlpanel/ontologies/authorization';
		var updateURL = '/controlpanel/ontologies/authorization/update';
		var deleteURL = '/controlpanel/ontologies/authorization/delete';
		var response = {};

		if (action === 'insert'){
			console.log('    |---> Inserting... ' + insertURL);
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({
				url:insertURL,
				headers: {
					[csrf_header]: csrf_value
			    },
				type:"POST",
				async: true,
				data: {"accesstype": accesstype, "ontology": ontology,"user": user},
				dataType:"json",
				success: function(response,status){

					var propAuth = {"users":user,"accesstypes":accesstype,"id": response.id};
					authorizationsArr.push(propAuth);
					console.log('     |---> JSONtoTable: ' + authorizationsArr.length + ' data: ' + JSON.stringify(authorizationsArr));
					// store ids for after actions. inside callback
					var user_id = user;
					var auth_id = response.id;
					var AuthId = {[user_id]:auth_id};
					authorizationsIds.push(AuthId);
					console.log('     |---> Auths: ' + authorizationsIds.length + ' data: ' + JSON.stringify(authorizationsIds));

					// TO-HTML
					if ($('#authorizations').attr('data-loaded') === 'true'){
						$('#ontology_autthorizations > tbody').html("");
						$('#ontology_autthorizations > tbody').append(mountableModel2);
					}
					console.log('authorizationsArr: ' + authorizationsArr.length + ' Arr: ' + JSON.stringify(authorizationsArr));
					$('#ontology_autthorizations').mounTable(authorizationsArr,{
						model: '.authorization-model',
						noDebug: false
					});

					// hide info , disable user and show table
					$('#alert-authorizations').toggle($('#alert-authorizations').hasClass('hide'));
					$("#users").selectpicker('deselectAll');
					$("#users option[value=" + $('#users').val() + "]").prop('disabled', true);
					$("#users").selectpicker('refresh');
					$('#authorizations').removeClass('hide');
					$('#authorizations').attr('data-loaded',true);
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});


		}
		if (action === 'update'){
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({url:updateURL, type:"POST", async: true,
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization, "accesstype": accesstype},
				dataType:"json",
				success: function(response,status){

					var updateIndex = foundIndex(user,'users',authorizationsArr);
					authorizationsArr[updateIndex]["accesstypes"] = accesstype;
					console.log('ACTUALIZADO: ' + authorizationsArr[updateIndex]["accesstypes"]);

					// UPDATING STATUS...
					$(btn).find("i").removeClass('fa fa-spin fa-refresh').addClass('fa fa-edit');
					$(btn).find("span").text('Update');
					
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			});


		}
		if (action  === 'delete'){
			console.log('    |---> Deleting... ' + user + ' with authId:' + authorization );
			var csrf_value = $("meta[name='_csrf']").attr("content");
			var csrf_header = $("meta[name='_csrf_header']").attr("content");
			$.ajax({url:deleteURL, type:"POST", async: true,
				headers: {
					[csrf_header]: csrf_value
			    },
				data: {"id": authorization},
				dataType:"json",
				success: function(response,status){

					// remove object
					var removeIndex = authorizationsIds.map(function(item) { return item[user]; }).indexOf(authorization);
					authorizationsIds.splice(removeIndex, 1);
					authorizationsArr.splice(removeIndex, 1);

					console.log('AuthorizationsIDs: ' + JSON.stringify(authorizationsIds));
					// refresh interface. TO-DO: EL this este fallará
					if ( response  ){
						$(btn).closest('tr').remove();
						$("#users option[value=" + user + "]").prop('disabled', false);
						$("#users").selectpicker('deselectAll');
						$("#users").selectpicker('refresh');
						if (authorizationsArr.length == 0){
							$('#alert-authorizations').toggle(!$('#alert-authorizations').is(':visible'));
							$('#authorizations').addClass('hide');

						}
						toastr.success(messagesForms.operations.genOpSuccess,'');
					} else {
						toastr.error(messagesForms.operations.genOpError,'NO RESPONSE!');
					}
				}
			});
		}
	};

	// return position to find authId.
	var foundIndex = function(what,item,arr){
		var found = '';
		arr.forEach(function(element, index, array) {
			if ( what === element[item]){ found = index;  console.log("a[" + index + "] = " + element[item] + ' Founded in position: ' + found ); }

		});
		return found;
	}
	
	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/ontologies/freeResource/" + id).done(
				function(data){
					console.log('freeResource() -> ok');
					navigateUrl(url); 
				}
			).fail(
				function(e){
					console.error("Error freeResource", e);
					navigateUrl(url); 
				}
			)		
	}

	// CONTROLLER PUBLIC FUNCTIONS
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return ontologyCreateReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			handleCodeMirror();
			initTemplateElements();
			dataModeltemplateCounters();
			
			//SET DEFAULT DATAMODEL TO EMPTYBASE
			$('#datamodelid').val($('#EmptyBase li').attr('data-model'));

			// PROTOTYPEs
			// ARRAY PROTOTYPE FOR CHECK UNIQUE PROPERTIES.
			Array.prototype.unique = function() {
				return this.filter(function (value, index, self) {
					return self.indexOf(value) === index;
				});
			};

			// ARRAY PROTROTYPE FOR REMOVE ELEMENT (not object) BY VALUE
			Array.prototype.remove = function() {
				var what, a = arguments, L = a.length, ax;
				while (L && this.length) {
					what = a[--L];
					while ((ax = this.indexOf(what)) !== -1) {
						console.log('AX: ' + ax);
						this.splice(ax, 1);
					}
				}
				return this;
			};
			
			$('#supportsJsonLd').on('click', function(){
				checkJsonLdContext();				
			});
			checkJsonLdContext();
		},

		// REDIRECT
		go: function(url){
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';
			freeResource(id,url);
		},

		// DELETE ONTOLOGY
		deleteOntology: function(ontologyId){
			logControl ? console.log(LIB_TITLE + ': deleteOntology()') : '';
			deleteOntologyConfirmation(ontologyId);
		},
		
		// WIZARD SEQUENCING
		wizardContinue: function(){
			wizardStepContinue();
		},

		// INSERT AUTHORIZATION
		insertAuthorization: function(){
			logControl ? console.log(LIB_TITLE + ': insertAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){
				// UPDATE MODE ONLY AND VALUES on user and accesstype
				if (($('#users').val() !== '') && ($("#users option:selected").attr('disabled') !== 'disabled') && ($('#accesstypes').val() !== '')){

					// AJAX INSERT (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns
					// object with data.
					authorization('insert',ontologyCreateReg.ontologyId,$('#users').val(),$('#accesstypes').val(),'');

				} else {  $.alert({title: 'ERROR!', theme: 'dark', type: 'red', content: ontologyCreateReg.validations.authuser}); }
			}
		},

		// REMOVE authorization
		removeAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': removeAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){

				// AJAX REMOVE (ACTION,ONTOLOGYID,USER,ACCESSTYPE) returns
				// object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();

				var removeIndex = foundIndex(selUser,'users',authorizationsArr);
				var selAuthorizationId = authorizationsIds[removeIndex][selUser];

				console.log('removeAuthorization:' + selAuthorizationId);

				authorization('delete',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj );
			}
		},
		
		// UPDATE authorization
		updateAuthorization: function(obj){
			logControl ? console.log(LIB_TITLE + ': updateAuthorization()') : '';
			if ( ontologyCreateReg.actionMode !== null){

				// AJAX UPDATE (ACTION,ONTOLOGYID,USER,ACCESSTYPE,ID) returns
				// object with data.
				var selUser = $(obj).closest('tr').find("input[name='users\\[\\]']").val();
				var selAccessType = $(obj).closest('tr').find("select[name='accesstypes\\[\\]']").val();

				var updateIndex = foundIndex(selUser,'users',authorizationsArr);
				var selAuthorizationId = authorizationsIds[updateIndex][selUser];

				console.log('updateAuthorization:' + selAuthorizationId);

				if (selAccessType !== authorizationsArr[updateIndex]["accesstypes"]){

					// UPDATING STATUS...
					$(obj).find("i").removeClass('fa fa-edit').addClass('fa fa-spin fa-refresh');
					$(obj).find("span").text('Updating...');

					authorization('update',ontologyCreateReg.ontologyId, selUser, selAccessType, selAuthorizationId, obj);
				}
				else { console.log('no hay cambios');}
			}
		},
		// GENERATE DUMMY ONTOLOGY INSTANCES
		setRtdbDatasource: function(){
			$('#rtdb').val($('#rtdbInstance').val());
		},
		// GENERATE DUMMY ONTOLOGY INSTANCES
		setRtdbCleanLapse: function(){
			var optSelected = $('#rtdbCleanLapseOpt').val();
			$('#rtdbCleanLapse').val(optSelected);
			if(optSelected != 'Never'){
				$('#rtdbClean').val(true);
			}else{
				$('#rtdbClean').val(false);
			}

		},
		insertRelation : function(){
			var srcProperty = $('#self-property :selected').text();
			var srcPropertyPath = $('#self-property').val();
			var dstOntology = $('#target-ontology').val();
			var dstProperty = $('#target-property :selected').text();
			var dstPropertyPath = $('#target-property').val();
			var validate = $('#validate-property').val();
			if(dstOntology != "" && dstProperty != "")
				insertRelation(srcProperty, dstOntology, dstProperty, srcPropertyPath, dstPropertyPath, validate);
		
			
		},
		deleteRelation : function(object){
			deleteRelation(object);
		},
		getTargetOntologyProperties : function(){
			getTargetOntologyProperties();
		}
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// GLOBAL JSON AND CODE EDITOR INSTANCES
	var editor;
	var aceEditor;
	var schema = ''; // current schema json string var
	var staticSchema = ''; // current schema json string but static just for
							// original fields validation.

	// if redirect from error, change editmode for loading the schema
	if(ontologyCreateJson.actionMode == null && headerReg.errores != null && ontologyCreateJson.schemaEditMode != null ){
		ontologyCreateJson.actionMode = 1;
		ontologyCreateJson.authorizations = [];
		console.log(ontologyCreateJson);
	}

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	OntologyCreateController.load(ontologyCreateJson);

	// AUTO INIT CONTROLLER.
	OntologyCreateController.init();
	
	/*jQuery.validator.addMethod("greaterThan", 
			function(value, element, params) {

			    if (!/Invalid|NaN/.test(new Date(value))) {
			        return new Date(value) > new Date($(params).val());
			    }

			    return isNaN(value) && isNaN($(params).val()) 
			        || (Number(value) > Number($(params).val())); 
			}, ontologyCreateJson.validations.greaterthan);
	
	jQuery.validator.addMethod("lessThan", 
			function(value, element, params) {

			    if (!/Invalid|NaN/.test(new Date(value))) {
			        return new Date(value) < new Date($(params).val());
			    }

			    return isNaN(value) && isNaN($(params).val()) 
			        || (Number(value) < Number($(params).val())); 
			}, ontologyCreateJson.validations.greaterthan);*/

});
