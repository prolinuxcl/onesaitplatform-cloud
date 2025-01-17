var HeaderController = function() {

	// DEFAULT PARAMETERS, VAR, CONSTS. 
	var APPNAME = 'Smart4Cities Control Panel'; 
	var LIB_TITLE = 'Header Controller';	
	var logControl = 0;     

	// CONTROLLER PRIVATE FUNCTIONS

	// GENERIC HEADER SEARCH
	var searchDocs = function(){		
		logControl ? console.log('searchDocs() Search --> '+ $("#search-query").val()) : '';

		// NOT-AVAILABLE 
		$.alert({title: 'onesait Platform Search:', theme: 'light' ,content: 'FUNCTIONALITY NOT-AVAILABLE!'}); return false;

		var search = $("#search-query").val();
		var url = "/console/api/rest/searchDocs/"+search;
		var settings = {"async": true, "url": url, "method": "GET", "headers": {"cache-control": "no-cache"} };

		// llamada para la búsqueda
		$.ajax(settings).done(function (response) {
			hideDocPost();
			if( !Array.isArray(response) ){
				showErrorDialog();
				return;
			}

			// total resultados obtenidos.
			$("#docs-count").text(response.length);

			blogResults = response.filter(function(f){ return f.type=="blog";});
			docsResults = response.filter(function(f){ return f.type=="doc";});

			// HTML de salida.
			var html = "";
			// BLOGS
			if( blogResults.length > 0 ){
				$("#blog-content-title").show();
				for ( var i = 0; i < blogResults.length; i++){
					var doc = blogResults[i];
					categorias = doc.categoria.join(" ");
					// TO-DO: ajustar css.
					html += "<div style='padding: 2px;margin-bottom: 10px;padding-bottom: 10px;' class='col-md-4 col-lg-3 "+ categorias.toLowerCase() +"'>"
					+ "<div class='search-card'>"
					+ "<div class='search-card-title'>"
					+ "<a onClick='javascript:showDocPost(\""+ doc.link +"\")'><span class='glyphicon glyphicon-blog'></span>"+ doc.title +"</a>"
					+ "</div>"
					+ "<div class='search-card-body'>"
					if (doc.imageUrl) { html +="<img style='width: 100%;;' src="+ doc.imageUrl +"></img>"}; 

					html += "<p>"+ doc.content +"</p>"
					+ "</div>"
					+ "<div class='search-card-foot'>"
					+ "<span class='glyphicon glyphicon-time'></span>"+new Date(doc.date).toLocaleDateString()+""
					+ "<span class='pull-right glyphicon glyphicon-new-window' onClick='javascript:window.open(\""+ doc.link +"\", \"_blank\")'></span>"
					+ "</div></div>"
					+ "</div>"
				}
			}
			else{
				// NO BLOGS
				$("#blog-content-title").hide();
			}

			// ADD HTML RESULT. 		
			$('#blog-content').html(html);

			// DOCS.
			html = "<ul class='searchdoc'>";
			if( docsResults.length > 0){
				$("#docs-content-title").show();
				for (var i = 0; i < docsResults.length; i++){
					var doc = docsResults[i];
					categorias = doc.categoria.join(" ");
					// TO-DO: ajustar css.
					html += "<li class='"+categorias.toLowerCase()+"'>"
					+ "<a onClick='javascript:showDocPost(\""+ doc.link +"\")'><span class='glyphicon glyphicon-book'></span> "+ doc.title +"</a>"
					+ "<br><span>"+ doc.content +"</span>"
					+ "</li>";
				}
			}
			else{
				// NO DOCS
				$("#docs-content-title").hide();
			}

			// ADD HTML RESULT.
			html += "</ul>";
			$('#docs-content').html(html);
			$('#modalDocs').modal();
			$(".modal-backdrop").hide()
		});
	}

	// SHOW SEARCH DOCS
	var showDocPost = function(url){		
		logControl ? console.log('showDocPost()...') : '';

		$("#result-show-content").html("<iframe id='map-iframe' width='100%' height='100%' frameborder=0 scrolling=no" + "marginheight=0 marginwidth=0 src='" + url +"'></iframe>");
		$("#modalDocs-result-show").show();
		$("#btn-search-back").show();
		$("#modalDocs-content").hide();		
	}

	// HIDE SEARCH DOCS
	var hideDocPost = function(){
		logControl ? console.log('hideDocPost()...') : '';

		$("#modalDocs-result-show").hide();
		$("#btn-search-back").hide();
		$("#modalDocs-content").show();				
	}

	// GENERIC-CONFIRM-DIALOG
	var showConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.genericConfirm;
		var Title = headerReg.titleConfirm;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {				
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!',theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// CONFIG-CONFIRM-DIALOG
	var showConfigurationConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.configurationConfirm;
		var Title = headerReg.configurationDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}
	
	// DATAMODEL-CONFIRM-DIALOG
	var showDataModelConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.dataModelConfirm;
		var Title = headerReg.dataModelDelete;

		// datamodel-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},				
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// CONFIG-CONFIRM-DIALOG
	var showScheduledSearchConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.scheduledSearchConfirm;
		var Title = headerReg.scheduledSearchDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light' , content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// TWITTERLISTENING-CONFIRM-DIALOG
	var showTwitterListeningConfirmDialog = function(formId){		
		logControl ? console.log('showConfirmDialogTwitterlistening()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.twitterListeningConfirm;
		var Title = headerReg.scheduledSearchDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			type: 'red',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}

	// ONTOLOGY-CONFIRM-DIALOG
	var showConfirmDialogOntologia = function(formId, ontologyId){		
		logControl ? console.log('showConfirmDialogOntologia()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.ontologyConfirm;
		var Title = headerReg.ontologyDelete;
		
		$.get("/controlpanel/ontologies/getResourcesAssociated/" + ontologyId).done(
				function(data){
					console.log('getResourcesAssociated() -> ok');
					if (data.rtdbDatasource.length > 0 && data.rtdbDatasource[0] === "PRESTO") {
						Content = '<label class="mt-checkbox control-label" data-trigger="hover" data-placement="top" data-container="body">' +
							'<div class="inline font-xs"> ' + headerReg.historicalOntologyDeleteData + '</div>' +
							'<input id="deleteData" name="deleteData" type="checkbox" class="form-control"/>' +
							'<span></span></label>';
						Content += '<br>' + headerReg.historicalOntologyConfirm;
					}
					if(data.apis.length > 0) {
						Content += "<br><b> APIs: </b>";
						for(var i=0; i<data.apis.length; i++){
							Content += "<br>" + data.apis[i];
						}
					}
					if(data.datasources.length > 0) {
						Content += "<br><b> Datasources: </b>";
						for(var i=0; i<data.datasources.length; i++){
							Content += "<br>" + data.datasources[i];
						}
					}
					if(data.layers.length > 0) {
						Content += "<br><b> Layers: </b>";
						for(var i=0; i<data.layers.length; i++){
							Content += "\n" + data.layers[i];
						}
					}
					if(data.subscriptions.length > 0) {
						Content += "<br><b> Subscriptions: </b>";
						for(var i=0; i<data.subscriptions.length; i++){
							Content += "<br>" + data.subscriptions[i];
						}
					}
					if(data.clients.length > 0) {
						Content += "<br><b> Digital Clients: </b>";
						for(var i=0; i<data.clients.length; i++){
							Content += "<br>" + data.clients[i];
						}
					}
					$.confirm({
						title: Title,
						theme: 'light',			
						columnClass: 'medium',
						content: Content,
						draggable: true,
						dragWindowGap: 100,
						backgroundDismiss: true,
						buttons: {
							close: {
								text: Close,
								btnClass: 'btn btn-outline blue dialog',
								action: function (){} //GENERIC CLOSE.		
							},
							remove: {
								text: Remove,
								btnClass: 'btn btn-primary',
								action: function(){ 
									if ( document.forms[formId] ) { 
										if (data.rtdbDatasource.length > 0 && data.rtdbDatasource[0] === "PRESTO") {
											var action = $('#'+formId).attr('action');
											var checkDeleteData = $('#deleteData').is(':checked');
											$('#'+formId).attr('action', action + '/data/' + checkDeleteData);
										}
										document.forms[formId].submit(); 
									} else { 
										$.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); 
									}
								}
							}
						}
					});
				}
			).fail(
				function(e){
					console.error("Error getResourcesAssociated", e);
				}
			)	

		// jquery-confirm DIALOG SYSTEM.
	}
	
	// LAYER-CONFIRM-DIALOG
	var showConfirmDialogLayer = function(formId){		
		logControl ? console.log('showConfirmDialogLayer()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.processConfirm;
		var Title = headerReg.processDelete;	

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// LAYER-CONFIRM-DIALOG
	var showConfirmDialogProcess = function(formId){		
		logControl ? console.log('showConfirmDialogProcess()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.processConfirm;
		var Title = headerReg.processDelete;	

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// VIEWER-CONFIRM-DIALOG
	var showConfirmDialogViewer = function(formId){		
		logControl ? console.log('showConfirmDialogViewer()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.viewerConfirm;
		var Title = headerReg.viewerDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// SUBSCRIPTION-CONFIRM-DIALOG
	var showConfirmDialogSubscription = function(formId){		
		logControl ? console.log('showConfirmDialogSubscription()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.subscriptionConfirm;
		var Title = headerReg.subscriptionDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// CATEGORY-CONFIRM-DIALOG
	var showConfirmDialogCategory = function(formId){		
		console.log('showConfirmDialogCategory()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.categoryConfirm;
		var Title = headerReg.categoryDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	var showConfirmDialogSub = function(formId,subcategoryId){		
		console.log('showConfirmDialogSub()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.categoryConfirm;
		var Title = headerReg.categoryDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { 
							
							$('#'+formId).attr('action', '/controlpanel/subcategories/' + subcategoryId);
							document.forms[formId].submit(); 
						} else { 
							$.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); 
						}
					}
				}
			}
		});

	}
	
	var showConfirmDialogCat = function(formId,categoryId){		
		console.log('showConfirmDialogSub()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.categoryConfirm;
		var Title = headerReg.categoryDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { 
							
							$('#'+formId).attr('action', '/controlpanel/categories/' + categoryId);
							document.forms[formId].submit(); 
						} else { 
							$.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); 
						}
					}
				}
			}
		});

	}
	
	var showConfirmDialogModel = function(formId){		
		console.log('showConfirmDialogModel()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.modelConfirm;
		var Title = headerReg.modelDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	var showConfirmDialogSubcategory = function(formId){		
		console.log('showConfirmDialogSubcategory()...');

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.subcategoryConfirm;
		var Title = headerReg.subcategoryDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// ONTOLOGY-CONFIRM-DIALOG
	var showConfirmDialogVirtualDatasource = function(formId){		
		logControl ? console.log('showConfirmDialogVirtualDatasource()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.virtualDatasourceConfirm;
		var Title = headerReg.virtualDatasourceDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// DIGITALTWINTYPE-CONFIRM-DIALOG
	var showConfirmDialogDigitalTwinType = function(formId){		
		logControl ? console.log('showConfirmDialogDigitalTwinType()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.digitalTwinTypeConfirm;
		var Title = headerReg.digitalTwinTypeDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}
	
	// DIGITALTWINdevice-CONFIRM-DIALOG
	var showConfirmDialogDigitalTwinDevice = function(formId){		
		logControl ? console.log('showConfirmDialogDigitalTwinDevice()...') : '';

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var Content = headerReg.digitalTwinDeviceConfirm;
		var Title = headerReg.digitalTwinDeviceDelete;		

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',			
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});

	}

	// USER-CONFIRM-DIALOG
	var showConfirmDialogUsuario = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.userConfirm;
		var Title = headerReg.userDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!',theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// DATASOURCE-CONFIRM-DIALOG
	var showConfirmDialogDatasource = function(formId, gadgetlist){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetDatasourceConfirm;
		if (gadgetlist.length>0) {
			Content = Content + '<BR>' + headerReg.gadgetDatasourceGadgetWarningConfirm + '<BR><BR>';
		    for( var i = 0; i < gadgetlist.length; i++ ){
				Content = Content + gadgetlist[i] + "  ";
			}
		}
		var Title = headerReg.gadgetDatasourceDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}

	// DATASOURCE-NAVIGATION-CONFIRM-DIALOG
	var showNavigationConfirmDialogDatasource = function(url, gadgetlist){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Edit = headerReg.btnEditar;
		var Content = "";
		if (gadgetlist.length>0) {
			Content = Content + headerReg.gadgetDatasourceGadgetWarningConfirm + '<BR><BR>';
		    for( var i = 0; i < gadgetlist.length; i++ ){
				Content = Content + gadgetlist[i] + "  ";
			}
		}
		var Title = headerReg.gadgetDatasourceGadgetWarningEdit;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				accept: {
					text: Edit,
					btnClass: 'btn btn-primary',
					action: function(){ 
						window.location.href = url;
					}											
				}
			}
		});
	}
	
	// GADGET-CONFIRM-DIALOG
	var showConfirmDialogGadget = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetConfirm;
		var Title = headerReg.gadgetDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// GADGET-CONFIRM-DIALOG
	var showConfirmDialogGadgetTemplate = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetTemplateConfirm;
		var Title = headerReg.gadgetTemplateDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	// GADGET-CONFIRM-DIALOG
	var showConfirmDialogInstance = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.gadgetInstanceConfirm;
		var Title = headerReg.gadgetInstanceDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	// DASHBOARD-CONF-CONFIRM-DIALOG
	var showConfirmDialogDashboardConf = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.dashboardConfConfirm;
		var Title = headerReg.dashboardConfDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	
	
	// DASHBOARDS-CONFIRM-DIALOG
	var showConfirmDialogDashboard = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.dashboardConfirm;
		var Title = headerReg.dashboardDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}

	// DEVICE-CONFIRM-DIALOG
	var showConfirmDialogDevice = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.deviceConfirm;
		var Title = headerReg.deviceDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}	
	
	var showConfirmDialogFlowDomain = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.flowDomainConfirm;
		var Title = headerReg.flowDomainDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	// KSQL-FLOW-CONFIRM-DIALOG
	var showConfirmDialogKsqlFlow = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.ksqlFlowConfirm;
		var Title = headerReg.ksqlFlowDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}
	
	//KSQL-RELATION-CONFIRM-DIALOG
	var showConfirmDialogKsqlRelation = function(ksqlRelationId, deletionCallback){

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.ksqlRelationConfirm;
		var Title = headerReg.ksqlRelationDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						deletionCallback(ksqlRelationId);
					}											
				}
			}
		});
	}

	// QUERY TEMPLATE-CONFIRM-DIALOG
	var showConfirmDialogQueryTemplate = function(formId){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.queryTemplateConfirm;
		var Title = headerReg.queryTemplateDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}											
				}
			}
		});
	}

	// INTERNATIONALIZATION-CONFIRM-DIALOG
	var showInternationalizationConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.internationalizationConfirm;
		var Title = headerReg.internationalizationDelete;

		// internationalization-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},				
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}

	// MARKET-ASSETS-CONFIRM-DIALOG
	var showMarketAssetConfirmDialog = function(formId){

		// i18 labels
		var Remove = headerReg.btnEliminar;
		var Close = headerReg.btnCancelar;
		var	Content = headerReg.marketAssetConfirm;
		var Title = headerReg.marketAssetDelete;

		// market-assets-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},				
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						if ( document.forms[formId] ) { document.forms[formId].submit(); } else { $.alert({title: 'ERROR!', theme: 'light', content: 'NO FORM SELECTED!'}); }
					}
				}
			}
		});		
	}
	
