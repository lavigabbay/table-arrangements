import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import EventTableService from './event-table.service';
import { useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import VenueTableService from '@/entities/venue-table/venue-table.service';
import { type IVenueTable } from '@/shared/model/venue-table.model';
import { EventTable, type IEventTable } from '@/shared/model/event-table.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'EventTableUpdate',
  setup() {
    const eventTableService = inject('eventTableService', () => new EventTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const eventTable: Ref<IEventTable> = ref(new EventTable());

    const venueTableService = inject('venueTableService', () => new VenueTableService());

    const venueTables: Ref<IVenueTable[]> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

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

    const initRelationships = () => {
      venueTableService()
        .retrieve()
        .then(res => {
          venueTables.value = res.data;
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
      venue: {},
    };
    const v$ = useVuelidate(validationRules, eventTable as any);
    v$.value.$validate();

    return {
      eventTableService,
      alertService,
      eventTable,
      previousState,
      isSaving,
      currentLanguage,
      venueTables,
      v$,
      t$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.eventTable.id) {
        this.eventTableService()
          .update(this.eventTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.eventTable.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.eventTableService()
          .create(this.eventTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.eventTable.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
