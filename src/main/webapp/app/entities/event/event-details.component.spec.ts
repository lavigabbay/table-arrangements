import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import EventDetails from './event-details.vue';
import EventService from './event.service';
import AlertService from '@/shared/alert/alert.service';

type EventDetailsComponentType = InstanceType<typeof EventDetails>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const eventSample = { id: 123 };

describe('Component Tests', () => {
  let alertService: AlertService;

  afterEach(() => {
    vitest.resetAllMocks();
  });

  describe('Event Management Detail Component', () => {
    let eventServiceStub: SinonStubbedInstance<EventService>;
    let mountOptions: MountingOptions<EventDetailsComponentType>['global'];

    beforeEach(() => {
      route = {};
      eventServiceStub = sinon.createStubInstance<EventService>(EventService);

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
          eventService: () => eventServiceStub,
        },
      };
    });

    describe('Navigate to details', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        eventServiceStub.find.resolves(eventSample);
        route = {
          params: {
            eventId: `${123}`,
          },
        };
        const wrapper = shallowMount(EventDetails, { global: mountOptions });
        const comp = wrapper.vm;
        // WHEN
        await comp.$nextTick();

        // THEN
        expect(comp.event).toMatchObject(eventSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        eventServiceStub.find.resolves(eventSample);
        const wrapper = shallowMount(EventDetails, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
