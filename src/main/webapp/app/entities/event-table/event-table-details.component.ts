import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import EventTableService from './event-table.service';
import { type IEventTable } from '@/shared/model/event-table.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'EventTableDetails',
  setup() {
    const eventTableService = inject('eventTableService', () => new EventTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const eventTable: Ref<IEventTable> = ref({});

    const retrieveEventTable = async eventTableId => {
      try {
        const res = await eventTableService().find(eventTableId);
        eventTable.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.eventTableId) {
      retrieveEventTable(route.params.eventTableId);
    }

    return {
      alertService,
      eventTable,

      previousState,
      t$: useI18n().t,
    };
  },
});
