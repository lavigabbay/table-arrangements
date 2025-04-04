import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import VenueTableService from './venue-table.service';
import { useDateFormat } from '@/shared/composables';
import { type IVenueTable } from '@/shared/model/venue-table.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'VenueTableDetails',
  setup() {
    const dateFormat = useDateFormat();
    const venueTableService = inject('venueTableService', () => new VenueTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const venueTable: Ref<IVenueTable> = ref({});

    const retrieveVenueTable = async venueTableId => {
      try {
        const res = await venueTableService().find(venueTableId);
        venueTable.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.venueTableId) {
      retrieveVenueTable(route.params.venueTableId);
    }

    return {
      ...dateFormat,
      alertService,
      venueTable,

      previousState,
      t$: useI18n().t,
    };
  },
});
