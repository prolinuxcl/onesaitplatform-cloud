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

// Controller for Error Configuration.
angular
  .module('dataCollectorApp.home')
  .controller('ErrorConfigurationController', ["$scope", "pipelineService", function ($scope, pipelineService) {
    angular.extend($scope, {
      pipelinePaneConfig: $scope.detailPaneConfig
    });

    var initialize = function() {
      $scope.detailPaneConfig = $scope.errorStageConfig;
      $scope.detailPaneServices = [];
      angular.forEach($scope.detailPaneConfig.services, function(serviceConfig) {
        $scope.detailPaneServices.push({
          definition: pipelineService.getServiceDefinition(serviceConfig.service),
          config: serviceConfig
        });
      });
    };

    $scope.$watch('errorStageConfig', function() {
      initialize();
    });

    $scope.$watch('$parent.detailPaneConfig', function() {
      $scope.pipelinePaneConfig = $scope.$parent.detailPaneConfig;
    });

    initialize();
  }]);
