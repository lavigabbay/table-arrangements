<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.venueTable.home.createOrEditLabel"
          data-cy="VenueTableCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.venueTable.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="venueTable.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="venueTable.id" readonly />
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.numberOfTables')"
              for="venue-table-numberOfTables"
            ></label>
            <input
              type="number"
              class="form-control"
              name="numberOfTables"
              id="venue-table-numberOfTables"
              data-cy="numberOfTables"
              :class="{ valid: !v$.numberOfTables.$invalid, invalid: v$.numberOfTables.$invalid }"
              v-model.number="v$.numberOfTables.$model"
              required
            />
            <div v-if="v$.numberOfTables.$anyDirty && v$.numberOfTables.$invalid">
              <small class="form-text text-danger" v-for="error of v$.numberOfTables.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.nearStageTables')"
              for="venue-table-nearStageTables"
            ></label>
            <input
              type="number"
              class="form-control"
              name="nearStageTables"
              id="venue-table-nearStageTables"
              data-cy="nearStageTables"
              :class="{ valid: !v$.nearStageTables.$invalid, invalid: v$.nearStageTables.$invalid }"
              v-model.number="v$.nearStageTables.$model"
              required
            />
            <div v-if="v$.nearStageTables.$anyDirty && v$.nearStageTables.$invalid">
              <small class="form-text text-danger" v-for="error of v$.nearStageTables.$errors" :key="error.$uid">{{
                error.$message
              }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.venueTable.venueName')" for="venue-table-venueName"></label>
            <input
              type="text"
              class="form-control"
              name="venueName"
              id="venue-table-venueName"
              data-cy="venueName"
              :class="{ valid: !v$.venueName.$invalid, invalid: v$.venueName.$invalid }"
              v-model="v$.venueName.$model"
              required
            />
            <div v-if="v$.venueName.$anyDirty && v$.venueName.$invalid">
              <small class="form-text text-danger" v-for="error of v$.venueName.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.eventOwners')"
              for="venue-table-eventOwners"
            ></label>
            <input
              type="text"
              class="form-control"
              name="eventOwners"
              id="venue-table-eventOwners"
              data-cy="eventOwners"
              :class="{ valid: !v$.eventOwners.$invalid, invalid: v$.eventOwners.$invalid }"
              v-model="v$.eventOwners.$model"
              required
            />
            <div v-if="v$.eventOwners.$anyDirty && v$.eventOwners.$invalid">
              <small class="form-text text-danger" v-for="error of v$.eventOwners.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.groomParents')"
              for="venue-table-groomParents"
            ></label>
            <input
              type="text"
              class="form-control"
              name="groomParents"
              id="venue-table-groomParents"
              data-cy="groomParents"
              :class="{ valid: !v$.groomParents.$invalid, invalid: v$.groomParents.$invalid }"
              v-model="v$.groomParents.$model"
              required
            />
            <div v-if="v$.groomParents.$anyDirty && v$.groomParents.$invalid">
              <small class="form-text text-danger" v-for="error of v$.groomParents.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.brideParents')"
              for="venue-table-brideParents"
            ></label>
            <input
              type="text"
              class="form-control"
              name="brideParents"
              id="venue-table-brideParents"
              data-cy="brideParents"
              :class="{ valid: !v$.brideParents.$invalid, invalid: v$.brideParents.$invalid }"
              v-model="v$.brideParents.$model"
              required
            />
            <div v-if="v$.brideParents.$anyDirty && v$.brideParents.$invalid">
              <small class="form-text text-danger" v-for="error of v$.brideParents.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.weddingDate')"
              for="venue-table-weddingDate"
            ></label>
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="venue-table-weddingDate"
                  v-model="v$.weddingDate.$model"
                  name="weddingDate"
                  class="form-control"
                  :locale="currentLanguage"
                  button-only
                  today-button
                  reset-button
                  close-button
                >
                </b-form-datepicker>
              </b-input-group-prepend>
              <b-form-input
                id="venue-table-weddingDate"
                data-cy="weddingDate"
                type="text"
                class="form-control"
                name="weddingDate"
                :class="{ valid: !v$.weddingDate.$invalid, invalid: v$.weddingDate.$invalid }"
                v-model="v$.weddingDate.$model"
                required
              />
            </b-input-group>
            <div v-if="v$.weddingDate.$anyDirty && v$.weddingDate.$invalid">
              <small class="form-text text-danger" v-for="error of v$.weddingDate.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.receptionTime')"
              for="venue-table-receptionTime"
            ></label>
            <div class="d-flex">
              <input
                id="venue-table-receptionTime"
                data-cy="receptionTime"
                type="datetime-local"
                class="form-control"
                name="receptionTime"
                :class="{ valid: !v$.receptionTime.$invalid, invalid: v$.receptionTime.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.receptionTime.$model)"
                @change="updateInstantField('receptionTime', $event)"
              />
            </div>
            <div v-if="v$.receptionTime.$anyDirty && v$.receptionTime.$invalid">
              <small class="form-text text-danger" v-for="error of v$.receptionTime.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label
              class="form-control-label"
              v-text="t$('tableArrangmentsApp.venueTable.weddingTime')"
              for="venue-table-weddingTime"
            ></label>
            <div class="d-flex">
              <input
                id="venue-table-weddingTime"
                data-cy="weddingTime"
                type="datetime-local"
                class="form-control"
                name="weddingTime"
                :class="{ valid: !v$.weddingTime.$invalid, invalid: v$.weddingTime.$invalid }"
                required
                :value="convertDateTimeFromServer(v$.weddingTime.$model)"
                @change="updateInstantField('weddingTime', $event)"
              />
            </div>
            <div v-if="v$.weddingTime.$anyDirty && v$.weddingTime.$invalid">
              <small class="form-text text-danger" v-for="error of v$.weddingTime.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.venueTable.user')" for="venue-table-user"></label>
            <select class="form-control" id="venue-table-user" data-cy="user" name="user" v-model="venueTable.user">
              <option :value="null"></option>
              <option
                :value="venueTable.user && userOption.id === venueTable.user.id ? venueTable.user : userOption"
                v-for="userOption in users"
                :key="userOption.id"
              >
                {{ userOption.login }}
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
<script lang="ts" src="./venue-table-update.component.ts"></script>
