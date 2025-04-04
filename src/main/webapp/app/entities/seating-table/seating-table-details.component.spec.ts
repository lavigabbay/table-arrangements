import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import SeatingTableDetails from './seating-table-details.vue';
import SeatingTableService from './seating-table.service';
import AlertService from '@/shared/alert/alert.service';

type SeatingTableDetailsComponentType = InstanceType<typeof SeatingTableDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const seatingTableSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('SeatingTable Management Detail Component', () => {
    let seatingTableServiceStub: SinonStubbedInstance<SeatingTableService>;
    let mountOptions: MountingOptions<SeatingTableDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      seatingTableServiceStub = sinon.createStubInstance<SeatingTableService>(SeatingTableService);

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
          seatingTableService: () => seatingTableServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        seatingTableServiceStub.find.resolves(seatingTableSample);
        route = {
          params: {
            seatingTableId: `${123}`,
          },
        };
        const wrapper = shallowMount(SeatingTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.seatingTable).toMatchObject(seatingTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        seatingTableServiceStub.find.resolves(seatingTableSample);
        const wrapper = shallowMount(SeatingTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
