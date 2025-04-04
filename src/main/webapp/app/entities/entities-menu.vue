<template>
  <div>
    <b-dropdown-item to="/event">
      <font-awesome-icon icon="asterisk" />
      <span v-text="t$('global.menu.entities.event')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/guest">
      <font-awesome-icon icon="asterisk" />
      <span v-text="t$('global.menu.entities.guest')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/seating-table">
      <font-awesome-icon icon="asterisk" />
      <span v-text="t$('global.menu.entities.seatingTable')"></span>
    </b-dropdown-item>
    <!-- jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here -->
  </div>
</template>

<script lang="ts" src="./entities-menu.component.ts"></script>
