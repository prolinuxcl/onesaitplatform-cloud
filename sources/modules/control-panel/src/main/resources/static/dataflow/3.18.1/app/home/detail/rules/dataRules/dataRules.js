/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Controller for Data Rules tab.
 */

angular
  .module('dataCollectorApp.home')

  .controller('DataRulesController', ["$scope", "$rootScope", "$modal", "pipelineConstant", "pipelineService", "previewService", "tracking", "trackingEvent", function (
      $scope, $rootScope, $modal, pipelineConstant,
      pipelineService, previewService, tracking, trackingEvent
    ) {
    var stageInstances = $scope.stageInstances;

    angular.extend($scope, {
      showLoading: false,
      filteredDataRules: [],
      streamLabelMap: _.reduce(stageInstances, function(labelMap, stageInstance){
        angular.forEach(stageInstance.outputLanes, function(outputLane, index) {
          labelMap[outputLane] = stageInstance.uiInfo.label + ' Output Stream ' + (index + 1);
        });

        angular.forEach(stageInstance.eventLanes, function(eventLane, index) {
          labelMap[eventLane] = stageInstance.uiInfo.label + ' Event Stream ' + (index + 1);
        });

        return labelMap;
      }, {}),

      /**
       * Callback function for Create New Data Rule button.
       */
      createDataRule: function() {
        if((!$scope.fieldPaths || $scope.fieldPaths.length === 0 ) && $scope.selectedType === pipelineConstant.LINK &&
          !$rootScope.common.isSlaveNode && !$scope.isPipelineRunning && $rootScope.$storage.runPreviewForFieldPaths) {
          updateFieldDataForStage($scope.selectedObject);
        }

        $scope.trackEvent(pipelineConstant.BUTTON_CATEGORY, pipelineConstant.CLICK_ACTION, 'Add Data Rule', 1);

        var modalInstance = $modal.open({
          templateUrl: 'app/home/detail/rules/dataRules/editDataRule.tpl.html',
          controller: 'CreateDataRuleModalInstanceController',
          size: 'lg',
          backdrop: 'static',
          resolve: {
            laneName: function () {
              if($scope.selectedType === pipelineConstant.LINK) {
                return $scope.selectedObject.outputLane || $scope.selectedObject.eventLane;
              } else {
                var firstStage = stageInstances.length ? stageInstances[0] : undefined;
                return firstStage && firstStage.outputLanes && firstStage.outputLanes.length ?
                  firstStage.outputLanes[0] : undefined;
              }
            },
            rulesElMetadata: function() {
              return pipelineService.getGeneralRulesElMetadata();
            },
            alertTextElMetadata: function() {
              return pipelineService.getAlertTextElMetadata();
            },
            fieldPaths: function() {
              return $scope.fieldPaths;
            },
            streamLabelMap: function() {
              return $scope.streamLabelMap;
            }
          }
        });

        modalInstance.result.then(function (newDataRuleDefn) {
          $scope.trackEvent(pipelineConstant.BUTTON_CATEGORY, pipelineConstant.CLICK_ACTION, 'Save Data Rule', 1);
          $scope.pipelineRules.dataRuleDefinitions.push(newDataRuleDefn);
          tracking.mixpanel.track(trackingEvent.RULE_CREATED, {'Pipeline ID': $scope.pipelineConfig.pipelineId});
        }, function () {

        });

      },

      /**
       * Callback function for Edit Data Rule button.
       */
      editDataRule: function(dataRuleDefn, index, $event) {

        if($event) {
          $event.stopPropagation();
        }

        if((!$scope.fieldPaths || $scope.fieldPaths.length === 0) && $scope.selectedType === pipelineConstant.LINK &&
          !$rootScope.common.isSlaveNode && !$scope.isPipelineRunning) {
          updateFieldDataForStage($scope.selectedObject);
        }

        var modalInstance = $modal.open({
          templateUrl: 'app/home/detail/rules/dataRules/editDataRule.tpl.html',
          controller: 'EditDataRuleModalInstanceController',
          size: 'lg',
          backdrop: 'static',
          resolve: {
            dataRuleDefn: function () {
              return angular.copy(dataRuleDefn);
            },
            rulesElMetadata: function() {
              return pipelineService.getGeneralRulesElMetadata();
            },
            alertTextElMetadata: function() {
              return pipelineService.getAlertTextElMetadata();
            },
            fieldPaths: function() {
              return $scope.fieldPaths;
            },
            streamLabelMap: function() {
              return $scope.streamLabelMap;
            }
          }
        });

        modalInstance.result.then(function (newDataRuleDefn) {
          angular.copy(newDataRuleDefn, dataRuleDefn);
        }, function () {

        });
      },

      /**
       * Remove Callback function
       *
       * @param ruleList
       * @param rule
       */
      removeRule: function(ruleList, rule, $event) {
        if($event) {
          $event.stopPropagation();
        }

        var index;

        angular.forEach(ruleList, function(r, ind) {
          if(r.id === rule.id) {
            index = ind;
          }
        });

        if(index !== undefined) {
          ruleList.splice(index, 1);
        }
      },

      /**
       * Returns filtered data rules
       *
       * @returns {*}
       */
      getFilteredDataRules: function() {
        if($scope.selectedType === pipelineConstant.LINK) {
          return _.filter($scope.pipelineRules.dataRuleDefinitions, function(rule) {
            return (rule.lane === $scope.selectedObject.outputLane || rule.lane === $scope.selectedObject.eventLane);
          });
        } else {
          return $scope.pipelineRules.dataRuleDefinitions;
        }
      }
    });



    /**
     * Update Stage Preview Data when stage selection changed.
     *
     * @param edge
     */
    var updateFieldDataForStage = function(edge) {
      if(edge) {

        previewService.getEdgeInputRecordsFromPreview($scope.activeConfigInfo.pipelineId, edge, 1).
          then(function (inputRecords) {
            if(_.isArray(inputRecords) && inputRecords.length) {
              var fieldPaths = [],
                dFieldPaths = [];
              pipelineService.getFieldPaths(inputRecords[0].value, fieldPaths, undefined, undefined, dFieldPaths);
              $scope.fieldPaths = fieldPaths;
              $rootScope.$broadcast('fieldPathsUpdated', fieldPaths, undefined, dFieldPaths);
            }
          },
          function(res) {
            $rootScope.common.errors = [res.data];
          });
      } else {

      }
    };

  }])

  .controller('CreateDataRuleModalInstanceController', ["$scope", "$modalInstance", "$translate", "$timeout", "pipelineService", "laneName", "rulesElMetadata", "fieldPaths", "streamLabelMap", "alertTextElMetadata", function (
    $scope, $modalInstance, $translate, $timeout, pipelineService, laneName, rulesElMetadata, fieldPaths,
    streamLabelMap, alertTextElMetadata
  ) {

    angular.extend($scope, {
      showLoading: false,
      common: {
        errors: []
      },
      dataRuleDefn: {
        id: 'dataRule' + (new Date()).getTime(),
        label: '',
        lane: laneName,
        condition: '',
        samplingPercentage: 5,
        samplingRecordsToRetain: 10,
        alertEnabled: true,
        alertText: '',
        thresholdType: 'COUNT',
        thresholdValue: '100',
        minVolume: 1000,
        sendEmail: false,
        meterEnabled: true,
        enabled: false
      },
      fieldPaths: fieldPaths,
      refreshCodemirror: false,
      streamLabelMap: streamLabelMap,

      getCodeMirrorOptions: function(fieldType) {
        var codeMirrorOptions = {
          dictionary: (fieldType === 'condition' ? rulesElMetadata : alertTextElMetadata),
          extraKeys: {
            'Tab': false,
            'Ctrl-Space': 'autocomplete'
          }
        };

        if(fieldType === 'condition') {
          $timeout(function() {
            $scope.refreshCodemirror = true;
          });
        }

        return angular.extend({}, pipelineService.getDefaultELEditorOptions(), codeMirrorOptions);
      },

      save : function () {
        $modalInstance.close($scope.dataRuleDefn);
      },
      cancel : function () {
        $modalInstance.dismiss('cancel');
      }
    });

    $scope.$broadcast('show-errors-check-validity');
  }])

  .controller('EditDataRuleModalInstanceController', ["$scope", "$modalInstance", "$translate", "pipelineService", "$timeout", "dataRuleDefn", "rulesElMetadata", "fieldPaths", "streamLabelMap", "alertTextElMetadata", function (
    $scope, $modalInstance, $translate, pipelineService, $timeout, dataRuleDefn, rulesElMetadata, fieldPaths,
    streamLabelMap, alertTextElMetadata
  ) {

    angular.extend($scope, {
      showLoading: false,
      common: {
        errors: []
      },
      dataRuleDefn: dataRuleDefn,
      fieldPaths: fieldPaths,
      refreshCodemirror: false,
      streamLabelMap: streamLabelMap,

      getCodeMirrorOptions: function(fieldType) {
        var codeMirrorOptions = {
          dictionary: (fieldType === 'condition' ? rulesElMetadata : alertTextElMetadata),
          extraKeys: {
            'Tab': false,
            'Ctrl-Space': 'autocomplete'
          }
        };

        if(fieldType === 'condition') {
          $timeout(function() {
            $scope.refreshCodemirror = true;
          });
        }

        return angular.extend({}, pipelineService.getDefaultELEditorOptions(), codeMirrorOptions);
      },

      save : function () {
        $modalInstance.close($scope.dataRuleDefn);
      },
      cancel : function () {
        $modalInstance.dismiss('cancel');
      }
    });

    $scope.$broadcast('show-errors-check-validity');
  }]);
