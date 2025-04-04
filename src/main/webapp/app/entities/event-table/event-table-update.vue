<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.eventTable.home.createOrEditLabel"
          data-cy="EventTableCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.eventTable.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="eventTable.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="eventTable.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.eventTable.tableNumber')"
              for="event-table-tableNumber"
            ></label>
            <input
              type="number"
              class="form-control"
              name="tableNumber"
              id="event-table-tableNumber"
              data-cy="tableNumber"
              :class="{ valid: !v$.tableNumber.$invalid, invalid: v$.tableNumber.$invalid }"
              v-model.number="v$.tableNumber.$model"
              required
            />
            <div v-if="v$.tableNumber.$anyDirty && v$.tableNumber.$invalid">
              <small class="form-text text-danger" v-for="error of v$.tableNumber.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.eventTable.maxSeats')" for="event-table-maxSeats"></label>
            <input
              type="number"
              class="form-control"
              name="maxSeats"
              id="event-table-maxSeats"
              data-cy="maxSeats"
              :class="{ valid: !v$.maxSeats.$invalid, invalid: v$.maxSeats.$invalid }"
              v-model.number="v$.maxSeats.$model"
              required
            />
            <div v-if="v$.maxSeats.$anyDirty && v$.maxSeats.$invalid">
              <small class="form-text text-danger" v-for="error of v$.maxSeats.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.eventTable.venue')" for="event-table-venue"></label>
            <select class="form-control" id="event-table-venue" data-cy="venue" name="venue" v-model="eventTable.venue">
              <option :value="null"></option>
              <option
                :value="eventTable.venue && venueTableOption.id === eventTable.venue.id ? eventTable.venue : venueTableOption"
                v-for="venueTableOption in venueTables"
                :key="venueTableOption.id"
              >
                {{ venueTableOption.venueName }}
              </option>
            </select>
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary" @click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.cancel')"></span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="v$.$invalid || isSaving"
            class="btn btn-primary"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
<script lang="ts" src="./event-table-update.component.ts"></script>
