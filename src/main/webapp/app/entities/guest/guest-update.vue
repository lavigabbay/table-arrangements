<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.guest.home.createOrEditLabel"
          data-cy="GuestCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.guest.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="guest.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="guest.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.guest.lastNameAndFirstName')"
              for="guest-lastNameAndFirstName"
            ></label>
            <input
              type="text"
              class="form-control"
              name="lastNameAndFirstName"
              id="guest-lastNameAndFirstName"
              data-cy="lastNameAndFirstName"
              :class="{ valid: !v$.lastNameAndFirstName.$invalid, invalid: v$.lastNameAndFirstName.$invalid }"
              v-model="v$.lastNameAndFirstName.$model"
              required
            />
            <div v-if="v$.lastNameAndFirstName.$anyDirty && v$.lastNameAndFirstName.$invalid">
              <small class="form-text text-danger" v-for="error of v$.lastNameAndFirstName.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.numberOfSeats')" for="guest-numberOfSeats"></label>
            <input
              type="number"
              class="form-control"
              name="numberOfSeats"
              id="guest-numberOfSeats"
              data-cy="numberOfSeats"
              :class="{ valid: !v$.numberOfSeats.$invalid, invalid: v$.numberOfSeats.$invalid }"
              v-model.number="v$.numberOfSeats.$model"
              required
            />
            <div v-if="v$.numberOfSeats.$anyDirty && v$.numberOfSeats.$invalid">
              <small class="form-text text-danger" v-for="error of v$.numberOfSeats.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.phone')" for="guest-phone"></label>
            <input
              type="text"
              class="form-control"
              name="phone"
              id="guest-phone"
              data-cy="phone"
              :class="{ valid: !v$.phone.$invalid, invalid: v$.phone.$invalid }"
              v-model="v$.phone.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.nearStage')" for="guest-nearStage"></label>
            <input
              type="checkbox"
              class="form-check"
              name="nearStage"
              id="guest-nearStage"
              data-cy="nearStage"
              :class="{ valid: !v$.nearStage.$invalid, invalid: v$.nearStage.$invalid }"
              v-model="v$.nearStage.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.status')" for="guest-status"></label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !v$.status.$invalid, invalid: v$.status.$invalid }"
              v-model="v$.status.$model"
              id="guest-status"
              data-cy="status"
              required
            >
              <option
                v-for="guestStatus in guestStatusValues"
                :key="guestStatus"
                :value="guestStatus"
                :label="t$('tableArrangmentsApp.GuestStatus.' + guestStatus)"
              >
                {{ guestStatus }}
              </option>
            </select>
            <div v-if="v$.status.$anyDirty && v$.status.$invalid">
              <small class="form-text text-danger" v-for="error of v$.status.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.side')" for="guest-side"></label>
            <select
              class="form-control"
              name="side"
              :class="{ valid: !v$.side.$invalid, invalid: v$.side.$invalid }"
              v-model="v$.side.$model"
              id="guest-side"
              data-cy="side"
            >
              <option
                v-for="guestSide in guestSideValues"
                :key="guestSide"
                :value="guestSide"
                :label="t$('tableArrangmentsApp.GuestSide.' + guestSide)"
              >
                {{ guestSide }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.relation')" for="guest-relation"></label>
            <select
              class="form-control"
              name="relation"
              :class="{ valid: !v$.relation.$invalid, invalid: v$.relation.$invalid }"
              v-model="v$.relation.$model"
              id="guest-relation"
              data-cy="relation"
              required
            >
              <option
                v-for="guestRelation in guestRelationValues"
                :key="guestRelation"
                :value="guestRelation"
                :label="t$('tableArrangmentsApp.GuestRelation.' + guestRelation)"
              >
                {{ guestRelation }}
              </option>
            </select>
            <div v-if="v$.relation.$anyDirty && v$.relation.$invalid">
              <small class="form-text text-danger" v-for="error of v$.relation.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.event')" for="guest-event"></label>
            <select
              class="form-control"
              id="guest-event"
              data-cy="event"
              name="event"
              v-model="v$.event.$model"
              :class="{ valid: !v$.event.$invalid, invalid: v$.event.$invalid }"
              required
            >
              <option :value="null"></option>
              <option
                :value="guest.event && eventOption.id === guest.event.id ? guest.event : eventOption"
                v-for="eventOption in events"
                :key="eventOption.id"
              >
                {{ eventOption.eventName }}
              </option>
            </select>
            <div v-if="v$.event.$anyDirty && v$.event.$invalid">
              <small class="form-text text-danger" v-for="error of v$.event.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>

          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guest.table')" for="guest-table"></label>
            <select class="form-control" id="guest-table" data-cy="table" name="table" v-model="guest.table">
              <option :value="null"></option>
              <option
                :value="guest.table && seatingTableOption.id === guest.table.id ? guest.table : seatingTableOption"
                v-for="seatingTableOption in seatingTables"
                :key="seatingTableOption.id"
              >
                {{ seatingTableOption.tableNumber }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label v-text="t$('tableArrangmentsApp.guest.avoidGuests')" for="guest-avoidGuests"></label>
            <select
              class="form-control"
              id="guest-avoidGuests"
              data-cy="avoidGuests"
              multiple
              name="avoidGuests"
              v-if="guest.avoidGuests !== undefined"
              v-model="guest.avoidGuests"
            >
              <option :value="getSelected(guest.avoidGuests, guestOption, 'id')" v-for="guestOption in guests" :key="guestOption.id">
                {{ guestOption.lastNameAndFirstName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label v-text="t$('tableArrangmentsApp.guest.preferGuests')" for="guest-preferGuests"></label>
            <select
              class="form-control"
              id="guest-preferGuests"
              data-cy="preferGuests"
              multiple
              name="preferGuests"
              v-if="guest.preferGuests !== undefined"
              v-model="guest.preferGuests"
            >
              <option :value="getSelected(guest.preferGuests, guestOption, 'id')" v-for="guestOption in guests" :key="guestOption.id">
                {{ guestOption.lastNameAndFirstName }}
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
<script lang="ts" src="./guest-update.component.ts"></script>
