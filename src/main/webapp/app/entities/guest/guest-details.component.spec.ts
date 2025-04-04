import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import GuestDetails from './guest-details.vue';
import GuestService from './guest.service';
import AlertService from '@/shared/alert/alert.service';

type GuestDetailsComponentType = InstanceType<typeof GuestDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const guestSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Guest Management Detail Component', () => {
    let guestServiceStub: SinonStubbedInstance<GuestService>;
    let mountOptions: MountingOptions<GuestDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      guestServiceStub = sinon.createStubInstance<GuestService>(GuestService);

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
          guestService: () => guestServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        guestServiceStub.find.resolves(guestSample);
        route = {
          params: {
            guestId: `${123}`,
          },
        };
        const wrapper = shallowMount(GuestDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.guest).toMatchObject(guestSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        guestServiceStub.find.resolves(guestSample);
        const wrapper = shallowMount(GuestDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
