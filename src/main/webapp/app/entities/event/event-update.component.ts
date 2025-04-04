import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import EventService from './event.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import UserService from '@/entities/user/user.service';
import { Event, type IEvent } from '@/shared/model/event.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'EventUpdate',
  setup() {
    const eventService = inject('eventService', () => new EventService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const event: Ref<IEvent> = ref(new Event());
    const userService = inject('userService', () => new UserService());
    const users: Ref<Array<any>> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveEvent = async eventId => {
      try {
        const res = await eventService().find(eventId);
        res.receptionTime = new Date(res.receptionTime);
        res.weddingTime = new Date(res.weddingTime);
        event.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.eventId) {
      retrieveEvent(route.params.eventId);
    }

    const initRelationships = () => {
      userService()
        .retrieve()
        .then(res => {
          users.value = res.data;
        });
    };

    initRelationships();

    const { t: t$ } = useI18n();
    const validations = useValidation();
    const validationRules = {
      eventName: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      eventOwners: {},
      groomParents: {},
      brideParents: {},
      weddingDate: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      receptionTime: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      weddingTime: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      user: {},
    };
    const v$ = useVuelidate(validationRules, event as any);
    v$.value.$validate();

    return {
      eventService,
      alertService,
      event,
      previousState,
      isSaving,
      currentLanguage,
      users,
      v$,
      ...useDateFormat({ entityRef: event }),
      t$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.event.id) {
        this.eventService()
          .update(this.event)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.event.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.eventService()
          .create(this.event)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.event.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
