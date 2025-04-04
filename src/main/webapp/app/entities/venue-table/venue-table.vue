<template>
  <div>
    <h2 id="page-heading" data-cy="VenueTableHeading">
      <span v-text="t$('tableArrangmentsApp.venueTable.home.title')" id="venue-table-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('tableArrangmentsApp.venueTable.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'VenueTableCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            id="jh-create-entity"
            data-cy="entityCreateButton"
            class="btn btn-primary jh-create-entity create-venue-table"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('tableArrangmentsApp.venueTable.home.createLabel')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && venueTables && venueTables.length === 0">
      <span v-text="t$('tableArrangmentsApp.venueTable.home.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="venueTables && venueTables.length > 0">
      <table class="table table-striped" aria-describedby="venueTables">
        <thead>
          <tr>
            <th scope="row" @click="changeOrder('id')">
              <span v-text="t$('global.field.id')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('numberOfTables')">
              <span v-text="t$('tableArrangmentsApp.venueTable.numberOfTables')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'numberOfTables'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('nearStageTables')">
              <span v-text="t$('tableArrangmentsApp.venueTable.nearStageTables')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'nearStageTables'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('venueName')">
              <span v-text="t$('tableArrangmentsApp.venueTable.venueName')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'venueName'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('eventOwners')">
              <span v-text="t$('tableArrangmentsApp.venueTable.eventOwners')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'eventOwners'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('groomParents')">
              <span v-text="t$('tableArrangmentsApp.venueTable.groomParents')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'groomParents'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('brideParents')">
              <span v-text="t$('tableArrangmentsApp.venueTable.brideParents')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'brideParents'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('weddingDate')">
              <span v-text="t$('tableArrangmentsApp.venueTable.weddingDate')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'weddingDate'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('receptionTime')">
              <span v-text="t$('tableArrangmentsApp.venueTable.receptionTime')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'receptionTime'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('weddingTime')">
              <span v-text="t$('tableArrangmentsApp.venueTable.weddingTime')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'weddingTime'"></jhi-sort-indicator>
            </th>
            <th scope="row" @click="changeOrder('user.login')">
              <span v-text="t$('tableArrangmentsApp.venueTable.user')"></span>
              <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'user.login'"></jhi-sort-indicator>
            </th>
            <th scope="row"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="venueTable in venueTables" :key="venueTable.id" data-cy="entityTable">
            <td>
              <router-link :to="{ name: 'VenueTableView', params: { venueTableId: venueTable.id } }">{{ venueTable.id }}</router-link>
            </td>
            <td>{{ venueTable.numberOfTables }}</td>
            <td>{{ venueTable.nearStageTables }}</td>
            <td>{{ venueTable.venueName }}</td>
            <td>{{ venueTable.eventOwners }}</td>
            <td>{{ venueTable.groomParents }}</td>
            <td>{{ venueTable.brideParents }}</td>
            <td>{{ venueTable.weddingDate }}</td>
            <td>{{ formatDateShort(venueTable.receptionTime) || '' }}</td>
            <td>{{ formatDateShort(venueTable.weddingTime) || '' }}</td>
            <td>
              {{ venueTable.user ? venueTable.user.login : '' }}
            </td>
            <td class="text-right">
              <div class="btn-group">
                <router-link :to="{ name: 'VenueTableView', params: { venueTableId: venueTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'VenueTableEdit', params: { venueTableId: venueTable.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entity.action.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  @click="prepareRemove(venueTable)"
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
          id="tableArrangmentsApp.venueTable.delete.question"
          data-cy="venueTableDeleteDialogHeading"
          v-text="t$('entity.delete.title')"
        ></span>
      </template>
      <div class="modal-body">
        <p id="jhi-delete-venueTable-heading" v-text="t$('tableArrangmentsApp.venueTable.delete.question', { id: removeId })"></p>
      </div>
      <template #modal-footer>
        <div>
          <button type="button" class="btn btn-secondary" v-text="t$('entity.action.cancel')" @click="closeDialog()"></button>
          <button
            type="button"
            class="btn btn-primary"
            id="jhi-confirm-delete-venueTable"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entity.action.delete')"
            @click="removeVenueTable()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="venueTables && venueTables.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="md" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./venue-table.component.ts"></script>
