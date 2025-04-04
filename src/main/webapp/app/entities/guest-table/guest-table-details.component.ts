import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import GuestTableService from './guest-table.service';
import { type IGuestTable } from '@/shared/model/guest-table.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'GuestTableDetails',
  setup() {
    const guestTableService = inject('guestTableService', () => new GuestTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const guestTable: Ref<IGuestTable> = ref({});

    const retrieveGuestTable = async guestTableId => {
      try {
        const res = await guestTableService().find(guestTableId);
        guestTable.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.guestTableId) {
      retrieveGuestTable(route.params.guestTableId);
    }

    return {
      alertService,
      guestTable,

      previousState,
      t$: useI18n().t,
    };
  },
});
