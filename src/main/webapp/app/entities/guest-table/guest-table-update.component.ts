import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import GuestTableService from './guest-table.service';
import { useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import VenueTableService from '@/entities/venue-table/venue-table.service';
import { type IVenueTable } from '@/shared/model/venue-table.model';
import EventTableService from '@/entities/event-table/event-table.service';
import { type IEventTable } from '@/shared/model/event-table.model';
import { GuestTable, type IGuestTable } from '@/shared/model/guest-table.model';
import { GuestStatus } from '@/shared/model/enumerations/guest-status.model';
import { GuestRelation } from '@/shared/model/enumerations/guest-relation.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'GuestTableUpdate',
  setup() {
    const guestTableService = inject('guestTableService', () => new GuestTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const guestTable: Ref<IGuestTable> = ref(new GuestTable());

    const venueTableService = inject('venueTableService', () => new VenueTableService());

    const venueTables: Ref<IVenueTable[]> = ref([]);

    const eventTableService = inject('eventTableService', () => new EventTableService());

    const eventTables: Ref<IEventTable[]> = ref([]);
    const guestStatusValues: Ref<string[]> = ref(Object.keys(GuestStatus));
    const guestRelationValues: Ref<string[]> = ref(Object.keys(GuestRelation));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

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

    const initRelationships = () => {
      venueTableService()
        .retrieve()
        .then(res => {
          venueTables.value = res.data;
        });
      eventTableService()
        .retrieve()
        .then(res => {
          eventTables.value = res.data;
        });
    };

    initRelationships();

    const { t: t$ } = useI18n();
    const validations = useValidation();
    const validationRules = {
      lastNameAndFirstName: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      numberOfSeats: {
        required: validations.required(t$('entity.validation.required').toString()),
        integer: validations.integer(t$('entity.validation.number').toString()),
      },
      phone: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      nearDanceFloor: {},
      status: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      side: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      relation: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      notWithId: {},
      withId: {},
      conditions: {},
      accessibility: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      venueName: {},
      eventTable: {},
    };
    const v$ = useVuelidate(validationRules, guestTable as any);
    v$.value.$validate();

    return {
      guestTableService,
      alertService,
      guestTable,
      previousState,
      guestStatusValues,
      guestRelationValues,
      isSaving,
      currentLanguage,
      venueTables,
      eventTables,
      v$,
      t$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.guestTable.id) {
        this.guestTableService()
          .update(this.guestTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.guestTable.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.guestTableService()
          .create(this.guestTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.guestTable.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
