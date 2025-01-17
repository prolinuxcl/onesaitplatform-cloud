var DeviceCreateController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS.
	var APPNAME = 'onesait Platform Control Panel';
	var LIB_TITLE = 'Menu Controller';
	var logControl = 1;
	var LANGUAGE = [ 'es' ];
	var currentLanguage = ''; // loaded from template.
	var currentFormat = '' // date format depends on currentLanguage.
	var internalFormat = 'yyyy/mm/dd';
	var internalLanguage = 'en';
	
	// CONTROLLER PRIVATE FUNCTIONS

	// REDIRECT URL
	var navigateUrl = function(url) {
		window.location.href = url;
	}

	var freeResource = function(id,url){
		console.log('freeResource() -> id: '+ id);
		$.get("/controlpanel/devices/freeResource/" + id).done(
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
	// CLEAN FIELDS FORM
	var cleanFields = function(formId) {
		logControl ? console.log('cleanFields() -> ') : '';

		// CLEAR OUT THE VALIDATION ERRORS
		$('#' + formId).validate().resetForm();
		$('#' + formId).find(
				'input:text, input:password, input:file, select, textarea')
				.each(function() {
					// CLEAN ALL EXCEPTS cssClass "no-remote" persistent fields
					if (!$(this).hasClass("no-remove")) {
						$(this).val('');
					}
				});

		// CLEANING SELECTs
		$(".selectpicker").each(function() {
			$(this).val('');
			$(this).selectpicker('deselectAll').selectpicker('refresh');
		});

		// CLEAN ALERT MSG
		$('.alert-danger').hide();

		// CLEAN META INFO
		$('#id_parameter_metaInfo').empty();
		$('#parameter_metaInfo').val('');

		// CLEAN ONTOLOGIES
		
		$("#datamodel_properties tbody tr").each(
				function(tr) {
					$("#onto").append(
							'<option value="' + this.dataset.ontology + '">'
									+ this.dataset.ontology + '</option>');
					this.remove();

				});
		sortOntologies();

		$("#parameter_clientPlatformOntologies").val('');
		showHideImageTableOntology();
	}
	
	var sortOntologies = function(){
		var options = $("#onto option");

		options.sort(function(a,b) {
		    if (a.text > b.text) return 1;
		    else if (a.text < b.text) return -1;
		    else return 0;
		});

		$("#onto").empty().append(options).selectpicker("refresh");
	}
	
	var sortOntologies = function(){
		var options = $("#onto option");

		options.sort(function(a,b) {
		    if (a.text > b.text) return 1;
		    else if (a.text < b.text) return -1;
		    else return 0;
		});

		$("#onto").empty().append(options).selectpicker("refresh");
	}

	// FORM VALIDATION
	var handleValidation = function() {
		logControl ? console.log('handleValidation() -> ') : '';
		// for more info visit the official plugin documentation:
		// http://docs.jquery.com/Plugins/Validation

		var form1 = $('#device_create_form');
		var error1 = $('.alert-danger');

		// set current language
		currentLanguage = deviceCreateReg.language || LANGUAGE;

		form1
				.validate({
					errorElement : 'span', // default input error message
											// container
					errorClass : 'help-block help-block-error', // default input
																// error message
																// class
					focusInvalid : false, // do not focus the last invalid
											// input
					ignore : ":hidden:not(.selectpicker)", // validate all
															// fields including
															// form hidden input
															// but not selectpicker
					lang : currentLanguage,
					// custom messages
					messages : {

					},
					// validation rules
					rules : {
						identification : {
							minlength : 5,
							required : true
						},
						description : {
							minlength : 5,
							required : true
						},

					},
					invalidHandler : function(event, validator) { // display error
																	// alert on form submit
						toastr.error(deviceCreateJson.messages.validationKO);
						if (!valOntologies()) {
							toastr.error(deviceCreateJson.ontologyNotSelected);
						}
					},
					errorPlacement : function(error, element) {
						if (element.is(':checkbox')) {
							error.insertAfter(element.closest(".md-checkbox-list, .md-checkbox-inline, .checkbox-list, .checkbox-inline"));
						} else if (element.is(':radio')) {
							error.insertAfter(element.closest(".md-radio-list, .md-radio-inline, .radio-list,.radio-inline"));
						} else {
							error.insertAfter(element);
						}
					},
					highlight : function(element) { // hightlight error inputs
						$(element).closest('.form-group').addClass('has-error');
					},
					unhighlight : function(element) { // revert the change
														// done by hightlight
						$(element).closest('.form-group').removeClass('has-error');
					},
					success : function(label) {
						label.closest('.form-group').removeClass('has-error');
					},
					// ALL OK, THEN SUBMIT.
					submitHandler : function(form) {
						if (valOntologies()) {
							toastr.success(deviceCreateJson.messages.validationOK);
							form.submit();
						} else {
							toastr.error(deviceCreateJson.ontologyNotSelected);
						}
					}
				});
	}

	var valOntologies = function() {
		return (validateOntologies().length > 0);
	}

	// INIT TEMPLATE ELEMENTS
	var initTemplateElements = function() {
		logControl ? console
				.log('initTemplateElements() -> selectpickers, datepickers, resetForm, today->dateCreated currentLanguage: '
						+ currentLanguage)
				: '';

		// selectpicker validate fix when handleValidation()
		$('.selectpicker').on('change', function() {
			$(this).valid();
		});

		// INPUT MASK FOR ontology identification allow only letters, numbers and -_
		$("#identification").inputmask({ regex: "[a-zA-Z0-9_-]*", greedy: false });
		
		// set current language and formats
		currentLanguage = deviceCreateReg.language || LANGUAGE[0];
		currentFormat = (currentLanguage == 'es') ? 'dd/mm/yyyy' : 'mm/dd/yyyy';

		logControl ? console.log('|---> datepickers currentLanguage: '
				+ currentLanguage) : '';

		// Reset form
		$('#resetBtn').on('click', function() {
			cleanFields('device_create_form');
		});
		
		$('input,textarea,select:visible').filter('[required]').bind('blur', function (ev) { // fires on every blur
			$('.form').validate().element('#' + event.target.id);                // checks form for validity
		});		

		// INSERT MODE ACTIONS (deviceCreateReg.actionMode = NULL )
		if (deviceCreateReg.actionMode === null) {
			logControl ? console.log('action-mode: INSERT') : '';
			updateTokens($("#parameter_clientPlatformTokens").val());
		}
		// EDIT MODE ACTION
		else {
			updateMetainfo($('#parameter_metaInfo').val());
			updateOntologies($("#parameter_clientPlatformOntologies").val());
			refreshTokens($("#identification").val());
		}

	}

	var addMetainfo = function() {

		var nombre = document.getElementById("name_metainfo").value;
		var valor = document.getElementById("value_metainfo").value;
		
		if (nombre === ''){ toastr.error(messagesForms.operations.genOpError,deviceCreateJson.invalidMeta); return false; }
		
		var p = document.createElement('p');
		text = document.createTextNode(nombre + '=' + valor);

		var div = document.createElement('div');
		div.className = "metainfo tag label label-primary";
		p.appendChild(text);

		var span = document.createElement('span');
		span.className = "fa fa-times";
		span.onclick = function() {
			this.parentNode.parentElement.remove();
			deleteMetainfo(document.getElementById("parameter_metaInfo").value, this.parentNode.parentElement.getElementsByTagName('p')[0].innerText);
		};

		div.appendChild(p);
		p.appendChild(span);

		$("#id_parameter_metaInfo").append(div);

		if (document.getElementById("parameter_metaInfo").value != '') {
			document.getElementById("parameter_metaInfo").value = document
					.getElementById("parameter_metaInfo").value
					+ '#' + nombre + '=' + valor;
		} else {
			document.getElementById("parameter_metaInfo").value = nombre + '='
					+ valor;
		}
		
		// remove values of meta info name and value to add new more...
		$('#name_metainfo').val('');
		$('#value_metainfo').val('');

	}
	
	var deleteMetainfo = function(metaInfoValue, metaVal) {
		if(metaInfoValue.includes('#' + metaVal)) {
			metaInfoValue = metaInfoValue.replace('#' + metaVal, '');
		} else if(metaInfoValue.includes(metaVal + '#')) {
			metaInfoValue = metaInfoValue.replace(metaVal + '#', '');
		} else if (metaInfoValue.includes(metaVal)) {
			metaInfoValue = metaInfoValue.replace(metaVal, '');
		}
		document.getElementById("parameter_metaInfo").value = metaInfoValue;
	}

	var deleteMetainfo = function(metaInfoValue, metaVal) {
		if(metaInfoValue.includes('#' + metaVal)) {
			metaInfoValue = metaInfoValue.replace('#' + metaVal, '');
		} else if(metaInfoValue.includes(metaVal + '#')) {
			metaInfoValue = metaInfoValue.replace(metaVal + '#', '');
		} else if (metaInfoValue.includes(metaVal)) {
			metaInfoValue = metaInfoValue.replace(metaVal, '');
		}
		document.getElementById("parameter_metaInfo").value = metaInfoValue;
	}
	
	var updateMetainfo = function(metaInfoValue) {
		if (metaInfoValue !== null && metaInfoValue.length > 0) {
			var metaInfoElements = metaInfoValue.split('#');
			for (var i = 0; i < metaInfoElements.length; i++) {
				var metaVal = metaInfoElements[i];
				var p = document.createElement('p');
				text = document.createTextNode(metaVal);
				var div = document.createElement('div');
				div.className = "metainfo tag label label-primary";
				p.appendChild(text);
				var span = document.createElement('span');
				span.className = "fa fa-times";
				span.onclick = function() {
					this.parentNode.parentElement.remove();
					deleteMetainfo(metaInfoValue, this.parentNode.parentElement.getElementsByTagName('p')[0].innerText);
				};
				div.appendChild(p);
				p.appendChild(span);
				$("#id_parameter_metaInfo").append(div);

			}
		}
	}

	var addOntologyRow = function() {

		var ontoSelected = $("#onto option:selected").text();
		var levelSelected = $("#accessLevel option:selected").text();

		if (ontoSelected === "") {
			toastr.error(messagesForms.operations.genOpError,deviceCreateJson.ontologyNotSelected);
			return false;
		}
		
		$('#datamodel_properties > tbody')
				.append(
						'<tr data-ontology="'
								+ ontoSelected
								+ '" data-level="'
								+ levelSelected
								+ '"><td>'
								+ ontoSelected
								+ '</td><td >'
								+ levelSelected
								+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="DeviceCreateController.removeOntology(this)"><i class="icon-delete"></i></span></td></tr>');
		$(".onto select option:selected").remove();
		$('.onto').selectpicker('refresh');
		showHideImageTableOntology();
		toastr.success(messagesForms.operations.genOpSuccess,deviceCreateJson.messages.ontologyAdded);
	}
	
	function showHideImageTableOntology(){
		if(typeof $('#datamodel_properties > tbody > tr').length =='undefined' || $('#datamodel_properties > tbody > tr').length == 0){
			$('#imageNoElementsOnTable').show();
		}else{
			$('#imageNoElementsOnTable').hide();
		}
		
	}
	
	var updateOntologies = function(ontologies) {
		var selectedOntologies = JSON.parse(ontologies);
		if (selectedOntologies !== null && selectedOntologies.length > 0) {
			for (var i = 0; i < selectedOntologies.length; i++) {
				var onto = selectedOntologies[i];
				
				$('#datamodel_properties > tbody')
						.append(
								'<tr data-ontology="'
										+ onto.id
										+ '" data-level="'
										+ onto.access
										+ '"><td>'
										+ onto.id
										+ '</td><td >'
										+ onto.access
										+ '</td><td class="icon" style="white-space: nowrap"><span class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" data-container="body" data-placement="bottom" th:title="#{gen.deleteBtn}" th:data-original-title="#{gen.deleteBtn}" onclick="DeviceCreateController.removeOntology(this)" th:text="#{device.ontology.remove}"><i class="icon-delete"></i></span></td></tr>');
			}
			$(".onto select option:selected").remove();
			$('.onto').selectpicker('refresh');
			showHideImageTableOntology();
		}

	}

	var removeOntology = function(row) {
		var ontoSelected = row.parentElement.parentElement.firstElementChild.innerHTML;
		$("#onto").append(
				'<option value="' + ontoSelected + '">' + ontoSelected
						+ '</option>');
		sortOntologies();
		row.parentElement.parentElement.remove();
		showHideImageTableOntology();
		toastr.success(messagesForms.operations.genOpSuccess,deviceCreateJson.messages.ontologyRemoved);
		
	}

	var validateOntologies = function() {
		var listOntology = [];
		
		$("#datamodel_properties tbody tr").each(function(tr) {
			listOntology.push({
				id : this.dataset.ontology,
				access : this.dataset.level
			});
		});
		$("#parameter_clientPlatformOntologies").val(
				JSON.stringify(listOntology));

		return listOntology;
	}

	var generateToken = function() {
		
		var selectedDevice = $("#identification").val();
		var request = {
			deviceIdentification : selectedDevice
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		var url = "/controlpanel/devices/generateToken";
		if(multitenancyEnabled === 'true' && $('#tenants').val()!=null ){
			url+='?tenant='+$('#tenants').val();
		}
		
		$.ajax({
			url : url,
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (data.ok) {
					refreshTokens(selectedDevice);
					toastr.success(messagesForms.operations.genOpSuccess,'');
				} else {
					toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceTokenCreateError);
					return false;
				}
			},
			error : function(data, status, er) {
				toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceTokenCreateError);
				return false;
			}
		});
	}

	var updateTokens = function(tokens) {

		var selectedtokens = JSON.parse(tokens);
		$('#datamodel_tokens > tbody').empty();
		if (selectedtokens !== null && selectedtokens.length > 0) {
			for (var i = 0; i < selectedtokens.length; i++) {
				var token = selectedtokens[i];
				var checked = '';
				var disableButton = "";
				if(deviceCreateReg.actionMode === null){
					disableButton = "disabled"; 
				}
				if (token.active) {
					checked = 'checked';
				}
				var html = '<tr data-id="'
					+ token.id
					+ '"><td>'
					+ token.token
					+ '</td>';
					if(multitenancyEnabled === 'true'){
						if(token.tenant !=null)
							html+='<td>'+token.tenant+'</td>';
						else
							html+='<td>'+currentTenant+'</td>';
					}
					html+='<td><div class="mt-checkbox-list"><div class="switch"><label><input '+disableButton+' id="active" class="form-control no-remove" type="checkbox" name="active" onclick="DeviceCreateController.changeEstatusToken(this);" '+ checked +'/><span class="checkbox-slider round"></span></label></div><div class="inline" style="padding:10px;"></div></div>'
					+ '</td><td class="icon" style="white-space: nowrap"><div class="grupo-iconos"><button   id="deleteBtn" type="button" class="btn btn-xs btn-no-border icon-on-table  tooltips color-red" style="background:rgba(255,255,255, 0.0);" '+disableButton+' name="delete"  value="Remove" onclick="DeviceCreateController.showConfirmDialogDeleteToken(this);" ><i class="icon-delete"></i></button></button></div></td></tr>';
				$('#datamodel_tokens > tbody').append(html);
				$('#parameter_clientPlatformTokens').val(tokens)

			}
		}
		
		$("#parameter_clientPlatformTokens").val(tokens);
	}

	var changeEstatusToken = function(check) {
		var $row = check.closest("tr"); // Find the row
		var selectedToken = $row.dataset.id;
		var selectedStatus = check.checked;
		activateDeactivateToken(selectedToken, selectedStatus);
	}

	var activateDeactivateToken = function(selectedToken, selectedStatus) {
		var data = {
			token : selectedToken,
			active : selectedStatus
		};
		requestData = JSON.stringify(data);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/desactivateToken",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (!data.ok) {
					toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceChangeActiveError);
					return false;
				}else{
					refreshTokens($("#identification").val());
					toastr.success(messagesForms.operations.genOpSuccess,'');
				}
			},
			error : function(data, status, er) {
				toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceChangeActiveError);
				return false;
			}
		});
	}

	var deleteToken = function(tokenToDelete) {
		var $row = tokenToDelete.closest("tr"); // Find the row
		var selectedToken = $row.dataset.id;
		var selectedDevice = $("#identification").val();
		var request = {
			token : selectedToken
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/deleteToken",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				if (data.ok) {
					refreshTokens(selectedDevice);
					toastr.success(messagesForms.operations.genOpSuccess,'');
				} else {
					toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceTokenDeleteError);
					return false;
				}
			},
			error : function(data, status, er) {
				toastr.error(messagesForms.operations.genOpError,deviceCreateJson.deviceTokenDeleteError);
				return false;
			}
		});
	}

	function refreshTokens(selectedDevice) {
		var request = {
			deviceIdentification : selectedDevice
		};
		requestData = JSON.stringify(request);
		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		$.ajax({
			url : "/controlpanel/devices/loadDeviceTokens",
			headers: {
				[csrf_header]: csrf_value
		    },
			type : 'POST',
			dataType : 'json',
			data : requestData,
			contentType : 'application/json',
			mimeType : 'application/json',
			success : function(data) {
				updateTokens(JSON.stringify(data));
			},
			error : function(data, status, er) {
				toastr.error(messagesForms.operations.genOpError,'');
			}
		});
	}

	var deleteDeviceConfirmation = function() {

		var id = deviceCreateReg.deviceId;

		// no Id no fun!
		if (!id) {
			toastr.error(messagesForms.operations.genOpError,'NO DEVICE-FORM SELECTED!');
			return false;
		}

		// call Confirm
		showConfirmDeleteDialog(id);
	}

	var showConfirmDeleteDialog = function(id) {

		// i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.deviceConfirm;
		var Title = headerReg.deviceDelete;

		var csrf_value = $("meta[name='_csrf']").attr("content");
		var csrf_header = $("meta[name='_csrf_header']").attr("content"); 
		
		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-outline blue dialog',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-primary',
					action : function() {
						console.log(id);
						$.ajax({
							url : '/controlpanel/devices/' + id,
							headers: {
								[csrf_header]: csrf_value
						    },
							type : 'DELETE',
							success : function(result) {
								console.log(result.responseText);
								navigateUrl('/controlpanel/devices/list');
							},
						    error: function(result){
						    	console.log(result.responseText);
						    	toastr.error(messagesForms.operations.genOpError,result.responseText);
						    }
						});
					}
				}
				
			}
		});
	}

	// CONFIRM DIALOG DELETE TOKEN
	var showConfirmDialogDeleteToken = function(data) {

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = deviceCreateJson.deleteTokenConfirm;
		var Title = deviceCreateJson.deleteTokenDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title : Title,
			theme : 'light',
			columnClass : 'medium',
			content : Content,
			draggable : true,
			dragWindowGap : 100,
			backgroundDismiss : true,
			buttons : {
				close : {
					text : Close,
					btnClass : 'btn btn-outline blue dialog',
					action : function() {
					} // GENERIC CLOSE.
				},
				remove : {
					text : Remove,
					btnClass : 'btn btn-primary',
					action : function() {
						deleteToken(data);
					}
				}
			}
		});
	}

	// CONTROLLER PUBLIC FUNCTIONS
	return {
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load : function(Data) {
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return deviceCreateReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init : function() {
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			handleValidation();
			initTemplateElements();
			if(deviceCreateReg.ontologyId != null){
				$('#onto').val(deviceCreateReg.ontologyId).change();
			}

		},
		// REDIRECT
		go : function(url) {
			logControl ? console.log(LIB_TITLE + ': go()') : '';
			navigateUrl(url);
		},
		cancel: function(id,url){
			logControl ? console.log(LIB_TITLE + ': cancel()') : '';			
			freeResource(id,url);
		},
		// DELETE DEVICE
		deleteDevice : function(id) {
			logControl ? console.log(LIB_TITLE + ': deleteDevice()') : '';
			deleteDeviceConfirmation(id);
		},
		// JSON SCHEMA VALIDATION
		validateOntologies : function() {
			validateOntologies();
		},
		addMetainfo : function() {
			addMetainfo();
		},
		addOntologyRow : function() {
			addOntologyRow();
		},
		removeOntology : function(row) {
			removeOntology(row);
		},
		generateToken : function() {
			generateToken();
		},
		changeEstatusToken : function(data) {
			changeEstatusToken(data);
		},
		showConfirmDialogDeleteToken : function(data) {
			showConfirmDialogDeleteToken(data);
		},
		deleteDeviceConfirmation : function(data) {
			deleteDeviceConfirmation(data);
		}

	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {

	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	DeviceCreateController.load(deviceCreateJson);

	// AUTO INIT CONTROLLER.
	DeviceCreateController.init();
});
