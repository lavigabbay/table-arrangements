<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="guestTable">
        <h2 class="jh-entity-heading" data-cy="guestTableDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.guestTable.detail.title')"></span> {{ guestTable.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.lastNameAndFirstName')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.lastNameAndFirstName }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.numberOfSeats')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.numberOfSeats }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.phone')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.phone }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.nearDanceFloor')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.nearDanceFloor }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.status')"></span>
          </dt>
          <dd>
            <span v-text="t$('tableArrangmentsApp.GuestStatus.' + guestTable.status)"></span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.side')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.side }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.relation')"></span>
          </dt>
          <dd>
            <span v-text="t$('tableArrangmentsApp.GuestRelation.' + guestTable.relation)"></span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.notWithId')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.notWithId }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.withId')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.withId }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.conditions')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.conditions }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.accessibility')"></span>
          </dt>
          <dd>
            <span>{{ guestTable.accessibility }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.venueName')"></span>
          </dt>
          <dd>
            <div v-if="guestTable.venueName">
              <router-link :to="{ name: 'VenueTableView', params: { venueTableId: guestTable.venueName.id } }">{{
                guestTable.venueName.venueName
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guestTable.eventTable')"></span>
          </dt>
          <dd>
            <div v-if="guestTable.eventTable">
              <router-link :to="{ name: 'EventTableView', params: { eventTableId: guestTable.eventTable.id } }">{{
                guestTable.eventTable.tableNumber
              }}</router-link>
            </div>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link
          v-if="guestTable.id"
          :to="{ name: 'GuestTableEdit', params: { guestTableId: guestTable.id } }"
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

<script lang="ts" src="./guest-table-details.component.ts"></script>
