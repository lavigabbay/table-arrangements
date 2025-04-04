<template>
  <div>
    <h2 id="page-heading" data-cy="EventTableHeading">
      <span v-text="t$('tableArrangmentsApp.eventTable.home.title')" id="event-table-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.eventTable.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'EventTableCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-event-table"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('tableArrangmentsApp.eventTable.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && eventTables && eventTables.length === 0">
      <span v-text="t$('tableArrangmentsApp.eventTable.home.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="eventTables && eventTables.length > 0">
      <table class="table table-striped" aria-describedby="eventTables">
        <thead>
          <tr>
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('tableNumber')">
              <span v-text="t$('tableArrangmentsApp.eventTable.tableNumber')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tableNumber'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('maxSeats')">
              <span v-text="t$('tableArrangmentsApp.eventTable.maxSeats')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'maxSeats'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('venue.venueName')">
              <span v-text="t$('tableArrangmentsApp.eventTable.venue')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venue.venueName'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="eventTable in eventTables" :key="eventTable.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'EventTableView', params: { eventTableId: eventTable.id } }">{{ eventTable.id }}</router-link>
            </td>
            <td>{{ eventTable.tableNumber }}</td>
            <td>{{ eventTable.maxSeats }}</td>
            <td>
              <div v-if="eventTable.venue">
                <router-link :to="{ name: 'VenueTableView', params: { venueTableId: eventTable.venue.id } }">{{
                  eventTable.venue.venueName
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'EventTableView', params: { eventTableId: eventTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'EventTableEdit', params: { eventTableId: eventTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(eventTable)"
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
          id="tableArrangmentsApp.eventTable.delete.question"
          data-cy="eventTableDeleteDialogHeading"
          v-text="t$('entity.delete.title')"
        ></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-eventTable-heading" v-text="t$('tableArrangmentsApp.eventTable.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-eventTable"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeEventTable()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="eventTables && eventTables.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./event-table.component.ts"></script>
