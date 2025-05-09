import { type Ref, defineComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import GuestService from './guest.service';
import { GuestAssignmentService } from './guest-assignment.service';
import { type IGuest } from '@/shared/model/guest.model';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'Guest',
  setup() {
    const { t: t$ } = useI18n();
    const guestService = inject('guestService', () => new GuestService());
    const alertService = inject('alertService', () => useAlertService(), true);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number | null> = ref(null);
    const page: Ref<number> = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);
    const isFetching = ref(false);

    const guests: Ref<IGuest[]> = ref([]);

    const clear = () => {
      page.value = 1;
    };

    const sort = (): Array<any> => {
      const result = [`${propOrder.value},${reverse.value ? 'desc' : 'asc'}`];
      if (propOrder.value !== 'id') {
        result.push('id');
      }
      return result;
    };

    const retrieveGuests = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await guestService().retrieve(paginationQuery);
        totalItems.value = Number(res.headers['x-total-count']);
        queryCount.value = totalItems.value;
        guests.value = res.data;
      } catch (err) {
        if (err && err.response) {
          alertService.showHttpError(err.response);
        } else {
          console.error('Unknown error:', err);
        }
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveGuests();
    };

    onMounted(async () => {
      await retrieveGuests();
    });

    const removeId: Ref<number | null> = ref(null);
    const removeEntity = ref<any>(null);
    const prepareRemove = (instance: IGuest) => {
      removeId.value = instance.id;
      removeEntity.value.show();
    };
    const closeDialog = () => {
      removeEntity.value.hide();
    };
    const removeGuest = async () => {
      try {
        await guestService().delete(removeId.value);
        const message = t$('tableArrangementsApp.guest.deleted', { param: removeId.value }).toString();
        alertService.showInfo(message, { variant: 'danger' });
        removeId.value = null;
        retrieveGuests();
        closeDialog();
      } catch (error) {
        if (error && error.response) {
          alertService.showHttpError(error.response);
        } else {
          console.error('Unknown error during guest deletion:', error);
        }
      }
    };

    const changeOrder = (newOrder: string) => {
      if (propOrder.value === newOrder) {
        reverse.value = !reverse.value;
      } else {
        reverse.value = false;
      }
      propOrder.value = newOrder;
    };

    watch([propOrder, reverse], async () => {
      if (page.value === 1) {
        await retrieveGuests();
      } else {
        clear();
      }
    });

    watch(page, async () => {
      await retrieveGuests();
    });

    const assignGuestsWithConstraints = async () => {
      try {
        const service = new GuestAssignmentService();
        const warnings = await service.assignGuestsToTables();

        if (warnings.length > 0) {
          alertService.showError('⚠️ האורחים הבאים לא שובצו:\n' + warnings.join('\n'));
        } else {
          alertService.showSuccess('✅ שיבוץ האורחים בוצע בהצלחה לפי האילוצים.');
        }

        await retrieveGuests();
      } catch (error) {
        const fallbackMessage = '❌ אירעה שגיאה בלתי צפויה במהלך שיבוץ האורחים.';
        const serverMessage = error?.response?.data?.message || error?.response?.headers?.['x-guestapp-alert'] || fallbackMessage;
        alertService.showError(serverMessage);
      }
    };

    return {
      guests,
      handleSyncList,
      isFetching,
      retrieveGuests,
      clear,
      removeId,
      removeEntity,
      prepareRemove,
      closeDialog,
      removeGuest,
      itemsPerPage,
      queryCount,
      page,
      propOrder,
      reverse,
      totalItems,
      changeOrder,
      t$,
      assignGuestsWithConstraints,
    };
  },
});
