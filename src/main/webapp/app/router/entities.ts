import { Authority } from '@/shared/security/authority';
const Entities = () => import('@/entities/entities.vue');

const Event = () => import('@/entities/event/event.vue');
const EventUpdate = () => import('@/entities/event/event-update.vue');
const EventDetails = () => import('@/entities/event/event-details.vue');

const Guest = () => import('@/entities/guest/guest.vue');
const GuestUpdate = () => import('@/entities/guest/guest-update.vue');
const GuestDetails = () => import('@/entities/guest/guest-details.vue');

const SeatingTable = () => import('@/entities/seating-table/seating-table.vue');
const SeatingTableUpdate = () => import('@/entities/seating-table/seating-table-update.vue');
const SeatingTableDetails = () => import('@/entities/seating-table/seating-table-details.vue');

// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default {
  path: '/',
  component: Entities,
  children: [
    {
      path: 'event',
      name: 'Event',
      component: Event,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'event/new',
      name: 'EventCreate',
      component: EventUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'event/:eventId/edit',
      name: 'EventEdit',
      component: EventUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'event/:eventId/view',
      name: 'EventView',
      component: EventDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'guest',
      name: 'Guest',
      component: Guest,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'guest/new',
      name: 'GuestCreate',
      component: GuestUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'guest/:guestId/edit',
      name: 'GuestEdit',
      component: GuestUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'guest/:guestId/view',
      name: 'GuestView',
      component: GuestDetails,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'seating-table',
      name: 'SeatingTable',
      component: SeatingTable,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'seating-table/new',
      name: 'SeatingTableCreate',
      component: SeatingTableUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'seating-table/:seatingTableId/edit',
      name: 'SeatingTableEdit',
      component: SeatingTableUpdate,
      meta: { authorities: [Authority.USER] },
    },
    {
      path: 'seating-table/:seatingTableId/view',
      name: 'SeatingTableView',
      component: SeatingTableDetails,
      meta: { authorities: [Authority.USER] },
    },
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ],
};
