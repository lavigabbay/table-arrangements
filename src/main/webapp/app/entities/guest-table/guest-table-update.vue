<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.guestTable.home.createOrEditLabel"
          data-cy="GuestTableCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.guestTable.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="guestTable.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="guestTable.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.guestTable.lastNameAndFirstName')"
              for="guest-table-lastNameAndFirstName"
            ></label>
            <input
              type="text"
              class="form-control"
              name="lastNameAndFirstName"
              id="guest-table-lastNameAndFirstName"
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
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.guestTable.numberOfSeats')"
              for="guest-table-numberOfSeats"
            ></label>
            <input
              type="number"
              class="form-control"
              name="numberOfSeats"
              id="guest-table-numberOfSeats"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.phone')" for="guest-table-phone"></label>
            <input
              type="text"
              class="form-control"
              name="phone"
              id="guest-table-phone"
              data-cy="phone"
              :class="{ valid: !v$.phone.$invalid, invalid: v$.phone.$invalid }"
              v-model="v$.phone.$model"
              required
            />
            <div v-if="v$.phone.$anyDirty && v$.phone.$invalid">
              <small class="form-text text-danger" v-for="error of v$.phone.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.guestTable.nearDanceFloor')"
              for="guest-table-nearDanceFloor"
            ></label>
            <input
              type="checkbox"
              class="form-check"
              name="nearDanceFloor"
              id="guest-table-nearDanceFloor"
              data-cy="nearDanceFloor"
              :class="{ valid: !v$.nearDanceFloor.$invalid, invalid: v$.nearDanceFloor.$invalid }"
              v-model="v$.nearDanceFloor.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.status')" for="guest-table-status"></label>
            <select
              class="form-control"
              name="status"
              :class="{ valid: !v$.status.$invalid, invalid: v$.status.$invalid }"
              v-model="v$.status.$model"
              id="guest-table-status"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.side')" for="guest-table-side"></label>
            <input
              type="text"
              class="form-control"
              name="side"
              id="guest-table-side"
              data-cy="side"
              :class="{ valid: !v$.side.$invalid, invalid: v$.side.$invalid }"
              v-model="v$.side.$model"
              required
            />
            <div v-if="v$.side.$anyDirty && v$.side.$invalid">
              <small class="form-text text-danger" v-for="error of v$.side.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.relation')" for="guest-table-relation"></label>
            <select
              class="form-control"
              name="relation"
              :class="{ valid: !v$.relation.$invalid, invalid: v$.relation.$invalid }"
              v-model="v$.relation.$model"
              id="guest-table-relation"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.notWithId')" for="guest-table-notWithId"></label>
            <input
              type="number"
              class="form-control"
              name="notWithId"
              id="guest-table-notWithId"
              data-cy="notWithId"
              :class="{ valid: !v$.notWithId.$invalid, invalid: v$.notWithId.$invalid }"
              v-model.number="v$.notWithId.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.withId')" for="guest-table-withId"></label>
            <input
              type="number"
              class="form-control"
              name="withId"
              id="guest-table-withId"
              data-cy="withId"
              :class="{ valid: !v$.withId.$invalid, invalid: v$.withId.$invalid }"
              v-model.number="v$.withId.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.conditions')" for="guest-table-conditions"></label>
            <input
              type="text"
              class="form-control"
              name="conditions"
              id="guest-table-conditions"
              data-cy="conditions"
              :class="{ valid: !v$.conditions.$invalid, invalid: v$.conditions.$invalid }"
              v-model="v$.conditions.$model"
            />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.guestTable.accessibility')"
              for="guest-table-accessibility"
            ></label>
            <input
              type="checkbox"
              class="form-check"
              name="accessibility"
              id="guest-table-accessibility"
              data-cy="accessibility"
              :class="{ valid: !v$.accessibility.$invalid, invalid: v$.accessibility.$invalid }"
              v-model="v$.accessibility.$model"
              required
            />
            <div v-if="v$.accessibility.$anyDirty && v$.accessibility.$invalid">
              <small class="form-text text-danger" v-for="error of v$.accessibility.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.venueName')" for="guest-table-venueName"></label>
            <select class="form-control" id="guest-table-venueName" data-cy="venueName" name="venueName" v-model="guestTable.venueName">
              <option :value="null"></option>
              <option
                :value="guestTable.venueName && venueTableOption.id === guestTable.venueName.id ? guestTable.venueName : venueTableOption"
                v-for="venueTableOption in venueTables"
                :key="venueTableOption.id"
              >
                {{ venueTableOption.venueName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.guestTable.eventTable')" for="guest-table-eventTable"></label>
            <select class="form-control" id="guest-table-eventTable" data-cy="eventTable" name="eventTable" v-model="guestTable.eventTable">
              <option :value="null"></option>
              <option
                :value="
                  guestTable.eventTable && eventTableOption.id === guestTable.eventTable.id ? guestTable.eventTable : eventTableOption
                "
                v-for="eventTableOption in eventTables"
                :key="eventTableOption.id"
              >
                {{ eventTableOption.tableNumber }}
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
<script lang="ts" src="./guest-table-update.component.ts"></script>
