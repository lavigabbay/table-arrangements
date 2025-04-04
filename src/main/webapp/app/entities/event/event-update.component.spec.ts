import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';
import { type RouteLocation } from 'vue-router';

import dayjs from 'dayjs';
import EventUpdate from './event-update.vue';
import EventService from './event.service';
import { DATE_TIME_LONG_FORMAT } from '@/shared/composables/date-format';
import AlertService from '@/shared/alert/alert.service';

import UserService from '@/entities/user/user.service';

type EventUpdateComponentType = InstanceType<typeof EventUpdate>;

let route: Partial<RouteLocation>;
const routerGoMock = vitest.fn();

vitest.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ go: routerGoMock }),
}));

const eventSample = { id: 123 };

describe('Component Tests', () => {
  let mountOptions: MountingOptions<EventUpdateComponentType>['global'];
  let alertService: AlertService;

  describe('Event Management Update Component', () => {
    let comp: EventUpdateComponentType;
    let eventServiceStub: SinonStubbedInstance<EventService>;

    beforeEach(() => {
      route = {};
      eventServiceStub = sinon.createStubInstance<EventService>(EventService);
      eventServiceStub.retrieve.onFirstCall().resolves(Promise.resolve([]));

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
          eventService: () => eventServiceStub,

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
        const wrapper = shallowMount(EventUpdate, { global: mountOptions });
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
        const wrapper = shallowMount(EventUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.event = eventSample;
        eventServiceStub.update.resolves(eventSample);

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(eventServiceStub.update.calledWith(eventSample)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });

      it('Should call create service on save for new entity', async () => {
        // GIVEN
        const entity = {};
        eventServiceStub.create.resolves(entity);
        const wrapper = shallowMount(EventUpdate, { global: mountOptions });
        comp = wrapper.vm;
        comp.event = entity;

        // WHEN
        comp.save();
        await comp.$nextTick();

        // THEN
        expect(eventServiceStub.create.calledWith(entity)).toBeTruthy();
        expect(comp.isSaving).toEqual(false);
      });
    });

    describe('Before route enter', () => {
      it('Should retrieve data', async () => {
        // GIVEN
        eventServiceStub.find.resolves(eventSample);
        eventServiceStub.retrieve.resolves([eventSample]);

        // WHEN
        route = {
          params: {
            eventId: `${eventSample.id}`,
          },
        };
        const wrapper = shallowMount(EventUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(comp.event).toMatchObject(eventSample);
      });
    });

    describe('Previous state', () => {
      it('Should go previous state', async () => {
        eventServiceStub.find.resolves(eventSample);
        const wrapper = shallowMount(EventUpdate, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();

        comp.previousState();
        await comp.$nextTick();

        expect(routerGoMock).toHaveBeenCalledWith(-1);
      });
    });
  });
});
