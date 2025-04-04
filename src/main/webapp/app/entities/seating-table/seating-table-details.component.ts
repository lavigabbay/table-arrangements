import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import SeatingTableService from './seating-table.service';
import { type ISeatingTable } from '@/shared/model/seating-table.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'SeatingTableDetails',
  setup() {
    const seatingTableService = inject('seatingTableService', () => new SeatingTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const seatingTable: Ref<ISeatingTable> = ref({});

    const retrieveSeatingTable = async seatingTableId => {
      try {
        const res = await seatingTableService().find(seatingTableId);
        seatingTable.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.seatingTableId) {
      retrieveSeatingTable(route.params.seatingTableId);
    }

    return {
      alertService,
      seatingTable,

      previousState,
      t$: useI18n().t,
    };
  },
});
