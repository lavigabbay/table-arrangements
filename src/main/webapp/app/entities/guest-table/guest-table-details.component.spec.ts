import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import GuestTableDetails from './guest-table-details.vue';
import GuestTableService from './guest-table.service';
import AlertService from '@/shared/alert/alert.service';

type GuestTableDetailsComponentType = InstanceType<typeof GuestTableDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const guestTableSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('GuestTable Management Detail Component', () => {
    let guestTableServiceStub: SinonStubbedInstance<GuestTableService>;
    let mountOptions: MountingOptions<GuestTableDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      guestTableServiceStub = sinon.createStubInstance<GuestTableService>(GuestTableService);

      alertService = new AlertService({
        i18n: { t: vitest.fn() } as any,
        bvToast: {
          toast: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'router-link': true,
        },
        provide: {
          alertService,
          guestTableService: () => guestTableServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        guestTableServiceStub.find.resolves(guestTableSample);
        route = {
          params: {
            guestTableId: `${123}`,
          },
        };
        const wrapper = shallowMount(GuestTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.guestTable).toMatchObject(guestTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        guestTableServiceStub.find.resolves(guestTableSample);
        const wrapper = shallowMount(GuestTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
