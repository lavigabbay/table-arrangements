<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="event">
        <h2 class="jh-entity-heading" data-cy="eventDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.event.detail.title')"></span> {{ event.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.eventName')"></span>
          </dt>
          <dd>
            <span>{{ event.eventName }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.eventOwners')"></span>
          </dt>
          <dd>
            <span>{{ event.eventOwners }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.groomParents')"></span>
          </dt>
          <dd>
            <span>{{ event.groomParents }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.brideParents')"></span>
          </dt>
          <dd>
            <span>{{ event.brideParents }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.weddingDate')"></span>
          </dt>
          <dd>
            <span>{{ event.weddingDate }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.receptionTime')"></span>
          </dt>
          <dd>
            <span v-if="event.receptionTime">{{ formatDateLong(event.receptionTime) }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.weddingTime')"></span>
          </dt>
          <dd>
            <span v-if="event.weddingTime">{{ formatDateLong(event.weddingTime) }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.event.user')"></span>
          </dt>
          <dd>
            {{ event.user ? event.user.login : '' }}
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link v-if="event.id" :to="{ name: 'EventEdit', params: { eventId: event.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.edit')"></span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./event-details.component.ts"></script>
