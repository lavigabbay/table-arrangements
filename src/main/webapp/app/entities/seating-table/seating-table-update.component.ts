/// <reference lib="dom" />

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

    const currentLanguage = inject(
      'currentLanguage',
      () =>
        computed(() => {
          const nav = globalThis.navigator as Navigator;
          return nav?.language ?? 'en';
        }),
      true,
    );

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveSeatingTable = async (seatingTableId: number): Promise<void> => {
      try {
        const res = await seatingTableService().find(seatingTableId);
        seatingTable.value = res;
      } catch (error: unknown) {
        alertService.showHttpError((error as any)?.response);
      }
    };

    if (route.params?.seatingTableId) {
      const id = parseInt(route.params.seatingTableId as string, 10);
      if (!isNaN(id)) {
        retrieveSeatingTable(id);
      }
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
      event: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
    };

    const v$ = useVuelidate(validationRules, seatingTable as any);
    v$.value.$validate();

    const save = async (): Promise<void> => {
      isSaving.value = true;
      try {
        if (seatingTable.value.id) {
          const res = await seatingTableService().update(seatingTable.value);
          alertService.showInfo(t$('tableArrangmentsApp.seatingTable.updated', { param: res.id }));
        } else {
          const res = await seatingTableService().create(seatingTable.value);
          alertService.showSuccess(t$('tableArrangmentsApp.seatingTable.created', { param: res.id }).toString());
        }
        previousState();
      } catch (error: unknown) {
        alertService.showHttpError((error as any)?.response);
      } finally {
        isSaving.value = false;
      }
    };

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
      save,
    };
  },
});
