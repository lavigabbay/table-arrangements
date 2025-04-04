<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="eventTable">
        <h2 class="jh-entity-heading" data-cy="eventTableDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.eventTable.detail.title')"></span> {{ eventTable.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.eventTable.tableNumber')"></span>
          </dt>
          <dd>
            <span>{{ eventTable.tableNumber }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.eventTable.maxSeats')"></span>
          </dt>
          <dd>
            <span>{{ eventTable.maxSeats }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.eventTable.venue')"></span>
          </dt>
          <dd>
            <div v-if="eventTable.venue">
              <router-link :to="{ name: 'VenueTableView', params: { venueTableId: eventTable.venue.id } }">{{
                eventTable.venue.venueName
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link
          v-if="eventTable.id"
          :to="{ name: 'EventTableEdit', params: { eventTableId: eventTable.id } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.edit')"></span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./event-table-details.component.ts"></script>