// WEBPROYECT-CONFIRM-DELETE-DIALOG
	var showConfirmDeleteDialogWebProject= function(url){	

		//i18 labels
		var Close = headerReg.btnCancelar;
		var Remove = headerReg.btnEliminar;
		var Content = headerReg.webprojectConfirm;
		var Title = headerReg.webprojectDelete;

		// jquery-confirm DIALOG SYSTEM.
		$.confirm({
			title: Title,
			theme: 'light',
			columnClass: 'medium',
			content: Content,
			draggable: true,
			dragWindowGap: 100,
			backgroundDismiss: true,
			buttons: {
				close: {
					text: Close,
					btnClass: 'btn btn-outline blue dialog',
					action: function (){} //GENERIC CLOSE.		
				},
				remove: {
					text: Remove,
					btnClass: 'btn btn-primary',
					action: function(){ 
						 window.location.href = url; 
					}											
				}
			}
		});
	}

	// SERVER ERRORS-DIALOG
	var errors = function(){		
		var Close = headerReg.btnCancelar;
		if ( headerReg.errores !== null ){	
			var htmlContent= "";
			headerReg.errores.split("\n").forEach(function(error){htmlContent+='<p>'+error+'</p>'});
			// jquery-confirm DIALOG SYSTEM.
			toastr.error(messagesForms.operations.genOpError,htmlContent);
			} else { logControl ? console.log('|---> errors() -> NO ERRORS FROM SERVER.') : ''; }		
	}

	// SERVER INFORMATION-DIALOG (ERRORS)
	var information = function(){		
		var Close = headerReg.btnCancelar;
		if (headerReg.informacion !== null ){			
			// jquery-confirm DIALOG SYSTEM.			
			toastr.info(messagesForms.operations.notification,headerReg.informacion);	
		}
		else { logControl ? console.log('|---> information() -> NO ERROR INFO.') : ''; }		
	}	


	// CONTROLLER PUBLIC FUNCTIONS 
	return{

		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER.
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return headerReg = Data;
		},

		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';

			// CALL ERRORS
			errors();			
			// CALL INFO
			information();
			
			// adjust breadcrum
			$('.page-bar').insertAfter($('.page-logo'));
			$('.page-bar').fadeIn();
			$( "#confluence-query" ).on( "keydown", function(event) {
			      if(event.which == 13)
			    	  gotoSearch();
			    	  
			});

		},

		// SEARCH
		search: function(){
			logControl ? console.log(LIB_TITLE + ': search()') : '';
			searchDocs();			
		},

		// SERVER-ERROR CONTROL-DIALOG
		showErrorDialog: function(message){		
			logControl ? console.log('showErrorDialog()...') : '';
			var Close = headerReg.btnCancelar;

			// jquery-confirm DIALOG SYSTEM.
			$.confirm({
				title: 'Error',
				theme: 'light',
				content: message,
				draggable: true,
				dragWindowGap: 100,
				backgroundDismiss: true,
				buttons: {				
					close: {
						text: Close,
						btnClass: 'btn btn-outline blue dialog',
						action: function (){} //GENERIC CLOSE.		
					}
				}
			});			
		},

		// GENERIC-CONFIRM-DIALOG
		showConfirmDialog : function(formId){		
			logControl ? console.log('showConfirmDialog()...') : '';
			showConfirmDialog(formId);
		},

		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogOntologia : function(formId, ontologyId){		
			logControl ? console.log('showConfirmDialogOntologia()...') : '';
			showConfirmDialogOntologia(formId, ontologyId);
		},
		// LAYER-CONFIRM-DIALOG
		showConfirmDialogLayer : function(formId){		
			logControl ? console.log('showConfirmDialogLayer()...') : '';
			showConfirmDialogLayer(formId);
		},
		// LAYER-CONFIRM-DIALOG
		showConfirmDialogViewer : function(formId){		
			logControl ? console.log('showConfirmDialogViewer()...') : '';
			showConfirmDialogViewer(formId);
		},
		// SUBSCRIPTION-CONFIRM-DIALOG
		showConfirmDialogSubscription : function(formId){		
			logControl ? console.log('showConfirmDialogSubscription()...') : '';
			showConfirmDialogSubscription(formId);
		},
		// CATEGORY-CONFIRM-DIALOG
		showConfirmDialogCategory : function(formId){		
			logControl ? console.log('showConfirmDialogCategory()...') : '';
			showConfirmDialogCategory(formId);
		},
		
		// SINGLE-CATEGORY-CONFIRM-DIALOG
		showConfirmDialogCat : function(formId,categoryId){		
			logControl ? console.log('showConfirmDialogCat()...') : '';
			showConfirmDialogCat(formId,categoryId);
		},
		// SUBCATEGORY-CONFIRM-DIALOG
		showConfirmDialogSub : function(formId,subcategoryId){		
			logControl ? console.log('showConfirmDialogSub()...') : '';
			showConfirmDialogSub(formId,subcategoryId);
		},
		
		
		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogModel : function(formId){		
			logControl ? console.log('showConfirmDialogModel()...') : '';
			showConfirmDialogModel(formId);
		},
		// ONTOLOGY-CONFIRM-DIALOG
		showConfirmDialogSubcategory : function(formId){		
			logControl ? console.log('showConfirmDialogSubcategory()...') : '';
			showConfirmDialogSubcategory(formId);
		},
		// DATASOURCE VIRTUAL-CONFIRM-DIALOG
		showConfirmDialogVirtualDatasource : function(formId){		
			logControl ? console.log('showConfirmDialogVirtualDatasource()...') : '';
			showConfirmDialogVirtualDatasource(formId);
		},
		// DIGITALTWINTYPE-CONFIRM-DIALOG
		showConfirmDialogDigitalTwinType : function(formId){		
			logControl ? console.log('showConfirmDialogDigitalTwinType()...') : '';
			showConfirmDialogDigitalTwinType(formId);
		},
		// DIGITALTWINTYPE-CONFIRM-DIALOG
		showConfirmDialogDigitalTwinDevice : function(formId){		
			logControl ? console.log('showConfirmDialogDigitalTwinDevice()...') : '';
			showConfirmDialogDigitalTwinDevice(formId);
		},
		showTwitterListeningConfirmDialog: function(formId){		
			logControl ? console.log('showTwitterListeningConfirmDialog()...') : '';
			showTwitterListeningConfirmDialog(formId);
		},
		// CONFIGURATION-CONFIRM-DIALOG
		showConfigurationConfirmDialog : function(formId){		
			logControl ? console.log('showConfigurationConfirmDialog()...') : '';
			showConfigurationConfirmDialog(formId);
		},
		// DATAMODEL-CONFIRM-DIALOG
		showDataModelConfirmDialog : function(formId){		
			logControl ? console.log('showDataModelConfirmDialog()...') : '';
			showDataModelConfirmDialog(formId);
		},
		// SCHEDULEDSEARCH-CONFIRM-DIALOG
		showScheduledSearchConfirmDialog : function(formId){		
			logControl ? console.log('showScheduledSearchConfirmDialog()...') : '';
			showScheduledSearchConfirmDialog(formId);
		},

		// USER-CONFIRM-DIALOG
		showConfirmDialogUsuario : function(formId){		
			logControl ? console.log('showConfirmDialogUsuario()...') : '';
			showConfirmDialogUsuario(formId);
		},
		
		// DATASOURCE-CONFIRM-DIALOG
		showConfirmDialogDatasource : function(formId, gadgetlist){		
			logControl ? console.log('showConfirmDialogDatasource()...') : '';
			showConfirmDialogDatasource(formId, gadgetlist);
		},
		
		// DATASOURCE-NAVIGATION-CONFIRM-DIALOG
		showNavigationConfirmDialogDatasource : function(url, gadgetlist){		
			logControl ? console.log('showNavigationConfirmDialogDatasource()...') : '';
			showNavigationConfirmDialogDatasource(url, gadgetlist);
		},
		
		// DASHBOARD-CONFIRM-DIALOG
		showConfirmDialogDashboard : function(formId){		
			logControl ? console.log('showConfirmDialogDashboard()...') : '';
			showConfirmDialogDashboard(formId);
		},
		
		// GADGET-CONFIRM-DIALOG
		showConfirmDialogGadget : function(formId){		
			logControl ? console.log('showConfirmDialogGadget()...') : '';
			showConfirmDialogGadget(formId);
		},
		// INSTANCE-CONFIRM-DIALOG
		showConfirmDialogInstance : function(formId){		
			logControl ? console.log('showConfirmDialogInstance()...') : '';
			showConfirmDialogInstance(formId);
		},
		
		showConfirmDeleteDialogWebProject : function(formId){		
			logControl ? console.log('showConfirmDeleteDialogWebProject()...') : '';
			showConfirmDeleteDialogWebProject(formId);
		},
		showConfirmDialogDashboardConf : function(formId){		
			logControl ? console.log('showConfirmDialogDashboardConf()...') : '';
			showConfirmDialogDashboardConf(formId);
		},
			
		showConfirmDialogDevice: function(formId){		
			logControl ? console.log('showConfirmDialogDevice()...') : '';
			showConfirmDialogDevice(formId);
		},// GADGET-CONFIRM-DIALOG
		showConfirmDialogGadgetTemplate : function(formId){		
			logControl ? console.log('showConfirmDialogGadgetTemplate()...') : '';
			showConfirmDialogGadgetTemplate(formId);
		},// FLOW-DOMAIN-CONFIRM-DIALOG
		showConfirmDialogFlowDomain : function(formId){		
			logControl ? console.log('showConfirmDialogKsqlFlow()...') : '';
			showConfirmDialogFlowDomain(formId);
		},// KSQL-FLOW-CONFIRM-DIALOG
		showConfirmDialogKsqlFlow : function(formId){		
			logControl ? console.log('showConfirmDialogKsqlFlow()...') : '';
			showConfirmDialogKsqlFlow(formId);
		},	// KSQL-RELATION-CONFIRM-DIALOG
		showConfirmDialogKsqlRelation : function(ksqlRelationId, deletionCallback){		
			logControl ? console.log('showConfirmDialogKsqlRelation()...') : '';
			showConfirmDialogKsqlRelation(ksqlRelationId, deletionCallback);
		},
		// QUERY TEMPLATE-CONFIRM-DIALOG
		showConfirmDialogQueryTemplate : function(formId){		
			logControl ? console.log('showConfirmDialogQueryTemplate()...') : '';
			showConfirmDialogQueryTemplate(formId);
		},
		// INTERNATIONALIZATION-CONFIRM-DIALOG
		showInternationalizationConfirmDialog : function(formId){		
			logControl ? console.log('showInternationalizationConfirmDialog()...') : '';
			showInternationalizationConfirmDialog(formId);
		},
		// MARKET-ASSET-CONFIRM-DIALOG
		showMarketAssetConfirmDialog : function(formId){		
			logControl ? console.log('showMarketAssetConfirmDialog()...') : '';
			showMarketAssetConfirmDialog(formId);
		},
		// PROCESS-CONFIRM-DIALOG
		showConfirmDialogProcess : function(formId){		
			logControl ? console.log('showConfirmDialogProcess()...') : '';
			showConfirmDialogProcess(formId);
		}
	};
}();
var jseval=this;
function theme(){
	var csrf_value = $("meta[name='_csrf']").attr("content");
	var csrf_header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({
		url : "/controlpanel/themes/getThemeJson",
		type : "GET",
		async : true,
		headers: {
			[csrf_header]: csrf_value
	    },
		success : function(response){
			
			var img64 = response.HEADER_IMAGE;
			var js = response.JS;
			var css = response.CSS;
			if (img64 != null && img64 != ""){
				$('#imagen').append("<img id='headerImg' alt='logo' class='logo-default' src='data:image/jpeg;base64, "+img64+"'/>");
			} else {
				$('#imagen').append("<img id='headerImg' alt='logo'  style='width: 133px;    padding-top: 14px;  margin-left: 12px; '   class='logo-default' src='/controlpanel/static/images/productlogo.svg'/>");
			}
			if(js!=null && js!="" && js.trim().length>0){
				jseval.eval(js);
			}
			if(css!=null && css!="" && css.trim().length>0){
				$('#headerStyle').append(css);
			}
		},
	    error :  function () {
	    	$('#imagen').append("<img id='headerImg' alt='logo' style='width:133px;padding-top: 14px;margin-left: 12px;  ' class='logo-default' src='/controlpanel/static/images/productlogo.svg'/>");
	    	}
	    
	})
	
}
theme();
//AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	HeaderController.load(headerJson);

	// AUTO INIT CONTROLLER.
	HeaderController.init();
});


// CONFLUENCE SEARCH
	function gotoSearch() { 
		var value=document.getElementById("querySearchConfluence").value;
	    var link="https://onesaitplatform.atlassian.net/wiki/dosearchsite.action?cql=siteSearch+~+\""+value+"\"&queryString="+value;
	    var a = document.getElementById("search-confluence");
	    a.href=link;
	    a.target="_blank";
	    a.click();
	}  
	function enterSearch(e) {
	    if (e.keyCode == 13) {
	        gotoSearch();
	    }
	}
// TABS CONTROL
	function handleTabsChange(){
	    $('.tabContainer .option').click(function (ev) {
	        $('.tabContainer').find('.option').removeClass('active');
	        $(ev.currentTarget).addClass('active');
	    });
	}
	
