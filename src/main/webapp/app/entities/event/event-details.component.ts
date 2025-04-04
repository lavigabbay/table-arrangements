import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import EventService from './event.service';
import { useDateFormat } from '@/shared/composables';
import { type IEvent } from '@/shared/model/event.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'EventDetails',
  setup() {
    const dateFormat = useDateFormat();
    const eventService = inject('eventService', () => new EventService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);
    const event: Ref<IEvent> = ref({});

    const retrieveEvent = async eventId => {
      try {
        const res = await eventService().find(eventId);
        event.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.eventId) {
      retrieveEvent(route.params.eventId);
    }

    return {
      ...dateFormat,
      alertService,
      event,

      previousState,
      t$: useI18n().t,
    };
  },
});
