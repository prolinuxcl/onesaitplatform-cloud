import _ from 'lodash';
import NativePromise from 'native-promise-only';
import Field from '../_classes/field/Field';
import Input from '../_classes/input/Input';
import { eachComponent } from '../../utils/utils';

export default class ButtonComponent extends Field {
  static schema(...extend) {
    return Input.schema({
      type: 'button',
      label: 'Submit',
      key: 'submit',
      input:'false',
      size: 'md',
      leftIcon: '',
      rightIcon: '',
      block: false,
      action: 'submit',
      persistent: false,
      disableOnInvalid: false,
      theme: 'primary',
      dataGridLabel: true,
      redirect:'',
      formCode:'',
      formOid:''
    }, ...extend);
  }

  static get builderInfo() {
    return {
      title: 'Button',
      group: 'basic',
      icon: 'stop',
      documentation: '/userguide/forms/form-components#button',
      weight: 110,
      schema: ButtonComponent.schema()
    };
  }

  constructor(component, options, data) {
    super(component, options, data);
    this.filesUploading = [];
  }

  get defaultSchema() {
    return ButtonComponent.schema();
  }

  get inputInfo() {
    const info = super.elementInfo();
    info.type = 'button';
    info.attr.type = (['submit', 'saveState'].includes(this.component.action)) ? 'submit' : 'button';
    this.component.theme = this.component.theme || 'default';
    info.attr.class = `btn btn-${this.component.theme}`;
    /* if (this.component.size) {
      info.attr.class += ` btn-${this.component.size}`;
    } */
    if (this.component.block) {
      info.attr.class += ' btn-block';
    }
    if (this.component.customClass) {
      info.attr.class += ` ${this.component.customClass}`;
    }
    info.content = this.t(this.component.label, { _userInput: true });
    return info;
  }

  get labelInfo() {
    return {
      hidden: true
    };
  }

  set loading(loading) {
    this.setLoading(this.refs.button, loading);
  }

  get skipInEmail() {
    return true;
  }

  // No label needed for buttons.
  createLabel() {}

  createInput(container) {
    this.refs.button = super.createInput(container);
    return this.refs.button;
  }

  get emptyValue() {
    return false;
  }

  getValue() {
    return this.dataValue;
  }

  get clicked() {
    return this.dataValue;
  }

  get defaultValue() {
    return false;
  }

  get className() {
    let className = super.className;
    className += ' form-group';
    return className;
  }

  get oauthConfig() {
    if (_.has(this, 'root.form.config.oauth') && this.component.oauthProvider) {
      return this.root.form.config.oauth[this.component.oauthProvider];
    }
    // Legacy oauth location.
    if (this.component.oauth) {
      return this.component.oauth;
    }
    return false;
  }

