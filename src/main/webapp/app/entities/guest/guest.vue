<template>
  <div>
    <h2 id="page-heading" data-cy="GuestHeading">
      <span v-text="t$('tableArrangmentsApp.guest.home.title')" id="guest-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.guest.home.refreshListLabel')"></span>
        </button>

        <!-- כפתור מיון רנדומלי -->
        <button class="btn btn-outline-primary mr-2" @click="sortGuestsRandomly" :disabled="isFetching">
          <font-awesome-icon icon="random" />
          מיין אורחים רנדומלית
        </button>

        <router-link :to="{ name: 'GuestCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-guest"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('tableArrangmentsApp.guest.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>

    <br />

    <div class="alert alert-warning" v-if="!isFetching && guests && guests.length === 0">
      <span v-text="t$('tableArrangmentsApp.guest.home.notFound')"></span>
    </div>

    <div class="table-responsive" v-if="guests && guests.length > 0">
      <table class="table table-striped" aria-describedby="guests">
        <thead>
          <tr>
            <th @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="id" />
            </th>
            <th @click="changeOrder('lastNameAndFirstName')">
              <span v-text="t$('tableArrangmentsApp.guest.lastNameAndFirstName')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="lastNameAndFirstName" />
            </th>
            <th @click="changeOrder('numberOfSeats')">
              <span v-text="t$('tableArrangmentsApp.guest.numberOfSeats')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="numberOfSeats" />
            </th>
            <th @click="changeOrder('phone')">
              <span v-text="t$('tableArrangmentsApp.guest.phone')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="phone" />
            </th>
            <th @click="changeOrder('nearStage')">
              <span v-text="t$('tableArrangmentsApp.guest.nearStage')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="nearStage" />
            </th>
            <th @click="changeOrder('status')">
              <span v-text="t$('tableArrangmentsApp.guest.status')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="status" />
            </th>
            <th @click="changeOrder('side')">
              <span v-text="t$('tableArrangmentsApp.guest.side')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="side" />
            </th>
            <th @click="changeOrder('relation')">
              <span v-text="t$('tableArrangmentsApp.guest.relation')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="relation" />
            </th>
            <th @click="changeOrder('accessibility')">
              <span v-text="t$('tableArrangmentsApp.guest.accessibility')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="accessibility" />
            </th>
            <th @click="changeOrder('event.eventName')">
              <span v-text="t$('tableArrangmentsApp.guest.event')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="event.eventName" />
            </th>
            <th @click="changeOrder('table.tableNumber')">
              <span v-text="t$('tableArrangmentsApp.guest.table')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" field-name="table.tableNumber" />
            </th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="guest in guests" :key="guest.id">
            <td>
              <router-link :to="{ name: 'GuestView', params: { guestId: guest.id } }">{{ guest.id }}</router-link>
            </td>
            <td>{{ guest.lastNameAndFirstName }}</td>
            <td>{{ guest.numberOfSeats }}</td>
            <td>{{ guest.phone }}</td>
            <td>{{ guest.nearStage }}</td>
            <td>{{ t$('tableArrangmentsApp.GuestStatus.' + guest.status) }}</td>
            <td>{{ t$('tableArrangmentsApp.GuestSide.' + guest.side) }}</td>
            <td>{{ t$('tableArrangmentsApp.GuestRelation.' + guest.relation) }}</td>
            <td>{{ guest.accessibility }}</td>
            <td>
              <router-link v-if="guest.event" :to="{ name: 'EventView', params: { eventId: guest.event.id } }">
                {{ guest.event.eventName }}
              </router-link>
            </td>
            <td>
              <router-link v-if="guest.table" :to="{ name: 'SeatingTableView', params: { seatingTableId: guest.table.id } }">
                {{ guest.table.tableNumber }}
              </router-link>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'GuestView', params: { guestId: guest.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm">
                    <font-awesome-icon icon="eye" />
                    <span class="d-none d-md-inline">{{ t$('entity.action.view') }}</span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'GuestEdit', params: { guestId: guest.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm">
                    <font-awesome-icon icon="pencil-alt" />
                    <span class="d-none d-md-inline">{{ t$('entity.action.edit') }}</span>
                  </button>
                </router-link>
                <b-button @click="prepareRemove(guest)" variant="danger" class="btn btn-sm" v-b-modal.removeEntity>
                  <font-awesome-icon icon="times" />
                  <span class="d-none d-md-inline">{{ t$('entity.action.delete') }}</span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <b-modal ref="removeEntity" id="removeEntity">
      <template #modal-title>
        <span>{{ t$('entity.delete.title') }}</span>
      </template>
      <div class="modal-body">
        <p>{{ t$('tableArrangmentsApp.guest.delete.question', { id: removeId }) }}</p>
      </div>
      <template #modal-footer>
        <button type="button" class="btn btn-secondary" @click="closeDialog()">{{ t$('entity.action.cancel') }}</button>
        <button type="button" class="btn btn-primary" id="jhi-confirm-delete-guest" @click="removeGuest()">
          {{ t$('entity.action.delete') }}
        </button>
      </template>
    </b-modal>

    <div v-show="guests && guests.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage" />
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./guest.component.ts"></script>
