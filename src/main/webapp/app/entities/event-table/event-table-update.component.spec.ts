import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import EventTableUpdate from './event-table-update.vue';
import EventTableService from './event-table.service';
import AlertService from '@/shared/alert/alert.service';

import VenueTableService from '@/entities/venue-table/venue-table.service';

type EventTableUpdateComponentType = InstanceType<typeof EventTableUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const eventTableSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<EventTableUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('EventTable Management Update Component', () => {
    let comp: EventTableUpdateComponentType;
    let eventTableServiceStub: SinonStubbedInstance<EventTableService>;

    beforeEach(() => {
      route = {};
      eventTableServiceStub = sinon.createStubInstance<EventTableService>(EventTableService);
      eventTableServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

      alertService = new AlertService({
        i18n: { t: vitest.fn() } as any,
        bvToast: {
          toast: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          'font-awesome-icon': true,
          'b-input-group': true,
          'b-input-group-prepend': true,
          'b-form-datepicker': true,
          'b-form-input': true,
        },
        provide: {
          alertService,
          eventTableService: () => eventTableServiceStub,
          venueTableService: () =>
            sinon.createStubInstance<VenueTableService>(VenueTableService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(EventTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.eventTable = eventTableSample;
        eventTableServiceStub.update.resolves(eventTableSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(eventTableServiceStub.update.calledWith(eventTableSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        eventTableServiceStub.create.resolves(entity);
        const wrapper = shallowMount(EventTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.eventTable = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(eventTableServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        eventTableServiceStub.find.resolves(eventTableSample);
        eventTableServiceStub.retrieve.resolves([eventTableSample]);

        // WHEN
        route = {
          params: {
            eventTableId: `${eventTableSample.id}`,
          },
        };
        const wrapper = shallowMount(EventTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.eventTable).toMatchObject(eventTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        eventTableServiceStub.find.resolves(eventTableSample);
        const wrapper = shallowMount(EventTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
