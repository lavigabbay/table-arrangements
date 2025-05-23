import { vitest } from 'vitest';
import { type MountingOptions, shallowMount } from '@vue/test-utils';
import sinon, { type SinonStubbedInstance } from 'sinon';

import SeatingTable from './seating-table.vue';
import SeatingTableService from './seating-table.service';
import AlertService from '@/shared/alert/alert.service';

type SeatingTableComponentType = InstanceType<typeof SeatingTable>;

const bModalStub = {
  render: () => {},
  methods: {
    hide: () => {},
    show: () => {},
  },
};

describe('Component Tests', () => {
  let alertService: AlertService;

  describe('SeatingTable Management Component', () => {
    let seatingTableServiceStub: SinonStubbedInstance<SeatingTableService>;
    let mountOptions: MountingOptions<SeatingTableComponentType>['global'];

    beforeEach(() => {
      seatingTableServiceStub = sinon.createStubInstance<SeatingTableService>(SeatingTableService);
      seatingTableServiceStub.retrieve.resolves({ headers: {} });

      alertService = new AlertService({
        i18n: { t: vitest.fn() } as any,
        bvToast: {
          toast: vitest.fn(),
        } as any,
      });

      mountOptions = {
        stubs: {
          jhiItemCount: true,
          bPagination: true,
          bModal: bModalStub as any,
          'font-awesome-icon': true,
          'b-badge': true,
          'jhi-sort-indicator': true,
          'b-button': true,
          'router-link': true,
        },
        directives: {
          'b-modal': {},
        },
        provide: {
          alertService,
          seatingTableService: () => seatingTableServiceStub,
        },
      };
    });

    describe('Mount', () => {
      it('Should call load all on init', async () => {
        // GIVEN
        seatingTableServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        const wrapper = shallowMount(SeatingTable, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.retrieve.calledOnce).toBeTruthy();
        expect(comp.seatingTables[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for an id', async () => {
        // WHEN
        const wrapper = shallowMount(SeatingTable, { global: mountOptions });
        const comp = wrapper.vm;
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['id,asc'],
        });
      });
    });
    describe('Handles', () => {
      let comp: SeatingTableComponentType;

      beforeEach(async () => {
        const wrapper = shallowMount(SeatingTable, { global: mountOptions });
        comp = wrapper.vm;
        await comp.$nextTick();
        seatingTableServiceStub.retrieve.reset();
        seatingTableServiceStub.retrieve.resolves({ headers: {}, data: [] });
      });

      it('should load a page', async () => {
        // GIVEN
        seatingTableServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.page = 2;
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.retrieve.called).toBeTruthy();
        expect(comp.seatingTables[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should not load a page if the page is the same as the previous page', () => {
        // WHEN
        comp.page = 1;

        // THEN
        expect(seatingTableServiceStub.retrieve.called).toBeFalsy();
      });

      it('should re-initialize the page', async () => {
        // GIVEN
        comp.page = 2;
        await comp.$nextTick();
        seatingTableServiceStub.retrieve.reset();
        seatingTableServiceStub.retrieve.resolves({ headers: {}, data: [{ id: 123 }] });

        // WHEN
        comp.clear();
        await comp.$nextTick();

        // THEN
        expect(comp.page).toEqual(1);
        expect(seatingTableServiceStub.retrieve.callCount).toEqual(1);
        expect(comp.seatingTables[0]).toEqual(expect.objectContaining({ id: 123 }));
      });

      it('should calculate the sort attribute for a non-id attribute', async () => {
        // WHEN
        comp.propOrder = 'name';
        await comp.$nextTick();

        // THEN
        expect(seatingTableServiceStub.retrieve.lastCall.firstArg).toMatchObject({
          sort: ['name,asc', 'id'],
        });
      });

      it('Should call delete service on confirmDelete', async () => {
        // GIVEN
        seatingTableServiceStub.delete.resolves({});

        // WHEN
        comp.prepareRemove({ id: 123 });

        comp.removeSeatingTable();
        await comp.$nextTick(); // clear components

        // THEN
        expect(seatingTableServiceStub.delete.called).toBeTruthy();

        // THEN
        await comp.$nextTick(); // handle component clear watch
        expect(seatingTableServiceStub.retrieve.callCount).toEqual(1);
      });
    });
  });
});
