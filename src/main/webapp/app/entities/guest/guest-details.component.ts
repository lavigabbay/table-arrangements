import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import GuestService from './guest.service';
import { type IGuest } from '@/shared/model/guest.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'GuestDetails',
  setup() {
    const guestService = inject('guestService', () => new GuestService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const guest: Ref<IGuest> = ref({});

    const retrieveGuest = async guestId => {
      try {
        const res = await guestService().find(guestId);
        guest.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.guestId) {
      retrieveGuest(route.params.guestId);
    }

    return {
      alertService,
      guest,

      previousState,
      t$: useI18n().t,
    };
  },
});
