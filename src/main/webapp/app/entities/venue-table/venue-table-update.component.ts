import { type Ref, computed, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useVuelidate } from '@vuelidate/core';

import VenueTableService from './venue-table.service';
import { useDateFormat, useValidation } from '@/shared/composables';
import { useAlertService } from '@/shared/alert/alert.service';

import UserService from '@/entities/user/user.service';
import { type IVenueTable, VenueTable } from '@/shared/model/venue-table.model';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'VenueTableUpdate',
  setup() {
    const venueTableService = inject('venueTableService', () => new VenueTableService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const venueTable: Ref<IVenueTable> = ref(new VenueTable());
    const userService = inject('userService', () => new UserService());
    const users: Ref<Array<any>> = ref([]);
    const isSaving = ref(false);
    const currentLanguage = inject('currentLanguage', () => computed(() => navigator.language ?? 'en'), true);

    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const retrieveVenueTable = async venueTableId => {
      try {
        const res = await venueTableService().find(venueTableId);
        res.receptionTime = new Date(res.receptionTime);
        res.weddingTime = new Date(res.weddingTime);
        venueTable.value = res;
      } catch (error) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.venueTableId) {
      retrieveVenueTable(route.params.venueTableId);
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
      numberOfTables: {
        required: validations.required(t$('entity.validation.required').toString()),
        integer: validations.integer(t$('entity.validation.number').toString()),
      },
      nearStageTables: {
        required: validations.required(t$('entity.validation.required').toString()),
        integer: validations.integer(t$('entity.validation.number').toString()),
      },
      venueName: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      eventOwners: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      groomParents: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
      brideParents: {
        required: validations.required(t$('entity.validation.required').toString()),
      },
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
    const v$ = useVuelidate(validationRules, venueTable as any);
    v$.value.$validate();

    return {
      venueTableService,
      alertService,
      venueTable,
      previousState,
      isSaving,
      currentLanguage,
      users,
      v$,
      ...useDateFormat({ entityRef: venueTable }),
      t$,
    };
  },
  created(): void {},
  methods: {
    save(): void {
      this.isSaving = true;
      if (this.venueTable.id) {
        this.venueTableService()
          .update(this.venueTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showInfo(this.t$('tableArrangmentsApp.venueTable.updated', { param: param.id }));
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      } else {
        this.venueTableService()
          .create(this.venueTable)
          .then(param => {
            this.isSaving = false;
            this.previousState();
            this.alertService.showSuccess(this.t$('tableArrangmentsApp.venueTable.created', { param: param.id }).toString());
          })
          .catch(error => {
            this.isSaving = false;
            this.alertService.showHttpError(error.response);
          });
      }
    },
  },
});
