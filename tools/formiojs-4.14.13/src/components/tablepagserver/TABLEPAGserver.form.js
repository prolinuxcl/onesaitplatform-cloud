import Components from '../Components';
import TABLEPAGEditDisplay from './editForm/TABLEPAGserver.edit.display';
import TABLEPAGEditLogic from './editForm/TABLEPAGserver.edit.logic';

export default function(...extend) {
  return Components.baseEditForm([
    {
      key: 'display',
      components: TABLEPAGEditDisplay,
    },
    {
      key: 'data',
      ignore: true,
    },
    {
      key: 'validation',
      ignore: true,
    },
    {
      key: 'logic',
      components: TABLEPAGEditLogic,
    },
    {
      key: 'addons',
      ignore: true
    },
  ], ...extend);
}
