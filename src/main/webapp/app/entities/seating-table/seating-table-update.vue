<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.seatingTable.home.createOrEditLabel"
          data-cy="SeatingTableCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.seatingTable.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="seatingTable.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="seatingTable.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.seatingTable.tableNumber')"
              for="seating-table-tableNumber"
            ></label>
            <input
              type="number"
              class="form-control"
              name="tableNumber"
              id="seating-table-tableNumber"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.seatingTable.maxSeats')" for="seating-table-maxSeats"></label>
            <input
              type="number"
              class="form-control"
              name="maxSeats"
              id="seating-table-maxSeats"
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
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.seatingTable.nearStage')"
              for="seating-table-nearStage"
            ></label>
            <input
              type="checkbox"
              class="form-check"
              name="nearStage"
              id="seating-table-nearStage"
              data-cy="nearStage"
              :class="{ valid: !v$.nearStage.$invalid, invalid: v$.nearStage.$invalid }"
              v-model="v$.nearStage.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.seatingTable.event')" for="seating-table-event"></label>
            <select class="form-control" id="seating-table-event" data-cy="event" name="event" v-model="seatingTable.event">
              <option :value="null"></option>
              <option
                :value="seatingTable.event && eventOption.id === seatingTable.event.id ? seatingTable.event : eventOption"
                v-for="eventOption in events"
                :key="eventOption.id"
              >
                {{ eventOption.eventName }}
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
<script lang="ts" src="./seating-table-update.component.ts"></script>
