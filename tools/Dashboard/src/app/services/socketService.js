(function () {
  'use strict';

  angular.module('dashboardFramework')
    .service('socketService', SocketService);

  /** @ngInject */
  function SocketService($stomp, $log, __env, $timeout, $q) {
      var vm = this;

      vm.stompClient = {};
      vm.hashRequestResponse = {};
      vm.connected = false;
      vm.firstShot = false;
      vm.retrying = false;
      vm.queue = {};
      vm.resetHeartBeatCallback;//call on connection and when some data is received for reset scheduling
      vm.enableListenerCallback;//call on connection and when some data is received for reset scheduling

      $stomp.setDebug(function (args) {
        $log.debug(args)
      });

      $stomp.setDebug(false);

      var errorfn = function(error){
        if (!vm.firstShot && !vm.retrying) {
          vm.retrying = true;
          console.log("Error websockets: " + error + " , try reconnecting in 5000 ms...");
          $stomp.disconnect().then(function () {
            $log.info('Disconnected');
            vm.connected = false;
            $timeout(vm.connect,5000);
          })
        } else {
          //if connected, HeartBeat deals with reconnection
        }
      }

      vm.connect = function(heartBeatCallback,enableListenerCallback){
        if(heartBeatCallback){
          vm.resetHeartBeatCallback = heartBeatCallback;
        }
        if(enableListenerCallback){
          vm.enableListenerCallback = enableListenerCallback;
        }
        $stomp.connect(__env.socketEndpointConnect+ "?" + (sessionStorage.getItem("dashboardEngineOauthtoken")?"oauthtoken=" +sessionStorage.getItem("dashboardEngineOauthtoken"):'anonymous'), [], errorfn, (__env.dashboardEngineProtocol === 'websocket' || __env.dashboardEngineProtocol === 'all')?{ transports: ['websocket']}:{}).then(
          function(frame){
            if(frame.command == "CONNECTED"){
              vm.connected=true;
              console.log('%c DSEngine Websocket Connected    ' + '%c ' + new Date(), 'color: #1e8fff; font-weight:bold; font-size:13px', 'color: #bbb; font-weight:bold; font-size:13px');
              if(vm.heartBeatCallback){
                vm.resetHeartBeatCallback();
              }
              Object.keys(vm.queue).map(
                function(dskey){
                  vm.sendAndSubscribe(vm.queue[dskey], true);
                }
              )
              if(vm.enableListenerCallback){
                vm.enableListenerCallback();
              }
              vm.firstShot=true;
            }
            else{
              console.log("Error websockets, reconnecting... " + new Date())
              $timeout(vm.connect,5000);
            }
          }
        ).catch(
          errorfn
        );
        vm.retrying=false;
      }

      vm.connectAndSendAndSubscribe = function(reqrespList){
        $stomp
          .connect(__env.socketEndpointConnect, [])
          .then(function (frame) {
            for(var reqrest in reqrespList){
              var UUID = generateConnectionUUID();
              vm.hashRequestResponse[UUID] = reqrespList[reqrest];
              vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
                var answerId = headers.destination.split("/").pop();
                vm.hashRequestResponse[answerId].callback(vm.hashRequestResponse[answerId].id,payload);
                // Unsubscribe
                vm.hashRequestResponse[answerId].subscription.unsubscribe();//Unsubscribe
              })
              // Send message
              $stomp.send(__env.socketEndpointSend + "/" + UUID, reqrespList[reqrest].msg)
            }
          })
        };

      vm.sendAndSubscribe = function(datasource,ignoreQueue){
        if(vm.connected && (ignoreQueue || notInQueue(datasource))){
          var UUID = generateConnectionUUID();
          if (!ignoreQueue) {
            addToQueue(datasource);
          }
          vm.hashRequestResponse[UUID] = getFromQueue(datasource);//get array callback from queue
          vm.hashRequestResponse[UUID].subscription = $stomp.subscribe(__env.socketEndpointSubscribe + "/" + UUID, function (payload, headers, res) {
            var answerId = headers.destination.split("/").pop();
            for (var i=0;i < vm.hashRequestResponse[answerId].callbacks.length;i++) {
              vm.hashRequestResponse[answerId].callbacks[i](vm.hashRequestResponse[answerId].id,vm.hashRequestResponse[answerId].type,payload);
            }
            // Unsubscribe
            vm.hashRequestResponse[UUID].subscription.unsubscribe();//Unsubscribe
            removeFromQueue(vm.hashRequestResponse[UUID]);//datasource remove
            function deleteHash(UUID){
              $timeout(
                function(){
                  delete vm.hashRequestResponse[UUID];
                },
                0
              );
            }
            deleteHash(UUID);
            vm.resetHeartBeatCallback();
          })

          // Send message
          var datasourcefinal;
          if (datasource.msg.filter && datasource.msg.filter.length > 0 && datasource.msg.filter[0].id) {
            datasourcefinal = JSON.parse(JSON.stringify(datasource));
            datasourcefinal.msg.filter = datasourcefinal.msg.filter.map(function (d) {
              return d.data[0]
            })
          } else {
            datasourcefinal = datasource
          }
          $stomp.send(__env.socketEndpointSend + "/" + UUID, datasourcefinal.msg)
        }
        else{
          addToQueue(datasource);
        }
      }

      vm.cleanqueue = function(reqrespList){
        vm.queue = {};
      }
      vm.disconnect = function(reqrespList){
        var deferred = $q.defer();
       
        $stomp.disconnect().then(function () {
          $log.info('disconnected');
          vm.connected = false;
          deferred.resolve();
        })
        return deferred.promise;
      }

      //provisional method, could be use hash key
      function generateDatasourceKey(datasource){
        var keyobj = {
          id: datasource.id,
          msg: datasource.msg,
          type: datasource.type          
        }
        return JSON.stringify(keyobj);
      }

      function notInQueue(datasource){
        return !vm.queue.hasOwnProperty(generateDatasourceKey(datasource));
      }

      function addToQueue(datasource){
        var key = generateDatasourceKey(datasource);
        if (datasource.id !== 1 || !(key in vm.queue)) {// 1 is from vm.from
          vm.queue[key] = {};
          vm.queue[key].id = datasource.id;
          vm.queue[key].msg = datasource.msg;
          vm.queue[key].type = datasource.type;
          vm.queue[key].callbacks = [datasource.callback];
        } else {
          vm.queue[key].callbacks.push(datasource.callback);
        }
      }

      function getFromQueue(datasource){
        return vm.queue[generateDatasourceKey(datasource)];
      }

      function removeFromQueue(datasource){
        delete vm.queue[generateDatasourceKey(datasource)];
        if(typeof vm.queue !== 'undefined' && vm.queue!=null && Object.keys(vm.queue).length == 0){          
          window.dispatchEvent(new CustomEvent('dashboardSocketIdle', {}));
        }
      }

      vm.addListenerForHeartbeat = function(callback){
        $stomp.sock.addEventListener("heartbeat",callback);
      }

      vm.isConnected = function(){
        return vm.connected;
      }

      function generateConnectionUUID(){
        var newUUID = (new Date()).getTime() + Math.floor(((Math.random()*1000000)));
        while(newUUID in vm.hashRequestResponse){
          newUUID = generateConnectionUUID();
        }
        return newUUID;
      }
  };
})();
