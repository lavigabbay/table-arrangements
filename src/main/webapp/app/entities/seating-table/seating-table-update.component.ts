import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import SeatingTableService from './seating-table.service';
import { useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import EventService from '@/entities/event/event.service';
import { type IEvent } from '@/shared/model/event.model';
import { type ISeatingTable, SeatingTable } from '@/shared/model/seating-table.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'SeatingTableUpdate',
  setup() {
    const seatingTableService = inject('seatingTableService', () => new SeatingTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const seatingTable: Ref<ISeatingTable> = ref(new SeatingTable());

    const eventService = inject('eventService', () => new EventService());

    const events: Ref<IEvent[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

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

    const initRelationships = () => {
      eventService()
        .retrieve()
        .then(res => {
          events.value = res.data;
        });
    };

    initRelationships();

    const { t: t$ } = useI18n();
    const validations = useValidation();
    const validationRules = {
      tableNumber: {
        required: validations.required(t$('entity.validation.required').toString()),
        integer: validations.integer(t$('entity.validation.number').toString()),
      },
      maxSeats: {
        required: validations.required(t$('entity.validation.required').toString()),
        integer: validations.integer(t$('entity.validation.number').toString()),
      },
      nearStage: {},
      accessibility: {},
      event: {},
    };
    const v$ = useVuelidate(validationRules, seatingTable as any);
    v$.value.$validate();

    return {
      seatingTableService,
      alertService,
      seatingTable,
      previousState,
      isSaving,
      currentLanguage,
      events,
      v$,
      t$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.seatingTable.id) {
        this.seatingTableService()
          .update(this.seatingTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.seatingTable.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.seatingTableService()
          .create(this.seatingTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.seatingTable.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
