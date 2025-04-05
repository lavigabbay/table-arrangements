<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="seatingTable">
        <h2 class="jh-entity-heading" data-cy="seatingTableDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.seatingTable.detail.title')"></span> {{ seatingTable.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.seatingTable.tableNumber')"></span>
          </dt>
          <dd>
            <span>{{ seatingTable.tableNumber }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.seatingTable.maxSeats')"></span>
          </dt>
          <dd>
            <span>{{ seatingTable.maxSeats }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.seatingTable.nearStage')"></span>
          </dt>
          <dd>
            <span>{{ seatingTable.nearStage }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.seatingTable.accessibility')"></span>
          </dt>
          <dd>
            <span>{{ seatingTable.accessibility }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.seatingTable.event')"></span>
          </dt>
          <dd>
            <div v-if="seatingTable.event">
              <router-link :to="{ name: 'EventView', params: { eventId: seatingTable.event.id } }">{{
                seatingTable.event.eventName
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link
          v-if="seatingTable.id"
          :to="{ name: 'SeatingTableEdit', params: { seatingTableId: seatingTable.id } }"
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

<script lang="ts" src="./seating-table-details.component.ts"></script>
