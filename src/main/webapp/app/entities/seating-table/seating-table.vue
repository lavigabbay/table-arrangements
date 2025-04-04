<template>
  <div>
    <h2 id="page-heading" data-cy="SeatingTableHeading">
      <span v-text="t$('tableArrangmentsApp.seatingTable.home.title')" id="seating-table-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.seatingTable.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'SeatingTableCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-seating-table"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('tableArrangmentsApp.seatingTable.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && seatingTables && seatingTables.length === 0">
      <span v-text="t$('tableArrangmentsApp.seatingTable.home.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="seatingTables && seatingTables.length > 0">
      <table class="table table-striped" aria-describedby="seatingTables">
        <thead>
          <tr>
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('tableNumber')">
              <span v-text="t$('tableArrangmentsApp.seatingTable.tableNumber')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'tableNumber'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('maxSeats')">
              <span v-text="t$('tableArrangmentsApp.seatingTable.maxSeats')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'maxSeats'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('nearStage')">
              <span v-text="t$('tableArrangmentsApp.seatingTable.nearStage')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nearStage'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('event.eventName')">
              <span v-text="t$('tableArrangmentsApp.seatingTable.event')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'event.eventName'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="seatingTable in seatingTables" :key="seatingTable.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'SeatingTableView', params: { seatingTableId: seatingTable.id } }">{{
                seatingTable.id
              }}</router-link>
            </td>
            <td>{{ seatingTable.tableNumber }}</td>
            <td>{{ seatingTable.maxSeats }}</td>
            <td>{{ seatingTable.nearStage }}</td>
            <td>
              <div v-if="seatingTable.event">
                <router-link :to="{ name: 'EventView', params: { eventId: seatingTable.event.id } }">{{
                  seatingTable.event.eventName
                }}</router-link>
              </div>
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'SeatingTableView', params: { seatingTableId: seatingTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'SeatingTableEdit', params: { seatingTableId: seatingTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(seatingTable)"
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
          id="tableArrangmentsApp.seatingTable.delete.question"
          data-cy="seatingTableDeleteDialogHeading"
          v-text="t$('entity.delete.title')"
        ></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-seatingTable-heading" v-text="t$('tableArrangmentsApp.seatingTable.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-seatingTable"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeSeatingTable()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="seatingTables && seatingTables.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./seating-table.component.ts"></script>
