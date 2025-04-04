import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import VenueTableDetails from './venue-table-details.vue';
import VenueTableService from './venue-table.service';
import AlertService from '@/shared/alert/alert.service';

type VenueTableDetailsComponentType = InstanceType<typeof VenueTableDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const venueTableSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('VenueTable Management Detail Component', () => {
    let venueTableServiceStub: SinonStubbedInstance<VenueTableService>;
    let mountOptions: MountingOptions<VenueTableDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      venueTableServiceStub = sinon.createStubInstance<VenueTableService>(VenueTableService);

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
          venueTableService: () => venueTableServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        venueTableServiceStub.find.resolves(venueTableSample);
        route = {
          params: {
            venueTableId: `${123}`,
          },
        };
        const wrapper = shallowMount(VenueTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.venueTable).toMatchObject(venueTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        venueTableServiceStub.find.resolves(venueTableSample);
        const wrapper = shallowMount(VenueTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
