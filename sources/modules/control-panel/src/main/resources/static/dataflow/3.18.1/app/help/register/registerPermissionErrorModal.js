/*
 * Copyright 2020 StreamSets Inc.
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
 * Controller for when user is not admin, so cannot show Register Modal Dialog
 */
angular
  .module('dataCollectorApp')
  .controller('RegisterPermissionErrorModalController', ["$scope", "$rootScope", "api", function ($scope, $rootScope, api) {
    angular.extend($scope, {
      logout: function() {
        api.admin.logout($rootScope.common.authenticationType, $rootScope.common.isDPMEnabled).then(function() {
          location.reload();
        });
      }
    });
  }]);
