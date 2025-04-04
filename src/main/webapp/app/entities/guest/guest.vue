<template>
  <div>
    <h2 id="page-heading" data-cy="GuestHeading">
      <span v-text="t$('tableArrangmentsApp.guest.home.title')" id="guest-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.guest.home.refreshListLabel')"></span>
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
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('lastNameAndFirstName')">
              <span v-text="t$('tableArrangmentsApp.guest.lastNameAndFirstName')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastNameAndFirstName'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('numberOfSeats')">
              <span v-text="t$('tableArrangmentsApp.guest.numberOfSeats')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'numberOfSeats'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('phone')">
              <span v-text="t$('tableArrangmentsApp.guest.phone')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'phone'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('nearStage')">
              <span v-text="t$('tableArrangmentsApp.guest.nearStage')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nearStage'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('status')">
              <span v-text="t$('tableArrangmentsApp.guest.status')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('side')">
              <span v-text="t$('tableArrangmentsApp.guest.side')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'side'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('relation')">
              <span v-text="t$('tableArrangmentsApp.guest.relation')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'relation'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('accessibility')">
              <span v-text="t$('tableArrangmentsApp.guest.accessibility')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'accessibility'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('event.eventName')">
              <span v-text="t$('tableArrangmentsApp.guest.event')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'event.eventName'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('table.tableNumber')">
              <span v-text="t$('tableArrangmentsApp.guest.table')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'table.tableNumber'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="guest in guests" :key="guest.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'GuestView', params: { guestId: guest.id } }">{{ guest.id }}</router-link>
            </td>
            <td>{{ guest.lastNameAndFirstName }}</td>
            <td>{{ guest.numberOfSeats }}</td>
            <td>{{ guest.phone }}</td>
            <td>{{ guest.nearStage }}</td>
            <td v-text="t$('tableArrangmentsApp.GuestStatus.' + guest.status)"></td>
            <td v-text="t$('tableArrangmentsApp.GuestSide.' + guest.side)"></td>
            <td v-text="t$('tableArrangmentsApp.GuestRelation.' + guest.relation)"></td>
            <td>{{ guest.accessibility }}</td>
            <td>
              <div v-if="guest.event">
                <router-link :to="{ name: 'EventView', params: { eventId: guest.event.id } }">{{ guest.event.eventName }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="guest.table">
                <router-link :to="{ name: 'SeatingTableView', params: { seatingTableId: guest.table.id } }">{{
                  guest.table.tableNumber
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'GuestView', params: { guestId: guest.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'GuestEdit', params: { guestId: guest.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(guest)"
                  variant="danger"
                  class="btn btn-sm"
                  data-cy="entityDeleteButton"
                  v-b-modal.removeEntity
                >
                  <font-awesome-icon icon="times"></font-awesome-icon>
                  <span class="d-none d-md-inline" v-text="t$('entity.action.delete')"></span>
                </b-button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <b-modal ref="removeEntity" id="removeEntity">
      <template #modal-title>
        <span id="tableArrangmentsApp.guest.delete.question" data-cy="guestDeleteDialogHeading" v-text="t$('entity.delete.title')"></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-guest-heading" v-text="t$('tableArrangmentsApp.guest.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-guest"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeGuest()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="guests && guests.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./guest.component.ts"></script>
