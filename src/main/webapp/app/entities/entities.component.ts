import { defineComponent, provide } from 'vue';

import EventService from './event/event.service';
import GuestService from './guest/guest.service';
import SeatingTableService from './seating-table/seating-table.service';
import UserService from '@/entities/user/user.service';
// jhipster-needle-add-entity-service-to-entities-component-import - JHipster will import entities services here

export default defineComponent({
  compatConfig: { MODE: 3 },
  name: 'Entities',
  setup() {
    provide('userService', () => new UserService());
    provide('eventService', () => new EventService());
    provide('guestService', () => new GuestService());
    provide('seatingTableService', () => new SeatingTableService());
    // jhipster-needle-add-entity-service-to-entities-component - JHipster will import entities services here
  },
});
