import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import GuestService from './guest.service';
import { useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import EventService from '@/entities/event/event.service';
import { type IEvent } from '@/shared/model/event.model';
import SeatingTableService from '@/entities/seating-table/seating-table.service';
import { type ISeatingTable } from '@/shared/model/seating-table.model';
import { Guest, type IGuest } from '@/shared/model/guest.model';
import { GuestStatus } from '@/shared/model/enumerations/guest-status.model';
import { GuestSide } from '@/shared/model/enumerations/guest-side.model';
import { GuestRelation } from '@/shared/model/enumerations/guest-relation.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'GuestUpdate',
  setup() {
    const guestService = inject('guestService', () => new GuestService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const guest: Ref<IGuest> = ref(new Guest());

    const eventService = inject('eventService', () => new EventService());

    const events: Ref<IEvent[]> = ref([]);

    const seatingTableService = inject('seatingTableService', () => new SeatingTableService());

    const seatingTables: Ref<ISeatingTable[]> = ref([]);

    const guests: Ref<IGuest[]> = ref([]);
    const guestStatusValues: Ref<string[]> = ref(Object.keys(GuestStatus));
    const guestSideValues: Ref<string[]> = ref(Object.keys(GuestSide));
    const guestRelationValues: Ref<string[]> = ref(Object.keys(GuestRelation));
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

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

    const initRelationships = () => {
      eventService()
        .retrieve()
        .then(res => {
          events.value = res.data;
        });
      seatingTableService()
        .retrieve()
        .then(res => {
          seatingTables.value = res.data;
        });
      guestService()
        .retrieve()
        .then(res => {
          guests.value = res.data;
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
      phone: {},
      nearStage: {},
      status: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      side: {},
      relation: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      accessibility: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      event: {},
      table: {},
      avoidGuests: {},
      preferGuests: {},
      avoidedBies: {},
      preferredBies: {},
    };
    const v$ = useVuelidate(validationRules, guest as any);
    v$.value.$validate();

    return {
      guestService,
      alertService,
      guest,
      previousState,
      guestStatusValues,
      guestSideValues,
      guestRelationValues,
      isSaving,
      currentLanguage,
      events,
      seatingTables,
      guests,
      v$,
      t$,
    };
  },
  created(): void {
    this.guest.avoidGuests = [];
    this.guest.preferGuests = [];
    this.guest.avoidedBies = [];
    this.guest.preferredBies = [];
  },
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.guest.id) {
        this.guestService()
          .update(this.guest)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.guest.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.guestService()
          .create(this.guest)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.guest.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },

    getSelected(selectedVals, option, pkField = 'id'): any {
      if (selectedVals) {
        return selectedVals.find(value => option[pkField] === value[pkField]) ?? option;
      }
      return option;
    },
  },
});