  render() {
    if (this.viewOnly || this.options.hideButtons) {
      this._visible = false;
    }
    return super.render(this.renderTemplate('button', {
      component: this.component,
      input: this.inputInfo,
    }));
  }
  getDataFromPath(data,path) {
    if (!data || !path || path.length === 0) {
      return null;
    }
    const p = path.split('.');
    let elem = JSON.parse(JSON.stringify(data));
    for ( var i=0; i<p.length;i++ ) {
      elem = elem[p[i]];
    }
    return elem;
  }
  attachButton() {
    this.addShortcut(this.refs.button);
    let onChange = null;
    let onError = null;
    if (this.component.action === 'submit') {
      this.on('submitButton', () => {
        this.disabled = true;
      }, true);
      this.on('cancelSubmit', () => {
        this.disabled = false;
      }, true);
      this.on(`submitDone${this.component.key?this.component.key:''}`, (message) => {
        const resultMessage = _.isString(message) ? message : this.t('complete');
        this.loading = false;
        this.disabled = false;
        // this.addClass(this.refs.button, 'btn-success submit-success');
        // this.removeClass(this.refs.button, 'btn-danger submit-fail');
        // this.addClass(this.refs.buttonMessageContainer, 'has-success');
        // this.removeClass(this.refs.buttonMessageContainer, 'has-error');
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
        this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-done');
        this.setContent(this.refs.buttonMessage, resultMessage);
        this.timeout = setTimeout(() => {
          this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-done');
          this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
          this.setContent(this.refs.buttonMessage, '');
        }, 10000);
        if (this.component.formCode && this.component.formCode.trim().length > 0 ) {
          if (window.redirectBy && window.redirectBy === 'events') {
            if ( this.component.formOid && this.component.formOid.trim().length > 0 ) {
              if ( this.component.formOid.replace(/ /g, '').includes('{{') ) {
                try {
                  this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.getDataFromPath( this.parent.submission.data,this.component.formOid.replace(/ /g, '').replace('{{', '').replace('}}', '')) } );
                }
                catch (error) {
                  this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.interpolate(this.component.formOid) } );
                }
              }
              else {
                this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.component.formOid } );
              }
            }
            else {
              this.emit('redirect',{ formcode: this.component.formCode, dataoid:null } );
            }
          }
          else {
          var url = `${window.showformbase}/${this.component.formCode}`;
          if ( this.component.formOid && this.component.formOid.trim().length > 0 ) {
            if ( this.component.formOid.replace(/ /g, '').includes('{{') ) {
              try {
                var pathOid = this.getDataFromPath( this.parent.submission.data,this.component.formOid.replace(/ /g, '').replace('{{', '').replace('}}', ''));
                url = `${url}/${pathOid}`;
              }
              catch (error) {
                url = `${url}/${this.interpolate(this.component.formOid)}`;
              }
            }
            else {
              url= `${url}/${this.component.formOid}`;
            }
          }
          window.location.href = url;
        }
        }
        if ( this.component.redirect && this.component.redirect.trim().length>0 ) {
          if ( this.component.redirect.includes('http') ) {
            window.location.href = this.component.redirect;
          }
          else {
            window.location.href = window.appbase + this.component.redirect;
          }
        }
      }, true);
      this.on('submitError', (message) => {
        const resultMessage = _.isString(message) ? this.t(message) : this.t(this.errorMessage('submitError'));
        this.loading = false;
        this.disabled = false;
        this.hasError = true;
        // this.removeClass(this.refs.button, 'btn-success submit-success');
        // this.addClass(this.refs.button, 'btn-danger submit-fail');
        // this.removeClass(this.refs.buttonMessageContainer, 'has-success');
        // this.addClass(this.refs.buttonMessageContainer, 'has-error');
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-done');
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
        this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
        this.setContent(this.refs.buttonMessage, resultMessage);
        this.timeout = setTimeout(() => {
          this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
          this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
          this.setContent(this.refs.buttonMessage, '');
        }, 10000);
      }, true);

      this.on('fileUploadingStart', (filePromise) => {
        this.filesUploading.push(filePromise);
        this.disabled = true;
        this.setDisabled(this.refs.button, this.disabled);
      }, true);

      this.on('fileUploadingEnd', (filePromise) => {
        const index = this.filesUploading.indexOf(filePromise);
        if (index !== -1) {
          this.filesUploading.splice(index, 1);
        }
        this.disabled = this.shouldDisabled ? true : false;
        this.setDisabled(this.refs.button, this.disabled);
      }, true);

      onChange = (value, isValid) => {
       /* if (value.changed) {
          this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
          this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-done');
        } */
        // this.removeClass(this.refs.button, 'btn-success submit-success');
        if (isValid) {
          // this.removeClass(this.refs.button, 'btn-danger submit-fail');
          if (this.hasError) {
            this.hasError = false;
            /* this.setContent(this.refs.buttonMessage, '');
            this.removeClass(this.refs.buttonMessageContainer, 'has-success');
            this.removeClass(this.refs.buttonMessageContainer, 'has-error'); */
          }
        }
      };
      onError = () => {
        this.hasError = true;
        // this.removeClass(this.refs.button, 'btn-success submit-success');
        // this.addClass(this.refs.button, 'btn-danger submit-fail');
        this.removeClass(this.refs.buttonMessageContainer, 'has-success');
        this.addClass(this.refs.buttonMessageContainer, 'has-error');
        this.setContent(this.refs.buttonMessage, this.t(this.errorMessage('submitError')));
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-done');
        this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
        this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
        this.timeout = setTimeout(() => {
          this.removeClass(this.refs.buttonMessageContainer, 'formio-button-message-container-show-error');
          this.addClass(this.refs.buttonMessageContainer, 'formio-button-message-container-hide');
          this.setContent(this.refs.buttonMessage, '');
        }, 10000);
      };
    }

    if (this.component.action === 'url') {
      this.on('requestButton', () => {
        this.disabled = true;
      }, true);
      this.on('requestDone', () => {
        this.loading = false;
        this.disabled = false;
      }, true);
    }

    this.on('change', (value, flags) => {
      let isValid = value.isValid;
      const isSilent = flags && flags.silent;
      //check root validity only if disableOnInvalid is set and when it is not possible to make submission because of validation errors
      if (flags && flags.noValidate && (this.component.disableOnInvalid || this.hasError)) {
        isValid = flags.rootValidity || (this.root ? this.root.checkValidity(this.root.data, null, null, true) : true);
        flags.rootValidity = isValid;
      }
      this.isDisabledOnInvalid = this.component.disableOnInvalid && (isSilent || !isValid);
      this.disabled = this.shouldDisabled;
      this.setDisabled(this.refs.button, this.disabled);

      if (onChange) {
        onChange(value, isValid);
      }
    }, true);

    this.on('error', () => {
      this.loading = false;
      this.disabled = false;
      if (onError) {
        onError();
      }
    }, true);

    if (this.component.saveOnEnter) {
      this.root.addEventListener(this.root.element, 'keyup', (event) => {
        if (event.keyCode === 13) {
          this.onClick.call(this, event);
        }
      });
    }
    this.addEventListener(this.refs.button, 'click', this.onClick.bind(this));
    this.addEventListener(this.refs.buttonMessageContainer, 'click', () => {
      if (this.refs.buttonMessageContainer.classList.contains('has-error')) {
        if (this.root && this.root.alert) {
          this.scrollIntoView(this.root.alert);
        }
      }
    });

    this.disabled = this.shouldDisabled;
    this.setDisabled(this.refs.button, this.disabled);

    function getUrlParameter(name) {
      name = name.replace(/[[]/, '\\[').replace(/[\]]/, '\\]');
      const regex = new RegExp(`[\\?&]${name}=([^&#]*)`);
      const results = regex.exec(location.search);
      if (!results) {
        return results;
      }
      return decodeURIComponent(results[1].replace(/\+/g, ' '));
    }

    // If this is an OpenID Provider initiated login, perform the click event immediately
    if ((this.component.action === 'oauth') && this.oauthConfig && !this.oauthConfig.error) {
      const iss = getUrlParameter('iss');
      if (iss && (this.oauthConfig.authURI.indexOf(iss) === 0)) {
        this.openOauth(this.oauthConfig);
      }
    }
  }

  get shouldDisabled() {
    return super.shouldDisabled || !!this.filesUploading?.length || this.isDisabledOnInvalid;
  }

  attach(element) {
    this.loadRefs(element, {
      button: 'single',
      buttonMessageContainer: 'single',
      buttonMessage: 'single'
    });

    const superAttach = super.attach(element);
    this.attachButton();
    return superAttach;
  }
  /* eslint-enable max-statements */

  detach(element) {
    if (element && this.refs.button) {
      this.removeShortcut(this.refs.button);
    }
    super.detach();
  }

  filterDataFields() {
    if (this.component.filtersubmission) {
      if (this.component.submissionfieldslist && this.component.submissionfieldslist.length > 0 ) {
        window.submissionfieldslist = { ...this.component.submissionfieldslist };
      }
    }
  }

  onClick(event) {
    this.triggerReCaptcha();
    // Don't click if disabled or in builder mode.
    if (this.disabled || this.options.attachMode === 'builder') {
      return;
    }
    this.dataValue = true;
    if (this.component.action !== 'submit' && this.component.showValidations) {
      this.emit('checkValidity', this.data);
    }
    window.buttonclicked = this.component.key;
    this.filterDataFields();
    switch (this.component.action) {
      case 'saveState':
      case 'submit':
        event.preventDefault();
        event.stopPropagation();
        this.loading = true;
        this.emit('submitButton', {
          state: this.component.state || 'submitted',
          component: this.component,
          instance: this
        });
        break;
      case 'event':
        this.emit(this.interpolate(this.component.event), this.data);
        this.events.emit(this.interpolate(this.component.event), this.data);
        this.emit('customEvent', {
          type: this.interpolate(this.component.event),
          component: this.component,
          data: this.data,
          event: event
        });
        break;
      case 'custom': {
        // Get the FormioForm at the root of this component's tree
        const form = this.getRoot();

        const flattened = {};
        const components = {};

        eachComponent(form.components, (componentWrapper, path) => {
          const component = componentWrapper.component || componentWrapper;
          flattened[path] = component;
          components[component.key] = component;
        }, true);

        this.evaluate(this.component.custom, {
          form,
          flattened,
          components
        });

        this.triggerChange();
        break;
      }
      case 'url':
        this.loading = true;
        this.emit('requestButton', {
          component: this.component,
          instance: this
        });
        this.emit('requestUrl', {
          url: this.interpolate(this.component.url),
          headers: this.component.headers
        });
        break;
      case 'reset':
        this.emit('resetForm');
        break;
      case 'delete':
        this.emit('deleteSubmission');
        break;
      case 'oauth':
        if (this.root === this) {
          console.warn('You must add the OAuth button to a form for it to function properly');
          return;
        }

        // Display Alert if OAuth config is missing
        if (!this.oauthConfig) {
          this.root.setAlert('danger', 'OAuth not configured. You must configure oauth for your project before it will work.');
          break;
        }

        // Display Alert if oAuth has an error is missing
        if (this.oauthConfig.error) {
          this.root.setAlert('danger', `The Following Error Has Occured ${this.oauthConfig.error}`);
          break;
        }

        this.openOauth(this.oauthConfig);

        break;
      case 'redirect':
      if (this.component.formCode && this.component.formCode.trim().length > 0 ) {
        if (window.redirectBy && window.redirectBy === 'events') {
          if ( this.component.formOid && this.component.formOid.trim().length > 0 ) {
            if ( this.component.formOid.replace(/ /g, '').includes('{{') ) {
              try {
                this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.getDataFromPath( this.parent.submission.data,this.component.formOid.replace(/ /g, '').replace('{{', '').replace('}}', '')) } );
              }
              catch (error) {
                this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.interpolate(this.component.formOid) } );
              }
            }
            else {
              this.emit('redirect',{ formcode: this.component.formCode, dataoid:this.component.formOid } );
            }
          }
          else {
            this.emit('redirect',{ formcode: this.component.formCode, dataoid:null } );
          }
        }
        else {
        var url = `${window.showformbase}/${this.component.formCode}`;
        if ( this.component.formOid && this.component.formOid.trim().length > 0 ) {
          if ( this.component.formOid.replace(/ /g, '').includes('{{') ) {
            try {
              var pathOid = this.getDataFromPath( this.parent.submission.data,this.component.formOid.replace(/ /g, '').replace('{{', '').replace('}}', ''));
              url = `${url}/${pathOid}`;
            }
            catch (error) {
              url = `${url}/${this.interpolate(this.component.formOid)}`;
            }
          }
          else {
            url= `${url}/${this.component.formOid}`;
          }
        }
        window.location.href = url;
      }
      }
      if ( this.component.redirect && this.component.redirect.trim().length>0 ) {
        if ( this.component.redirect.includes('http') ) {
          window.location.href = this.component.redirect;
        }
        else {
          window.location.href = window.appbase + this.component.redirect;
        }
      }
      break;
    }
  }

  openOauth(settings) {
    if (!this.root.formio) {
      console.warn('You must attach a Form API url to your form in order to use OAuth buttons.');
      return;
    }

    /*eslint-disable camelcase */
    let params = {
      response_type: 'code',
      client_id: settings.clientId,
      redirect_uri: settings.redirectURI || window.location.origin || `${window.location.protocol}//${window.location.host}`,
      state: settings.state,
      scope: settings.scope
    };
    /*eslint-enable camelcase */

    // Needs for the correct redirection URI for the OpenID
    const originalRedirectUri = params.redirect_uri;

    // Make display optional.
    if (settings.display) {
      params.display = settings.display;
    }

    params = Object.keys(params).map(key => {
      return `${key}=${encodeURIComponent(params[key])}`;
    }).join('&');

    const separator = settings.authURI.indexOf('?') !== -1 ? '&' : '?';
    const url = `${settings.authURI}${separator}${params}`;
    const popup = window.open(url, settings.provider, 'width=1020,height=618');

    const interval = setInterval(() => {
      try {
        const popupHost = popup.location.host;
        const currentHost = window.location.host;
        if (popup && !popup.closed && popupHost === currentHost && popup.location.search) {
          popup.close();
          const params = popup.location.search.substr(1).split('&').reduce((params, param) => {
            const split = param.split('=');
            params[split[0]] = split[1];
            return params;
          }, {});
          if (params.error) {
            alert(params.error_description || params.error);
            this.root.setAlert('danger', params.error_description || params.error);
            return;
          }
          // TODO: check for error response here
          if (settings.state !== params.state) {
            this.root.setAlert('danger', 'OAuth state does not match. Please try logging in again.');
            return;
          }
          // Depending on where the settings came from, submit to either the submission endpoint (old) or oauth endpoint (new).
          let requestPromise = NativePromise.resolve();

          if (_.has(this, 'root.form.config.oauth') && this.root.form.config.oauth[this.component.oauthProvider]) {
            params.provider = settings.provider;
            params.redirectURI = originalRedirectUri;

            // Needs for the exclude oAuth Actions that not related to this button
            params.triggeredBy = this.key;
            requestPromise = this.root.formio.makeRequest('oauth', `${this.root.formio.projectUrl}/oauth2`, 'POST', params);
          }
          else {
            const submission = { data: {}, oauth: {} };
            submission.oauth[settings.provider] = params;
            submission.oauth[settings.provider].redirectURI = originalRedirectUri;

            // Needs for the exclude oAuth Actions that not related to this button
            submission.oauth[settings.provider].triggeredBy = this.key;
            requestPromise = this.root.formio.saveSubmission(submission);
          }
          requestPromise.then((result) => {
              this.root.onSubmit(result, true);
            })
            .catch((err) => {
              this.root.onSubmissionError(err);
            });
        }
      }
      catch (error) {
        if (error.name !== 'SecurityError') {
          this.root.setAlert('danger', error.message || error);
        }
      }
      if (!popup || popup.closed || popup.closed === undefined) {
        clearInterval(interval);
      }
    }, 100);
  }

  focus() {
    if (this.refs.button) {
      this.refs.button.focus();
    }
  }

  triggerReCaptcha() {
    if (!this.root) {
      return;
    }

    let recaptchaComponent;

    this.root.everyComponent((component)=> {
      if ( component.component.type === 'recaptcha' &&
        component.component.eventType === 'buttonClick' &&
        component.component.buttonKey === this.component.key) {
          recaptchaComponent = component;
        }
    });

    if (recaptchaComponent) {
      recaptchaComponent.verify(`${this.component.key}Click`);
    }
  }
}
