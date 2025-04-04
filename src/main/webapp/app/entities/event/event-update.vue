<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate @submit.prevent="save()">
        <h2
          id="tableArrangmentsApp.event.home.createOrEditLabel"
          data-cy="EventCreateUpdateHeading"
          v-text="t$('tableArrangmentsApp.event.home.createOrEditLabel')"
        ></h2>
        <div>
          <div class="form-group" v-if="event.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="event.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.eventName')" for="event-eventName"></label>
            <input
              type="text"
              class="form-control"
              name="eventName"
              id="event-eventName"
              data-cy="eventName"
              :class="{ valid: !v$.eventName.$invalid, invalid: v$.eventName.$invalid }"
              v-model="v$.eventName.$model"
              required
            />
            <div v-if="v$.eventName.$anyDirty && v$.eventName.$invalid">
              <small class="form-text text-danger" v-for="error of v$.eventName.$errors" :key="error.$uid">{{ error.$message }}</small>
            </div>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.eventOwners')" for="event-eventOwners"></label>
            <input
              type="text"
              class="form-control"
              name="eventOwners"
              id="event-eventOwners"
              data-cy="eventOwners"
              :class="{ valid: !v$.eventOwners.$invalid, invalid: v$.eventOwners.$invalid }"
              v-model="v$.eventOwners.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.groomParents')" for="event-groomParents"></label>
            <input
              type="text"
              class="form-control"
              name="groomParents"
              id="event-groomParents"
              data-cy="groomParents"
              :class="{ valid: !v$.groomParents.$invalid, invalid: v$.groomParents.$invalid }"
              v-model="v$.groomParents.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.brideParents')" for="event-brideParents"></label>
            <input
              type="text"
              class="form-control"
              name="brideParents"
              id="event-brideParents"
              data-cy="brideParents"
              :class="{ valid: !v$.brideParents.$invalid, invalid: v$.brideParents.$invalid }"
              v-model="v$.brideParents.$model"
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.weddingDate')" for="event-weddingDate"></label>
            <b-input-group class="mb-3">
              <b-input-group-prepend>
                <b-form-datepicker
                  aria-controls="event-weddingDate"
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
                id="event-weddingDate"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.receptionTime')" for="event-receptionTime"></label>
            <div class="d-flex">
              <input
                id="event-receptionTime"
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
            <label class="form-control-label" v-text="t$('tableArrangmentsApp.event.weddingTime')" for="event-weddingTime"></label>
            <div class="d-flex">
              <input
                id="event-weddingTime"
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
<script lang="ts" src="./event-update.component.ts"></script>
