<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="guest">
        <h2 class="jh-entity-heading" data-cy="guestDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.guest.detail.title')"></span> {{ guest.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.lastNameAndFirstName')"></span>
          </dt>
          <dd>
            <span>{{ guest.lastNameAndFirstName }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.numberOfSeats')"></span>
          </dt>
          <dd>
            <span>{{ guest.numberOfSeats }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.phone')"></span>
          </dt>
          <dd>
            <span>{{ guest.phone }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.nearStage')"></span>
          </dt>
          <dd>
            <span>{{ guest.nearStage }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.status')"></span>
          </dt>
          <dd>
            <span v-text="t$('tableArrangmentsApp.GuestStatus.' + guest.status)"></span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.side')"></span>
          </dt>
          <dd>
            <span v-text="t$('tableArrangmentsApp.GuestSide.' + guest.side)"></span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.relation')"></span>
          </dt>
          <dd>
            <span v-text="t$('tableArrangmentsApp.GuestRelation.' + guest.relation)"></span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.accessibility')"></span>
          </dt>
          <dd>
            <span>{{ guest.accessibility }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.event')"></span>
          </dt>
          <dd>
            <div v-if="guest.event">
              <router-link :to="{ name: 'EventView', params: { eventId: guest.event.id } }">{{ guest.event.eventName }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.table')"></span>
          </dt>
          <dd>
            <div v-if="guest.table">
              <router-link :to="{ name: 'SeatingTableView', params: { seatingTableId: guest.table.id } }">{{
                guest.table.tableNumber
              }}</router-link>
            </div>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.avoidGuests')"></span>
          </dt>
          <dd>
            <span v-for="(avoidGuests, i) in guest.avoidGuests" :key="avoidGuests.id"
              >{{ i > 0 ? ', ' : '' }}
              <router-link :to="{ name: 'GuestView', params: { guestId: avoidGuests.id } }">{{
                avoidGuests.lastNameAndFirstName
              }}</router-link>
            </span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.guest.preferGuests')"></span>
          </dt>
          <dd>
            <span v-for="(preferGuests, i) in guest.preferGuests" :key="preferGuests.id"
              >{{ i > 0 ? ', ' : '' }}
              <router-link :to="{ name: 'GuestView', params: { guestId: preferGuests.id } }">{{
                preferGuests.lastNameAndFirstName
              }}</router-link>
            </span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link v-if="guest.id" :to="{ name: 'GuestEdit', params: { guestId: guest.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.edit')"></span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./guest-details.component.ts"></script>
