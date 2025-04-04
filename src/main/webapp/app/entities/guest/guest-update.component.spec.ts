import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import GuestUpdate from './guest-update.vue';
import GuestService from './guest.service';
import AlertService from '@/shared/alert/alert.service';

import EventService from '@/entities/event/event.service';
import SeatingTableService from '@/entities/seating-table/seating-table.service';

type GuestUpdateComponentType = InstanceType<typeof GuestUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const guestSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<GuestUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Guest Management Update Component', () => {
    let comp: GuestUpdateComponentType;
    let guestServiceStub: SinonStubbedInstance<GuestService>;

    beforeEach(() => {
      route = {};
      guestServiceStub = sinon.createStubInstance<GuestService>(GuestService);
      guestServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          guestService: () => guestServiceStub,
          eventService: () =>
            sinon.createStubInstance<EventService>(EventService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
          seatingTableService: () =>
            sinon.createStubInstance<SeatingTableService>(SeatingTableService, {
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
        const wrapper = shallowMount(GuestUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.guest = guestSample;
        guestServiceStub.update.resolves(guestSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(guestServiceStub.update.calledWith(guestSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        guestServiceStub.create.resolves(entity);
        const wrapper = shallowMount(GuestUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.guest = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(guestServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        guestServiceStub.find.resolves(guestSample);
        guestServiceStub.retrieve.resolves([guestSample]);

        // WHEN
        route = {
          params: {
            guestId: `${guestSample.id}`,
          },
        };
        const wrapper = shallowMount(GuestUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.guest).toMatchObject(guestSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        guestServiceStub.find.resolves(guestSample);
        const wrapper = shallowMount(GuestUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
