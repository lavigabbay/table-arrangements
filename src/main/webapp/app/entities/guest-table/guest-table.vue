<template>
  <div>
    <h2 id="page-heading" data-cy="GuestTableHeading">
      <span v-text="t$('tableArrangmentsApp.guestTable.home.title')" id="guest-table-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.guestTable.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'GuestTableCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-guest-table"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('tableArrangmentsApp.guestTable.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && guestTables && guestTables.length === 0">
      <span v-text="t$('tableArrangmentsApp.guestTable.home.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="guestTables && guestTables.length > 0">
      <table class="table table-striped" aria-describedby="guestTables">
        <thead>
          <tr>
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('lastNameAndFirstName')">
              <span v-text="t$('tableArrangmentsApp.guestTable.lastNameAndFirstName')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastNameAndFirstName'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('numberOfSeats')">
              <span v-text="t$('tableArrangmentsApp.guestTable.numberOfSeats')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'numberOfSeats'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('phone')">
              <span v-text="t$('tableArrangmentsApp.guestTable.phone')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'phone'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('nearDanceFloor')">
              <span v-text="t$('tableArrangmentsApp.guestTable.nearDanceFloor')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nearDanceFloor'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('status')">
              <span v-text="t$('tableArrangmentsApp.guestTable.status')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'status'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('side')">
              <span v-text="t$('tableArrangmentsApp.guestTable.side')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'side'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('relation')">
              <span v-text="t$('tableArrangmentsApp.guestTable.relation')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'relation'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('notWithId')">
              <span v-text="t$('tableArrangmentsApp.guestTable.notWithId')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'notWithId'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('withId')">
              <span v-text="t$('tableArrangmentsApp.guestTable.withId')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'withId'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('conditions')">
              <span v-text="t$('tableArrangmentsApp.guestTable.conditions')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'conditions'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('accessibility')">
              <span v-text="t$('tableArrangmentsApp.guestTable.accessibility')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'accessibility'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('venueName.venueName')">
              <span v-text="t$('tableArrangmentsApp.guestTable.venueName')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venueName.venueName'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('eventTable.tableNumber')">
              <span v-text="t$('tableArrangmentsApp.guestTable.eventTable')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'eventTable.tableNumber'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="guestTable in guestTables" :key="guestTable.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'GuestTableView', params: { guestTableId: guestTable.id } }">{{ guestTable.id }}</router-link>
            </td>
            <td>{{ guestTable.lastNameAndFirstName }}</td>
            <td>{{ guestTable.numberOfSeats }}</td>
            <td>{{ guestTable.phone }}</td>
            <td>{{ guestTable.nearDanceFloor }}</td>
            <td v-text="t$('tableArrangmentsApp.GuestStatus.' + guestTable.status)"></td>
            <td>{{ guestTable.side }}</td>
            <td v-text="t$('tableArrangmentsApp.GuestRelation.' + guestTable.relation)"></td>
            <td>{{ guestTable.notWithId }}</td>
            <td>{{ guestTable.withId }}</td>
            <td>{{ guestTable.conditions }}</td>
            <td>{{ guestTable.accessibility }}</td>
            <td>
              <div v-if="guestTable.venueName">
                <router-link :to="{ name: 'VenueTableView', params: { venueTableId: guestTable.venueName.id } }">{{
                  guestTable.venueName.venueName
                }}</router-link>
              </div>
            </td>
            <td>
              <div v-if="guestTable.eventTable">
                <router-link :to="{ name: 'EventTableView', params: { eventTableId: guestTable.eventTable.id } }">{{
                  guestTable.eventTable.tableNumber
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'GuestTableView', params: { guestTableId: guestTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'GuestTableEdit', params: { guestTableId: guestTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(guestTable)"
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
        <span
          id="tableArrangmentsApp.guestTable.delete.question"
          data-cy="guestTableDeleteDialogHeading"
          v-text="t$('entity.delete.title')"
        ></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-guestTable-heading" v-text="t$('tableArrangmentsApp.guestTable.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-guestTable"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeGuestTable()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="guestTables && guestTables.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./guest-table.component.ts"></script>
