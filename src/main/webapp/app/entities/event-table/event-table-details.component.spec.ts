import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import EventTableDetails from './event-table-details.vue';
import EventTableService from './event-table.service';
import AlertService from '@/shared/alert/alert.service';

type EventTableDetailsComponentType = InstanceType<typeof EventTableDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const eventTableSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('EventTable Management Detail Component', () => {
    let eventTableServiceStub: SinonStubbedInstance<EventTableService>;
    let mountOptions: MountingOptions<EventTableDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      eventTableServiceStub = sinon.createStubInstance<EventTableService>(EventTableService);

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
          eventTableService: () => eventTableServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        eventTableServiceStub.find.resolves(eventTableSample);
        route = {
          params: {
            eventTableId: `${123}`,
          },
        };
        const wrapper = shallowMount(EventTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.eventTable).toMatchObject(eventTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        eventTableServiceStub.find.resolves(eventTableSample);
        const wrapper = shallowMount(EventTableDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
