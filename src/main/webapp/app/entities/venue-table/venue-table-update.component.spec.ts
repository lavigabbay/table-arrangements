import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import dayjs from 'dayjs';
import VenueTableUpdate from './venue-table-update.vue';
import VenueTableService from './venue-table.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';
import AlertService from '@/shared/alert/alert.service';

import UserService from '@/entities/user/user.service';

type VenueTableUpdateComponentType = InstanceType<typeof VenueTableUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const venueTableSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<VenueTableUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('VenueTable Management Update Component', () => {
    let comp: VenueTableUpdateComponentType;
    let venueTableServiceStub: SinonStubbedInstance<VenueTableService>;

    beforeEach(() => {
      route = {};
      venueTableServiceStub = sinon.createStubInstance<VenueTableService>(VenueTableService);
      venueTableServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          venueTableService: () => venueTableServiceStub,

          userService: () =>
            sinon.createStubInstance<UserService>(UserService, {
              retrieve: sinon.stub().resolves({}),
            } as any),
        },
      };
    });

    afterEach(() => {
      vitest.resetAllMocks();
    });

    describe('load', () => {
      beforeEach(() => {
        const wrapper = shallowMount(VenueTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
      });
      it('Should convert date from string', () => {
        // GIVEN
        const date = new Date('2019-10-15T11:42:02Z');

        // WHEN
        const convertedDate = comp.convertDateTimeFromServer(date);

        // THEN
        expect(convertedDate).toEqual(dayjs(date).format(DATE_TIME_LONG_FORMAT));
      });

      it('Should not convert date if date is not present', () => {
        expect(comp.convertDateTimeFromServer(null)).toBeNull();
      });
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', async () => {
        // GIVEN
        const wrapper = shallowMount(VenueTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.venueTable = venueTableSample;
        venueTableServiceStub.update.resolves(venueTableSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(venueTableServiceStub.update.calledWith(venueTableSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        venueTableServiceStub.create.resolves(entity);
        const wrapper = shallowMount(VenueTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.venueTable = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(venueTableServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        venueTableServiceStub.find.resolves(venueTableSample);
        venueTableServiceStub.retrieve.resolves([venueTableSample]);

        // WHEN
        route = {
          params: {
            venueTableId: `${venueTableSample.id}`,
          },
        };
        const wrapper = shallowMount(VenueTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.venueTable).toMatchObject(venueTableSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        venueTableServiceStub.find.resolves(venueTableSample);
        const wrapper = shallowMount(VenueTableUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
