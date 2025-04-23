import { type Ref, computed, defineComponent, inject, onMounted, ref } from 'vue';
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
    const eventService = inject('eventService', () => new EventService());
    const seatingTableService = inject('seatingTableService', () => new SeatingTableService());

    // Use globalThis.navigator and cast to any to avoid TS "cannot find name 'navigator'" error
    const currentLanguage = inject(
      'currentLanguage',
      () =>
        computed(() => {
          const nav = (globalThis as any).navigator;
          return nav && typeof nav.language === 'string' ? nav.language : 'en';
        }),
      true,
    );

    const guest: Ref<IGuest> = ref(new Guest());
    const events: Ref<IEvent[]> = ref([]);
    const seatingTables: Ref<ISeatingTable[]> = ref([]);
    const guests: Ref<IGuest[]> = ref([]);
    const guestStatusValues: Ref<string[]> = ref(Object.keys(GuestStatus));
    const guestSideValues: Ref<string[]> = ref(Object.keys(GuestSide));
    const guestRelationValues: Ref<string[]> = ref(Object.keys(GuestRelation));
    const isSaving = ref(false);

    const route = useRoute();
    const router = useRouter();
    const { t: t$ } = useI18n();

    const previousState = () => router.go(-1);

    // parse the string param into a number
    const retrieveGuest = async (guestId: string) => {
      const id = parseInt(guestId, 10);
      if (isNaN(id)) {
        // invalid id, skip
        return;
      }
      try {
        const res = await guestService().find(id);
        guest.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const initRelationships = () => {
      eventService()
        .retrieve()
        .then(res => (events.value = res.data));
      seatingTableService()
        .retrieve()
        .then(res => (seatingTables.value = res.data));
      guestService()
        .retrieve()
        .then(res => (guests.value = res.data));
    };

    const save = async () => {
      isSaving.value = true;
      try {
        if (guest.value.id) {
          const res = await guestService().update(guest.value);
          alertService.showInfo(t$('tableArrangmentsApp.guest.updated', { param: res.id }));
        } else {
          const res = await guestService().create(guest.value);
          alertService.showSuccess(t$('tableArrangmentsApp.guest.created', { param: res.id }).toString());
        }
        previousState();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    const getSelected = (selectedVals: any[], option: any, pkField = 'id') => {
      if (selectedVals) {
        return selectedVals.find(value => option[pkField] === value[pkField]) ?? option;
      }
      return option;
    };

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

    onMounted(() => {
      if (route.params?.guestId) {
        retrieveGuest(route.params.guestId as string);
      }
      initRelationships();
      guest.value.avoidGuests = [];
      guest.value.preferGuests = [];
      guest.value.avoidedBies = [];
      guest.value.preferredBies = [];
    });

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
      save,
      getSelected,
    };
  },
});
