import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import GuestTableUpdate from './guest-table-update.vue';
import GuestTableService from './guest-table.service';
import AlertService from '@/shared/alert/alert.service';

import VenueTableService from '@/entities/venue-table/venue-table.service';
import EventTableService from '@/entities/event-table/event-table.service';

type GuestTableUpdateComponentType = InstanceType<typeof GuestTableUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const guestTableSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<GuestTableUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('GuestTable Management Update Component', () => {
    let comp: GuestTableUpdateComponentType;
    let guestTableServiceStub: SinonStubbedInstance<GuestTableService>;

    beforeEach(() => {
      route = {};
      guestTableServiceStub = sinon.createStubInstance<GuestTableService>(GuestTableService);
      guestTableServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          guestTableService: () => guestTableServiceStub,
          venueTableService: () =>
            sinon.createStubInstance<VenueTableService>(VenueTableService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          eventTableService: () =>
            sinon.createStubInstance<EventTableService>(EventTableService, {
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
        const wrapper = shallowMount(GuestTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.guestTable = guestTableSample;
        guestTableServiceStub.update.resolves(guestTableSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(guestTableServiceStub.update.calledWith(guestTableSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        guestTableServiceStub.create.resolves(entity);
        const wrapper = shallowMount(GuestTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.guestTable = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(guestTableServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        guestTableServiceStub.find.resolves(guestTableSample);
        guestTableServiceStub.retrieve.resolves([guestTableSample]);

        // WHEN
        route = {
          params: {
            guestTableId: `${guestTableSample.id}`,
          },
        };
        const wrapper = shallowMount(GuestTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.guestTable).toMatchObject(guestTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        guestTableServiceStub.find.resolves(guestTableSample);
        const wrapper = shallowMount(GuestTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
