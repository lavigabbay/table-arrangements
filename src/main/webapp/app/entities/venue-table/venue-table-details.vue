<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="venueTable">
        <h2 class="jh-entity-heading" data-cy="venueTableDetailsHeading">
          <span v-text="t$('tableArrangmentsApp.venueTable.detail.title')"></span> {{ venueTable.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.numberOfTables')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.numberOfTables }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.nearStageTables')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.nearStageTables }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.venueName')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.venueName }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.eventOwners')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.eventOwners }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.groomParents')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.groomParents }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.brideParents')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.brideParents }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.weddingDate')"></span>
          </dt>
          <dd>
            <span>{{ venueTable.weddingDate }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.receptionTime')"></span>
          </dt>
          <dd>
            <span v-if="venueTable.receptionTime">{{ formatDateLong(venueTable.receptionTime) }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.weddingTime')"></span>
          </dt>
          <dd>
            <span v-if="venueTable.weddingTime">{{ formatDateLong(venueTable.weddingTime) }}</span>
          </dd>
          <dt>
            <span v-text="t$('tableArrangmentsApp.venueTable.user')"></span>
          </dt>
          <dd>
            {{ venueTable.user ? venueTable.user.login : '' }}
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.back')"></span>
        </button>
        <router-link
          v-if="venueTable.id"
          :to="{ name: 'VenueTableEdit', params: { venueTableId: venueTable.id } }"
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

<script lang="ts" src="./venue-table-details.component.ts"></script>
