import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import SeatingTableUpdate from './seating-table-update.vue';
import SeatingTableService from './seating-table.service';
import AlertService from '@/shared/alert/alert.service';

import EventService from '@/entities/event/event.service';

type SeatingTableUpdateComponentType = InstanceType<typeof SeatingTableUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const seatingTableSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<SeatingTableUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('SeatingTable Management Update Component', () => {
    let comp: SeatingTableUpdateComponentType;
    let seatingTableServiceStub: SinonStubbedInstance<SeatingTableService>;

    beforeEach(() => {
      route = {};
      seatingTableServiceStub = sinon.createStubInstance<SeatingTableService>(SeatingTableService);
      seatingTableServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          seatingTableService: () => seatingTableServiceStub,
          eventService: () =>
            sinon.createStubInstance<EventService>(EventService, {
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
        const wrapper = shallowMount(SeatingTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.seatingTable = seatingTableSample;
        seatingTableServiceStub.update.resolves(seatingTableSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.update.calledWith(seatingTableSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        seatingTableServiceStub.create.resolves(entity);
        const wrapper = shallowMount(SeatingTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.seatingTable = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        seatingTableServiceStub.find.resolves(seatingTableSample);
        seatingTableServiceStub.retrieve.resolves([seatingTableSample]);

        // WHEN
        route = {
          params: {
            seatingTableId: `${seatingTableSample.id}`,
          },
        };
        const wrapper = shallowMount(SeatingTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.seatingTable).toMatchObject(seatingTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        seatingTableServiceStub.find.resolves(seatingTableSample);
        const wrapper = shallowMount(SeatingTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
